package searchengine.services;

import searchengine.dto.response.IndexingResponse;
import searchengine.model.Page;
import searchengine.model.Site;

import java.util.List;

public interface IndexingService {

    IndexingResponse startIndexing();

    IndexingResponse stopIndexing();

    boolean isIndexingRunning();

    Page indexPage(String url);

    Long getPagesCount();

    String getContentByUrl(String url);

    Long getSitesCount();

    List<Site> getAllSites();

    Long getPageCountBySit(Site site);
}
