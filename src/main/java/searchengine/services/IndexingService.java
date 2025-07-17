package searchengine.services;

import searchengine.dto.response.IndexingResponse;
import searchengine.model.Page;

public interface IndexingService {

    IndexingResponse startIndexing();

    IndexingResponse stopIndexing();

    boolean isIndexingRunning();

    Page indexPage(String url);
}
