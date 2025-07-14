package searchengine.controllers;

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
import searchengine.dto.statistics.StatisticsResponse;
import searchengine.model.Page;
import searchengine.services.IndexingService;
import searchengine.services.LemmaService;
import searchengine.services.StatisticsService;
import searchengine.util.PatternValidationUtil;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
@Slf4j
public class ApiController {

    private final StatisticsService statisticsService;

    private final IndexingService indexingService;

    private final LemmaService lemmaService;

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

        lemmaService.saveAllLemmas(page);
        return ResponseEntity.ok().body(new IndexingResponse());
    }
}
