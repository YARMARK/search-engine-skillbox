package searchengine.services.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Service;
import searchengine.config.SiteInfo;
import searchengine.config.SitesList;
import searchengine.dto.response.IndexingResponse;
import searchengine.model.Page;
import searchengine.model.Site;
import searchengine.model.SiteStatus;
import searchengine.repository.PageRepository;
import searchengine.repository.SiteRepository;
import searchengine.services.IndexingService;
import searchengine.task.PageCrawler;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class IndexingServiceImpl implements IndexingService {

    private final SitesList sitesList;

    private final SiteRepository siteRepository;

    private final PageRepository pageRepository;

    private final List<ForkJoinPool> forkJoinPools = new ArrayList<>();

    private final List<Thread> threads = new ArrayList<>();

    private static final String INDEXING_WAS_TERMINATED_BY_USER = "Индексация остановлена пользователем";

    private static final String INDEXING_IS_ALREADY_STARTED = "Индексация уже запущена";

    private static final String INDEXING_IS_NOT_STARTED = "Индексация не запущена";


    @Override
    public IndexingResponse startIndexing() {
        if (isIndexingStarted()) {
            log.warn("Indexing is already started");
            return new IndexingResponse(INDEXING_IS_ALREADY_STARTED);
        }

        ExecutorService executorService = Executors.newFixedThreadPool(sitesList.getSites().size());
        List<Future<?>> futures = new ArrayList<>();

        for (SiteInfo info : sitesList.getSites()) {
            Future<?> future = executorService.submit(() -> handleSiteIndexing(info));
            futures.add(future);
        }

        executorService.shutdown();

        waitForCompletionAsync(futures);

        return new IndexingResponse();
    }

    private void handleSiteIndexing(SiteInfo info) {
        try {
            if (Thread.interrupted()) {  // Проверка прерывания потока
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

            if (allTasksCompleted) {
                log.info("All indexing tasks completed successfully.");
//            notifyClients("Indexing completed");
            } else {
                log.warn("Indexing finished with interruptions or errors.");
//            notifyClients("Indexing was interrupted");
            }
        }, "Indexing-Waiter").start();
    }

    private void processSite(SiteInfo info, String referrer, String userAgent) throws InterruptedException {
        if (Thread.currentThread().isInterrupted()) {
            throw new InterruptedException();
        }

        deleteExistingData(info.getUrl());
        Site site = createAndSaveSite(info);
        log.info("Created new site: {}", info.getUrl());

        try {
            crawlSite(site, userAgent, referrer);
            updateSiteStatus(site, SiteStatus.INDEXED, null);
        } catch (Exception e) {
            log.error("Error processing site: {}", info.getUrl(), e);
            updateSiteStatus(site, SiteStatus.FAILED, e.getMessage());
            throw e;
        }

    }

    private void deleteExistingData(String siteUrl) {
        pageRepository.deleteBySiteUrl(siteUrl);
        siteRepository.deleteByUrl(siteUrl);
    }

    private Site createAndSaveSite(SiteInfo info) {
        Site site = new Site();
        site.setUrl(info.getUrl());
        site.setName(info.getName());
        site.setStatus(SiteStatus.INDEXING);
        site.setStatusTime(LocalDateTime.now());
        return siteRepository.save(site);
    }

    private void crawlSite(Site site, String userAgent, String referrer) {
        ForkJoinPool forkJoinPool = new ForkJoinPool();
        forkJoinPools.add(forkJoinPool);
        forkJoinPool.invoke(new PageCrawler(site.getUrl(), userAgent, referrer, site, pageRepository, siteRepository));
    }

    private void updateSiteStatus(Site site, SiteStatus status, String error) {
        site.setStatus(status);
        site.setLastError(error);
        site.setStatusTime(LocalDateTime.now());
        siteRepository.save(site);
    }

    @Override
    public IndexingResponse stopIndexing() {
        if (!isIndexingStarted()) {
            return new IndexingResponse(INDEXING_IS_NOT_STARTED);
        }
        threads.forEach(Thread::interrupt);
        threads.clear();

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

        for (Site site : siteRepository.findByStatus(SiteStatus.INDEXING)) {
            updateSiteStatus(site, SiteStatus.FAILED, INDEXING_WAS_TERMINATED_BY_USER);
        }
        return new IndexingResponse();
    }

    boolean isIndexingStarted() {
        List<Site> byStatus = siteRepository.findByStatus(SiteStatus.INDEXING);
        return !byStatus.isEmpty();
    }

    @Override
    public IndexingResponse indexPage(String url) {
        SiteInfo info = isPageFromSiteListConfig(url);
        if (info == null) {
            log.warn("URL does not match any configured site: {}", url);
            String message = "Нет сайтов с заданным URL: " + url;
            return new IndexingResponse(message);
        }
        log.info("Matched site: {}", info);

        Site site = findOrCreateSite(info);

        if (pageRepository.existsPageByPath(url)) {
            log.info("Page already indexed, removing previous data: {}", url);
            pageRepository.deletePageByPath(url);
        }

        // Парсим страницу и сохраняем
        parseAndSavePage(url, site);
        return new IndexingResponse();
    }

    private Site findOrCreateSite(SiteInfo info) {
        Site site = siteRepository.findByUrl(info.getUrl());

        if (site == null) {
            site = createAndSaveSite(info);
        }

        return site;
    }

    private void parseAndSavePage(String url, Site site) {
        try {
            Connection.Response response = Jsoup.connect(url).execute();
            Document document = response.parse();

            Page page = new Page();
            page.setCode(response.statusCode());
            page.setPath(url);
            page.setContent(document.html());
            page.setSite(site);

            site.setStatus(SiteStatus.INDEXED);
            site.setStatusTime(LocalDateTime.now());

            siteRepository.save(site);
            pageRepository.save(page);

            log.info("Page parsed and saved: {}", url);

        } catch (IOException e) {
            log.error("Failed to fetch or parse page: {}", url, e);
            site.setStatus(SiteStatus.FAILED);
            site.setStatusTime(LocalDateTime.now());
            siteRepository.save(site);
        }
    }

    private SiteInfo isPageFromSiteListConfig(String pageUrl) {
        try {
            URL pageUri = new URL(pageUrl);
            String pageHost = pageUri.getHost().replaceFirst("^www\\.", "");

            for (SiteInfo site : sitesList.getSites()) {
                URL siteUrl = new URL(site.getUrl());
                String siteHost = siteUrl.getHost().replaceFirst("^www\\.", "");

                // Сравниваем домены
                if (pageHost.equalsIgnoreCase(siteHost)) {
                    return site;
                }
            }
        } catch (MalformedURLException e) {
            log.error("MalformedURLException {}", e.getMessage());
            return null;
        }

        return null;
    }
}
