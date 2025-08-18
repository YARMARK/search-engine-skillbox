package searchengine.services.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import searchengine.config.SitesList;
import searchengine.dto.statistics.DetailedStatisticsItem;
import searchengine.dto.statistics.StatisticsData;
import searchengine.dto.statistics.StatisticsResponse;
import searchengine.dto.statistics.TotalStatistics;
import searchengine.model.Site;
import searchengine.model.SiteStatus;
import searchengine.repository.LemmaRepository;
import searchengine.repository.SiteRepository;
import searchengine.services.PageService;
import searchengine.services.StatisticsService;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class StatisticsServiceImpl implements StatisticsService {

    private final SiteRepository siteRepository;

    private final PageService pageService;

    private final LemmaRepository lemmaRepository;

    private final SitesList sitesList;

    @Override
    @Transactional(readOnly = true)
    public StatisticsResponse getStatistics() {
        List<Site> sites = siteRepository.findAll();

        StatisticsData data = new StatisticsData();
        data.setTotal(buildTotalStatistics(sites));
        data.setDetailed(buildDetailedStatistics(sites));

        StatisticsResponse response = new StatisticsResponse();
        response.setResult(true);
        response.setStatistics(data);
        return response;
    }

    private TotalStatistics buildTotalStatistics(List<Site> sites) {
        int totalSites = sitesList.getSites().size();
        int totalPages = pageService.countAllPages();
        int totalLemmas = Optional.ofNullable(lemmaRepository.countAllLemmas()).orElse(0);
        boolean isIndexing = sites.stream().anyMatch(site -> site.getStatus() == SiteStatus.INDEXING);

        TotalStatistics total = new TotalStatistics();
        total.setSites(totalSites);
        total.setPages(totalPages);
        total.setLemmas(totalLemmas);
        total.setIndexing(isIndexing);
        return total;
    }

    private List<DetailedStatisticsItem> buildDetailedStatistics(List<Site> sites) {
        return sitesList.getSites().stream()
                .map(configSite -> {
                    Optional<Site> optionalSite = sites.stream()
                            .filter(site -> site.getUrl().equals(configSite.getUrl()))
                            .findFirst();

                    DetailedStatisticsItem item = new DetailedStatisticsItem();
                    item.setName(configSite.getName());
                    item.setUrl(configSite.getUrl());

                    if (optionalSite.isPresent()) {
                        Site site = optionalSite.get();
                        item.setPages(pageService.countPageBySite(site));
                        item.setLemmas(Optional.ofNullable(lemmaRepository.countLemmasBySite(site)).orElse(0));
                        item.setStatus(site.getStatus().name());

                        LocalDateTime statusTime = site.getStatusTime() != null
                                ? site.getStatusTime()
                                : LocalDateTime.now();
                        item.setStatusTime(statusTime.toEpochSecond(ZoneOffset.ofHours(-4)));

                        String error = Optional.ofNullable(site.getLastError())
                                .filter(err -> !err.isBlank())
                                .orElse("-");
                        item.setError(error);
                    } else {
                        item.setPages(0);
                        item.setLemmas(0);
                        item.setStatus("NOT INDEXED YET");
                        item.setStatusTime(LocalDateTime.now().toEpochSecond(ZoneOffset.ofHours(-4)));
                        item.setError("N/A");
                    }

                    return item;
                })
                .toList();
    }
}
