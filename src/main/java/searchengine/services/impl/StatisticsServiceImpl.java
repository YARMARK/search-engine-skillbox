package searchengine.services.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import searchengine.dto.statistics.DetailedStatisticsItem;
import searchengine.dto.statistics.StatisticsData;
import searchengine.dto.statistics.StatisticsResponse;
import searchengine.dto.statistics.TotalStatistics;
import searchengine.model.Site;
import searchengine.model.SiteStatus;
import searchengine.repository.LemmaRepository;
import searchengine.repository.PageRepository;
import searchengine.repository.SiteRepository;
import searchengine.services.StatisticsService;

import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StatisticsServiceImpl implements StatisticsService {

    private final SiteRepository siteRepository;
    private final PageRepository pageRepository;
    private final LemmaRepository lemmaRepository;

    @Override
    @Transactional(readOnly = true)
    public StatisticsResponse getStatistics() {
        List<Site> sites = siteRepository.findAll();

        TotalStatistics total = buildTotalStatistics(sites);
        List<DetailedStatisticsItem> detailed = buildDetailedStatistics(sites);

        StatisticsData data = new StatisticsData();
        data.setTotal(total);
        data.setDetailed(detailed);

        StatisticsResponse response = new StatisticsResponse();
        response.setStatistics(data);
        response.setResult(true);

        return response;
    }

    private TotalStatistics buildTotalStatistics(List<Site> sites) {
        int totalSites = sites.size();
        int totalPages = (int) pageRepository.count();
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
        return sites.stream()
                .map(site -> {
                    DetailedStatisticsItem item = new DetailedStatisticsItem();
                    item.setName(site.getName());
                    item.setUrl(site.getUrl());
                    item.setPages(pageRepository.countBySite(site));
                    item.setLemmas(Optional.ofNullable(lemmaRepository.countLemmasByWebSite(site)).orElse(0));
                    item.setStatus(site.getStatus().name());
                    item.setStatusTime(site.getStatusTime().toEpochSecond(ZoneOffset.UTC));
                    if (site.getLastError() != null && !site.getLastError().isBlank()) {
                        item.setError(site.getLastError());
                    }
                    return item;
                })
                .collect(Collectors.toList());
    }
}
