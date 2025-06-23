package searchengine.controllers;

import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import searchengine.dto.response.IndexingResponse;
import searchengine.dto.statistics.StatisticsResponse;
import searchengine.services.IndexingService;
import searchengine.services.StatisticsService;

import java.util.concurrent.atomic.AtomicBoolean;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
@Slf4j
public class ApiController {

    private final StatisticsService statisticsService;

    private final IndexingService indexingService;

    private AtomicBoolean indexingStatus = new AtomicBoolean(false);

    @GetMapping("/statistics")
    public ResponseEntity<StatisticsResponse> statistics() {
        return ResponseEntity.ok(statisticsService.getStatistics());
    }

    @GetMapping("/startIndexing")
    public IndexingResponse startIndexing() {
        IndexingResponse response = new IndexingResponse();
        if (indexingStatus.get()) {
            response.setResult(false);
            response.setError("Индексация уже запущена");
            return response;
        }

        try {
            indexingService.startIndexing();
            indexingStatus.set(true);
            response.setResult(true);
            return response;
        } catch (Exception e) {
            response.setResult(false);
            response.setError("Ошибка при запуске индексации: " + e.getMessage());
            return response;
        }
    }

    @GetMapping("/stopIndexing")
    public IndexingResponse stopIndexing() {
        IndexingResponse response = new IndexingResponse();
        if (!indexingStatus.get()) {
            response.setResult(false);
            response.setError("Индексация не запущена");
            return response;
        }
        try {
            indexingService.stopIndexing();
            indexingStatus.set(false);
            response.setResult(true);
            return response;
        } catch (Exception e) {
            response.setResult(false);
            response.setError("Ошибка при остановке индексации: " + e.getMessage());
            return response;
        }
    }
}
