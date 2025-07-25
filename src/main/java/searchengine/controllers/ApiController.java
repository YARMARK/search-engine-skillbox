package searchengine.controllers;

import searchengine.facade.LemmaFacade;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import searchengine.dto.response.IndexingResponse;
import searchengine.dto.serach.SearchResponse;
import searchengine.dto.statistics.StatisticsResponse;
import searchengine.model.Page;
import searchengine.services.IndexingService;
import searchengine.services.SearchService;
import searchengine.services.StatisticsService;
import searchengine.util.PatternValidationUtil;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
@Slf4j
public class ApiController {

    private final StatisticsService statisticsService;

    private final IndexingService indexingService;

    private final LemmaFacade lemmaFacade;

    private final SearchService searchService;

    @GetMapping("/statistics")
    public ResponseEntity<StatisticsResponse> statistics() {
        return ResponseEntity.ok(statisticsService.getStatistics());
    }

    @GetMapping("/startIndexing")
    public ResponseEntity<IndexingResponse> startIndexing() {
        return ResponseEntity.ok(indexingService.startIndexing());
    }

    @GetMapping("/stopIndexing")
    public ResponseEntity<IndexingResponse> stopIndexing() {
        return ResponseEntity.ok(indexingService.stopIndexing());
    }

    @PostMapping("/indexPage")
    public ResponseEntity<IndexingResponse> indexPage(@RequestParam(name = "url", required = false) String url) {
        if (!PatternValidationUtil.isValidUrl(url)) {
            return ResponseEntity.badRequest().body(new IndexingResponse("Неверный URL, введите корректный URL"));
        }
        Page page = indexingService.indexPage(url);

        if (page == null) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(new IndexingResponse("Страница не найдена или не была проиндексирована"));
        }

        lemmaFacade.saveAllLemmas(page);
        return ResponseEntity.ok().body(new IndexingResponse());
    }

    @GetMapping("/search")
    public ResponseEntity<SearchResponse> search(@RequestParam(value = "query") String query,
                                                 @RequestParam(value = "site", required = false) String site,
                                                 @RequestParam(value = "offset", defaultValue = "0") Integer offset,
                                                 @RequestParam(value = "limit", defaultValue = "10") Integer limit) {
        log.info("Search request, query {}, site {}, offset {}, limit {}", query, site, offset, limit);
        SearchResponse response = searchService.search(query.trim(), site, offset, limit);
        if (!response.isResult()) {
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }
        return ResponseEntity.ok(response);
    }
}
