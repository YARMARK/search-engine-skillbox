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


    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void markAllSitesWithIndexingStatusAsField(){
        List<Site> sites = siteService.findSiteByStatus(SiteStatus.INDEXING);
        for (Site site : sites) {
            site.setStatus(SiteStatus.FAILED);
            siteService.saveSite(site);
        }
    }

    @Override
    public IndexingResponse startIndexing() {
        if (isIndexingRunning()) {
            log.warn("Indexing is already started");
            return new IndexingResponse(INDEXING_IS_ALREADY_STARTED);
        }

        List<SiteInfo> uniqueSites = getUniqueSites();

        int maxConcurrentSites = Math.min(4, uniqueSites.size());
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
        siteUrl = modifyUrlToValid(siteUrl);
        Site site = siteService.findSiteByUrl(siteUrl);
        if (site == null) {
            log.warn("Site not found for url: {}", siteUrl);
            return;
        }

        searchIndexService.deleteAllIndexesBySite(site);
        pageService.deleteAllPagesBySite(site);
        lemmaService.deleteAllLemmasBySite(site);
        siteService.deleteSite(site);

        log.info("Deleted site related info, url: {}", siteUrl);
    }

    private Site createAndSaveSite(SiteInfo info) {
        Site site = new Site();
        site.setUrl(modifyUrlToValid(info.getUrl()));
        site.setName(info.getName());
        site.setStatus(SiteStatus.INDEXING);
        site.setStatusTime(LocalDateTime.now());
        return siteService.saveSite(site);
    }

    private String modifyUrlToValid(String url) {
        if (!url.endsWith("/")) {
            url = url + "/";
        }
        return url;
    }

    private void crawlSite(Site site, String userAgent, String referrer) {
        ForkJoinPool forkJoinPool = new ForkJoinPool();
        forkJoinPools.add(forkJoinPool);
        forkJoinPool.invoke(new PageCrawler(site.getUrl(), userAgent, referrer, site, pageService, siteService, lemmaService, lemmaIndexer));
    }

    private void updateSiteStatus(Site site, SiteStatus status, String error) {
        site.setStatus(status);
        site.setLastError(error);
        site.setStatusTime(LocalDateTime.now());
        siteService.saveSite(site);
    }

    public void saveMap(Map<String, Set<String>> map, String fileName) {
        try (FileOutputStream fileOut = new FileOutputStream(fileName);
             ObjectOutputStream out = new ObjectOutputStream(fileOut)) {
             out.writeObject(map);
             log.info("Lemma forms are written to file");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

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

    @Override
    public boolean isIndexingRunning() {
        List<Site> byStatus = siteService.findSiteByStatus(SiteStatus.INDEXING);
        return !byStatus.isEmpty();
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

        if (pageService.existsPageByPath(pageUrl)) {
            log.info("Page already indexed, removing previous data: {}", pageUrl);
            pageService.deletePageByPath(pageUrl);
        }

        return parseAndSavePage(pageUrl, site);
    }


    // TODO HANDLE EXCEPTION
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
        Site site = siteService.findSiteByUrl(info.getUrl());

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
