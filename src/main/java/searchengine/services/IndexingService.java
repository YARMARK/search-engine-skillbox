package searchengine.services;

import searchengine.dto.response.IndexingResponse;
import searchengine.model.Site;

import java.util.List;

public interface IndexingService {

    IndexingResponse startIndexing();

    IndexingResponse stopIndexing();

    boolean isIndexingRunning();

    IndexingResponse indexPage(String url);

    String getContentByUrl(String url);

    Site getSiteByUrl(String url);

    Long getPagesCount();

    Long getSitesCount();

    List<Site> getAllWebSites();
}
