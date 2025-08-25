package searchengine.services.indexing;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import searchengine.config.SiteInfo;
import searchengine.config.SitesList;
import searchengine.dto.response.IndexingResponse;
import searchengine.model.Page;
import searchengine.model.Site;
import searchengine.model.SiteStatus;
import searchengine.morpholgy.LemmaIndexer;
import searchengine.services.IndexingService;
import searchengine.services.PageService;
import searchengine.services.SearchIndexService;
import searchengine.services.SiteService;
import searchengine.services.persistency.LemmaServiceImpl;
import searchengine.task.PageCrawler;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * {@inheritDoc}
 * <p>
 * Реализует функционал запуска, обработки и завершения индексации сайтов.
 * Использует многопоточность для параллельной обработки нескольких сайтов и страниц.
 * Поддерживает сохранение ошибок и логирование состояния процесса.
 * </p>
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class IndexingServiceImpl implements IndexingService {

    private final SitesList sitesList;

    private final SiteService siteService;

    private final PageService pageService;

    private final SearchIndexService searchIndexService;

    private final LemmaServiceImpl lemmaService;

    private final LemmaIndexer lemmaIndexer;

    private final List<ForkJoinPool> forkJoinPools = new ArrayList<>();

    private final List<Thread> threads = new ArrayList<>();

    private ExecutorService siteExecutor;

    private static final String INDEXING_WAS_TERMINATED_BY_USER = "Индексация остановлена пользователем";

    private static final String INDEXING_IS_ALREADY_STARTED = "Индексация уже запущена";

    private static final String INDEXING_IS_NOT_STARTED = "Индексация не запущена";

    /**
     * Метод, вызываемый при запуске приложения.
     * Проверяет все сайты со статусом {@link SiteStatus#INDEXING} и переводит их в статус {@link SiteStatus#FAILED},
     * чтобы завершить некорректные или прерванные индексации.
     */
    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void markAllSitesWithIndexingStatusAsField() {
        List<Site> sites = siteService.findSiteByStatus(SiteStatus.INDEXING);
        for (Site site : sites) {
            site.setStatus(SiteStatus.FAILED);
            siteService.saveSite(site);
        }
    }

    /**
     * Запускает процесс индексации всех уникальных сайтов из списка.
     * <p>
     * Использует {@link ExecutorService} для параллельной обработки сайтов.
     * Каждый сайт обрабатывается в отдельной задаче.
     * </p>
     *
     * @return {@link IndexingResponse} с информацией о начале индексации
     */
    @Override
    public IndexingResponse startIndexing() {
        if (isIndexingRunning()) {
            log.warn("Indexing is already started");
            return new IndexingResponse(INDEXING_IS_ALREADY_STARTED);
        }

        List<SiteInfo> uniqueSites = getUniqueSites();

        int configuredMax = Math.max(1, sitesList.getMaxConcurrentSites());
        int maxConcurrentSites = Math.min(configuredMax, uniqueSites.size());
        siteExecutor = Executors.newFixedThreadPool(maxConcurrentSites);
        List<Future<?>> futures = new ArrayList<>();

        for (SiteInfo info : uniqueSites) {
            Future<?> future = siteExecutor.submit(() -> handleSiteIndexing(info));
            futures.add(future);
        }

        siteExecutor.shutdown();

        waitForCompletionAsync(futures);

        return new IndexingResponse();
    }

    /**
     * Получает список уникальных сайтов для индексации.
     * <p>
     * Удаляет дубликаты по URL и возвращает уникальные значения.
     * </p>
     *
     * @return список уникальных {@link SiteInfo}
     */
    private List<SiteInfo> getUniqueSites() {
        if (!sitesList.getSites().isEmpty()) {
            return sitesList.getSites().stream()
                    .distinct()
                    .collect(Collectors.toMap(
                            SiteInfo::getUrl,
                            Function.identity(),
                            (existing, duplicate) -> existing
                    ))
                    .values()
                    .stream()
                    .toList();
        }
        return Collections.emptyList();
    }

    /**
     * Обрабатывает индексацию одного сайта.
     * <p>
     * Проверяет прерывание потока и вызывает основной метод {@link #processSite(SiteInfo, String, String)}.
     * Ловит и логирует все исключения.
     * </p>
     *
     * @param info информация о сайте
     */
    private void handleSiteIndexing(SiteInfo info) {
        try {
            if (Thread.interrupted()) {
                throw new InterruptedException();
            }
            processSite(info, sitesList.getReferrer(), sitesList.getUserAgent());
        } catch (InterruptedException e) {
            log.warn("Task was interrupted: {}", Thread.currentThread().getName());
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            log.error("Unexpected exception during site indexing: {}", info.getUrl(), e);
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Асинхронно ожидает завершения всех задач индексации.
     * <p>
     * После завершения всех задач сохраняет формы лемм в файл и логирует результат.
     * </p>
     *
     * @param futures список {@link Future} задач индексации
     */
    private void waitForCompletionAsync(List<Future<?>> futures) {
        new Thread(() -> {
            boolean allTasksCompleted = true;

            for (Future<?> future : futures) {
                try {
                    future.get();
                } catch (Exception e) {
                    log.warn("Exception while waiting for task completion: {}", e.getMessage());
                    allTasksCompleted = false;
                }
            }

            saveMap(lemmaService.getLemmaForms(), "lemma/lemma-forms.txt");
            if (allTasksCompleted) {
                log.info("All indexing tasks completed successfully.");
            } else {
                log.warn("Indexing finished with interruptions or errors.");
            }
        }, "Indexing-Waiter").start();
    }

    /**
     * Основной метод обработки индексации сайта.
     * <p>
     * Подготавливает сайт для индексации, очищает старые данные, выполняет обход сайта
     * и обновляет статус после успешной индексации или ошибки.
     * </p>
     *
     * @param info      информация о сайте
     * @param referrer  заголовок Referrer для HTTP-запросов
     * @param userAgent User-Agent для HTTP-запросов
     * @throws InterruptedException если поток был прерван
     */
    private void processSite(SiteInfo info, String referrer, String userAgent) throws InterruptedException {
        log.info("Start indexing site: {}", info.getUrl());

        if (Thread.currentThread().isInterrupted()) {
            throw new InterruptedException();
        }

        Site site = prepareSite(info);
        log.info("Prepared site for indexing: {}", site.getUrl());

        try {
            crawlSite(site, userAgent, referrer);
            updateSiteStatus(site, SiteStatus.INDEXED, null);
        } catch (Exception e) {
            log.error("Error processing site: {}", site.getUrl(), e);
            updateSiteStatus(site, SiteStatus.FAILED, e.getMessage());
            throw e;
        }
    }

    /**
     * Подготавливает объект {@link Site} для индексации.
     * <p>
     * Если сайт уже существует в базе, очищает его старые данные.
     * Устанавливает статус {@link SiteStatus#INDEXING}.
     * </p>
     *
     * @param info информация о сайте
     * @return подготовленный объект {@link Site}
     */
    private Site prepareSite(SiteInfo info) {
        String siteUrl = modifyUrlToValid(info.getUrl());
        Site site = siteService.findSiteByUrl(siteUrl);

        if (site == null) {
            site = new Site();
            site.setUrl(siteUrl);
            site.setName(info.getName());
        } else {
            deleteSiteRelatedInfo(site);
        }

        site.setStatus(SiteStatus.INDEXING);
        site.setStatusTime(LocalDateTime.now());
        site.setLastError(null);

        return siteService.saveSite(site);
    }

    /**
     * Удаляет все связанные с сайтом данные из базы данных.
     * <p>
     * Удаляет страницы, леммы и индексы поиска.
     * </p>
     *
     * @param site объект {@link Site}, для которого нужно удалить данные
     */
    private void deleteSiteRelatedInfo(Site site) {
        searchIndexService.deleteAllIndexesBySite(site);
        pageService.deleteAllPagesBySite(site);
        lemmaService.deleteAllLemmasBySite(site);
        log.info("Cleared old data for site: {}", site.getUrl());
    }

    /**
     * Преобразует URL сайта в корректный формат для индексации.
     * <p>
     * Добавляет слэш в конец, если его нет.
     * </p>
     *
     * @param url исходный URL
     * @return скорректированный URL
     */
    private String modifyUrlToValid(String url) {
        if (!url.endsWith("/")) {
            url = url + "/";
        }
        return url;
    }

    /**
     * Запускает рекурсивное обходное сканирование сайта.
     * Используется ForkJoinPool для параллельной обработки страниц.
     *
     * @param site      Сайт, который необходимо проиндексировать.
     * @param userAgent User-Agent, который будет использован при HTTP-запросах.
     * @param referrer  Заголовок Referrer для HTTP-запросов.
     */
    private void crawlSite(Site site, String userAgent, String referrer) {
        int parallelism = Math.max(1, sitesList.getCrawlerParallelism());
        ForkJoinPool forkJoinPool = new ForkJoinPool(parallelism);
        forkJoinPools.add(forkJoinPool);
        forkJoinPool.invoke(new PageCrawler(site.getUrl(), userAgent, referrer, site, pageService, siteService, lemmaService, lemmaIndexer));
    }

    /**
     * Обновляет статус сайта и сохраняет возможную ошибку.
     *
     * @param site   Сайт, который необходимо обновить.
     * @param status Новый статус сайта.
     * @param error  Сообщение об ошибке, если индексирование завершилось с ошибкой.
     */
    private void updateSiteStatus(Site site, SiteStatus status, String error) {
        site.setStatus(status);
        site.setLastError(error);
        site.setStatusTime(LocalDateTime.now());
        siteService.saveSite(site);
    }

    /**
     * Сохраняет карту лемм (ключевые слова и их формы) в файл.
     *
     * @param map      Карта лемм.
     * @param fileName Имя файла для сохранения.
     */
    public void saveMap(Map<String, Set<String>> map, String fileName) {
        try (FileOutputStream fileOut = new FileOutputStream(fileName);
             ObjectOutputStream out = new ObjectOutputStream(fileOut)) {
            out.writeObject(map);
            log.info("Lemma forms are written to file");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Останавливает процесс индексирования всех сайтов.
     * Прерывает все потоки и ForkJoinPool'ы, обновляет статус сайтов на FAILED,
     * если они находились в процессе индексирования.
     *
     * @return Объект IndexingResponse с результатом операции.
     */
    @Override
    public IndexingResponse stopIndexing() {
        if (!isIndexingRunning()) {
            return new IndexingResponse(INDEXING_IS_NOT_STARTED);
        }
        threads.forEach(Thread::interrupt);
        threads.clear();

        if (siteExecutor != null && !siteExecutor.isShutdown()) {
            siteExecutor.shutdownNow();
            try {
                if (!siteExecutor.awaitTermination(60, TimeUnit.SECONDS)) {
                    log.warn("SiteExecutor wasn't terminate");
                }
            } catch (Exception e) {
                log.error("Error during siteExecutor interrupt: {}", e.getMessage(), e);
            }
        }

        forkJoinPools.forEach(pool -> {
            pool.shutdownNow();
            try {
                if (!pool.awaitTermination(60, TimeUnit.SECONDS)) {
                    log.warn("ForkJoinPool wasn't terminate");
                }
            } catch (Exception e) {
                log.error("Error during forkJoinPool interrupt: {}", e.getMessage(), e);
            }
        });
        forkJoinPools.clear();

        for (Site site : siteService.findSiteByStatus(SiteStatus.INDEXING)) {
            updateSiteStatus(site, SiteStatus.FAILED, INDEXING_WAS_TERMINATED_BY_USER);
        }
        return new IndexingResponse();
    }

    /**
     * Проверяет, запущено ли индексирование хотя бы одного сайта.
     *
     * @return true, если есть сайты со статусом INDEXING, иначе false.
     */
    @Override
    public boolean isIndexingRunning() {
        List<Site> byStatus = siteService.findSiteByStatus(SiteStatus.INDEXING);
        return !byStatus.isEmpty();
    }

    /**
     * Индексирует страницу по указанному URL.
     * <p>
     * Метод выполняет следующие действия:
     * <ol>
     *     <li>Проверяет, принадлежит ли URL к одному из сайтов, указанных в конфигурации.</li>
     *     <li>Проверяет доступность соединения с URL.</li>
     *     <li>Если сайт присутствует в конфигурации и доступен, ищет существующий объект Site или создаёт новый.</li>
     *     <li>Если страница уже индексировалась, удаляет предыдущие данные.</li>
     *     <li>Парсит страницу и сохраняет её содержимое в базе данных.</li>
     * </ol>
     * </p>
     *
     * @param pageUrl URL страницы для индексации
     * @return объект {@link Page}, содержащий данные индексированной страницы, или {@code null}, если:
     *         <ul>
     *             <li>URL не соответствует ни одному из сайтов конфигурации</li>
     *             <li>Соединение с URL недоступно</li>
     *         </ul>
     */
    @Override
    public Page indexPage(String pageUrl) {
        SiteInfo info = isPageFromSiteListConfig(pageUrl);

        if (info == null) {
            log.debug("URL does not match any configured site: {}", pageUrl);
            return null;
        }

        if (!isConnectionAvailable(pageUrl)) {
            log.warn("Connection to URL is unavailable: {}", pageUrl);
            return null;
        }

        log.info("Site is from config list and available: {}", info.getUrl());

        Site site = findOrCreateSite(info);

        if (pageService.existsPageByPath(pageUrl)) {
            log.info("Page already indexed, removing previous data: {}", pageUrl);
            deletePageInfo(pageUrl);
        }

        return parseAndSavePage(pageUrl, site);
    }

    /**
     * Проверяет доступность соединения с указанным URL.
     *
     * @param url URL для проверки.
     * @return true, если соединение успешно и код ответа < 400, иначе false.
     */
    private boolean isConnectionAvailable(String url) {
        int statusCode = 0;
        try {
            statusCode = Jsoup.connect(url).execute().statusCode();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return statusCode < 400;
    }

    /**
     * Находит сайт в базе по URL или создает новый.
     *
     * @param info Информация о сайте.
     * @return Найденный или созданный объект Site.
     */
    private Site findOrCreateSite(SiteInfo info) {
        Site site = siteService.findSiteByUrl(info.getUrl());

        if (site == null) {
            site = createAndSaveSite(info);
        }

        return site;
    }

    /**
     * Создает новый сайт и сохраняет его в базе.
     *
     * @param info Информация о сайте.
     * @return Созданный объект Site.
     */
    private Site createAndSaveSite(SiteInfo info) {
        Site site = new Site();
        site.setUrl(modifyUrlToValid(info.getUrl()));
        site.setName(info.getName());
        site.setStatus(SiteStatus.INDEXING);
        site.setStatusTime(LocalDateTime.now());
        return siteService.saveSite(site);
    }

    /**
     * Удаляет страницу и ее индексы.
     *
     * @param pageUrl URL страницыю
     */
    private void deletePageInfo(String pageUrl) {
        Page page = pageService.findPageByPath(pageUrl);
        searchIndexService.deleteIndexByPage(page);
        pageService.deletePage(page);
    }

    /**
     * Парсит страницу по URL и сохраняет её содержимое в базу.
     * Обновляет статус сайта на INDEXED или FAILED при ошибке.
     *
     * @param url  URL страницы.
     * @param site Сайт, которому принадлежит страница.
     * @return Объект Page с данными страницы или null при ошибке.
     */
    private Page parseAndSavePage(String url, Site site) {
        Page page = null;
        try {
            Connection.Response response = Jsoup.connect(url).execute();
            Document document = response.parse();

            page = new Page();
            page.setCode(response.statusCode());
            page.setPath(url);
            page.setContent(document.html());
            page.setSite(site);

            site.setStatus(SiteStatus.INDEXED);
            site.setStatusTime(LocalDateTime.now());

            siteService.saveSite(site);
            page = pageService.savePage(page);

        } catch (IOException e) {
            log.error("Failed to fetch or parse page: {}", url, e);
            site.setStatus(SiteStatus.FAILED);
            site.setStatusTime(LocalDateTime.now());
            siteService.saveSite(site);
        }

        return page;
    }

    /**
     * Проверяет, принадлежит ли страница одному из сайтов, указанных в конфигурации.
     *
     * @param pageUrl URL страницы.
     * @return Информация о сайте SiteInfo, если страница совпадает, иначе null.
     */
    private SiteInfo isPageFromSiteListConfig(String pageUrl) {
        try {
            URL pageUri = new URL(pageUrl);
            String pageHost = pageUri.getHost().replaceFirst("^www\\.", "");

            for (SiteInfo site : sitesList.getSites()) {
                URL siteUrl = new URL(site.getUrl());
                String siteHost = siteUrl.getHost().replaceFirst("^www\\.", "");

                if (pageHost.equalsIgnoreCase(siteHost)) {
                    return site;
                }
            }
        } catch (MalformedURLException e) {
            log.warn("MalformedURLException {}", e.getMessage());
            return null;
        }

        return null;
    }
}
