package searchengine.services.impl;

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
import searchengine.facade.LemmaFacade;
import searchengine.model.Page;
import searchengine.model.Site;
import searchengine.model.SiteStatus;
import searchengine.repository.LemmaRepository;
import searchengine.repository.PageRepository;
import searchengine.repository.SearchIndexRepository;
import searchengine.repository.SiteRepository;
import searchengine.services.IndexingService;
import searchengine.task.PageCrawler;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class IndexingServiceImpl implements IndexingService {

    private final SitesList sitesList;

    private final SiteRepository siteRepository;

    private final PageRepository pageRepository;

    private final SearchIndexRepository indexRepository;

    private final LemmaRepository lemmaRepository;

    private final LemmaFacade lemmaFacade;

    private final ForkJoinPool forkJoinPool = new ForkJoinPool(Runtime.getRuntime().availableProcessors());

    private final List<Thread> threads = new ArrayList<>();

    private static final String INDEXING_WAS_TERMINATED_BY_USER = "Индексация остановлена пользователем";

    private static final String INDEXING_IS_ALREADY_STARTED = "Индексация уже запущена";

    private static final String INDEXING_IS_NOT_STARTED = "Индексация не запущена";


    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void markAllSitesWithIndexingStatusAsField() {
        List<Site> sites = siteRepository.findByStatus(SiteStatus.INDEXING);
        for (Site site : sites) {
            site.setStatus(SiteStatus.FAILED);
            siteRepository.save(site);
        }
    }

    @Override
    public IndexingResponse startIndexing() {
        if (isIndexingRunning()) {
            log.warn("Indexing is already started");
            return new IndexingResponse(INDEXING_IS_ALREADY_STARTED);
        }

        List<CompletableFuture<Void>> futures = new ArrayList<>();

        for (SiteInfo info : sitesList.getSites()) {
            CompletableFuture<Void> future = CompletableFuture.runAsync(() -> handleSiteIndexing(info), forkJoinPool);
            futures.add(future);
        }

        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                .thenRun(() -> {
                    saveMap(lemmaFacade.getLemmaForms(), "lemma/lemma-forms.txt");
                    log.info("All indexing tasks completed.");
                })
                .exceptionally(ex -> {
                    log.error("Error during indexing", ex);
                    return null;
                });

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

    private void processSite(SiteInfo info, String referrer, String userAgent) throws InterruptedException {
        if (Thread.currentThread().isInterrupted()) {
            throw new InterruptedException();
        }

        deleteExistingData(info.getUrl());
        Site site = createAndSaveSite(info);
        log.info("Created new site: {}", info.getUrl());

        try {
            forkJoinPool.invoke(new PageCrawler(site.getUrl(), userAgent, referrer, site, pageRepository, siteRepository, lemmaFacade));
            updateSiteStatus(site, SiteStatus.INDEXED, null);
        } catch (Exception e) {
            log.error("Error processing site: {}", info.getUrl(), e);
            updateSiteStatus(site, SiteStatus.FAILED, e.getMessage());
            throw e;
        }

    }

    private void deleteExistingData(String siteUrl) {
        siteUrl = modifyUrlToValid(siteUrl);
        Site site = siteRepository.findByUrl(siteUrl);
        if (site == null) {
            log.warn("Site not found for url: {}", siteUrl);
            return;
        }

        indexRepository.deleteAllIndexesBySite(site);
        pageRepository.deleteAllPagesBySite(site);
        lemmaRepository.deleteAllLemmasBySite(site);
        siteRepository.delete(site);

        log.info("Deleted site related info, url: {}", siteUrl);
    }

    private Site createAndSaveSite(SiteInfo info) {
        Site site = new Site();
        site.setUrl(modifyUrlToValid(info.getUrl()));
        site.setName(info.getName());
        site.setStatus(SiteStatus.INDEXING);
        site.setStatusTime(LocalDateTime.now());
        return siteRepository.save(site);
    }

    private String modifyUrlToValid(String url) {
        if (!url.endsWith("/")) {
            url = url + "/";
        }
        return url;
    }

    private void updateSiteStatus(Site site, SiteStatus status, String error) {
        site.setStatus(status);
        site.setLastError(error);
        site.setStatusTime(LocalDateTime.now());
        siteRepository.save(site);
    }

    public void saveMap(Map<String, Set<String>> map, String fileName) {
        try (FileOutputStream fileOut = new FileOutputStream(fileName);
             ObjectOutputStream out = new ObjectOutputStream(fileOut)) {
            out.writeObject(map);
            log.info("Lemma forms are written to file");
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }
    }

    @Override
    public IndexingResponse stopIndexing() {
        if (!isIndexingRunning()) {
            return new IndexingResponse(INDEXING_IS_NOT_STARTED);
        }
        if (!forkJoinPool.isShutdown()) {
            forkJoinPool.shutdownNow();  // Попытка прервать все задачи

            try {
                // Ждем максимум 60 секунд, чтобы пул корректно завершился
                if (!forkJoinPool.awaitTermination(60, TimeUnit.SECONDS)) {
                    log.warn("ForkJoinPool did not terminate within timeout");
                }
            } catch (InterruptedException e) {
                log.error("Interrupted while waiting for ForkJoinPool termination", e);
                Thread.currentThread().interrupt();
            }
        }

        for (Site site : siteRepository.findByStatus(SiteStatus.INDEXING)) {
            updateSiteStatus(site, SiteStatus.FAILED, INDEXING_WAS_TERMINATED_BY_USER);
        }

        log.info("Indexing stop by user");
        return new IndexingResponse();
    }

    @Override
    public boolean isIndexingRunning() {
        boolean hasRunningSites = !siteRepository.findByStatus(SiteStatus.INDEXING).isEmpty();
        boolean poolIsActive = !forkJoinPool.isShutdown() && !forkJoinPool.isTerminated();
        return hasRunningSites && poolIsActive;
    }

    @Override
    public Page indexPage(String pageUrl) {
        SiteInfo info = isPageFromSiteListConfig(pageUrl);

        if (info == null) {
            log.warn("URL does not match any configured site: {}", pageUrl);
            return null;
        }

        if (!isConnectionAvailable(pageUrl)) {
            log.warn("Connection to URL is unavailable: {}", pageUrl);
            return null;
        }

        log.info("Site is from config list and available: {}", info.getUrl());

        Site site = findOrCreateSite(info);

        if (pageRepository.existsPageByPath(pageUrl)) {
            log.info("Page already indexed, removing previous data: {}", pageUrl);
            pageRepository.deletePageByPath(pageUrl);
        }

        return parseAndSavePage(pageUrl, site);
    }


    private boolean isConnectionAvailable(String url) {
        int statusCode = 0;
        try {
            statusCode = Jsoup.connect(url).execute().statusCode();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return statusCode < 400;
    }

    private Site findOrCreateSite(SiteInfo info) {
        Site site = siteRepository.findByUrl(info.getUrl());

        if (site == null) {
            site = createAndSaveSite(info);
        }

        return site;
    }

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

            siteRepository.save(site);
            page = pageRepository.save(page);

        } catch (IOException e) {
            log.error("Failed to fetch or parse page: {}", url, e);
            site.setStatus(SiteStatus.FAILED);
            site.setStatusTime(LocalDateTime.now());
            siteRepository.save(site);
        }

        return page;
    }

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
            log.error("MalformedURLException {}", e.getMessage());
            return null;
        }

        return null;
    }
}
