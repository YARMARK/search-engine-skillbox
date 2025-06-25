package searchengine.services.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import searchengine.config.SiteInfo;
import searchengine.config.SitesList;
import searchengine.dto.response.IndexingResponse;
import searchengine.model.Site;
import searchengine.model.SiteStatus;
import searchengine.repository.PageRepository;
import searchengine.repository.SiteRepository;
import searchengine.services.IndexingService;
import searchengine.task.PageCrawler;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ForkJoinPool;
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

        for (SiteInfo info : sitesList.getSites()) {
            Thread thread = new Thread(() -> {
                try {
                    processSite(info, sitesList.getReferrer(), sitesList.getUserAgent());
                } catch (InterruptedException e) {
                    log.error("Thread was interrupted: {}", Thread.currentThread().getName());
                }
            });
            threads.add(thread);
            thread.start();
        }
        return new IndexingResponse();
    }

    private void processSite(SiteInfo info, String referrer, String userAgent) throws InterruptedException {
        if (Thread.currentThread().isInterrupted()) {
            throw new InterruptedException();
        }

        deleteExistingData(info.getUrl());
        Site site = createAndSaveSite(info);

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
}
