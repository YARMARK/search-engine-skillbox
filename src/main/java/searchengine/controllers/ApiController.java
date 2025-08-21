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
import searchengine.dto.serach.SearchResponse;
import searchengine.dto.statistics.StatisticsResponse;
import searchengine.model.Page;
import searchengine.morpholgy.LemmaIndexer;
import searchengine.services.IndexingService;
import searchengine.services.SearchService;
import searchengine.services.StatisticsService;
import searchengine.util.PatternValidationUtil;

/**
 * REST-контроллер для работы с поисковым сервисом, индексированием страниц и статистикой.
 * <p>
 * Предоставляет следующие эндпоинты:
 * <ul>
 *     <li>GET /api/statistics — получение статистики по индексированию сайтов.</li>
 *     <li>GET /api/startIndexing — запуск процесса индексирования всех сайтов из конфигурации.</li>
 *     <li>GET /api/stopIndexing — остановка текущего процесса индексирования.</li>
 *     <li>POST /api/indexPage — индексирование одной конкретной страницы по URL.</li>
 *     <li>GET /api/search — поиск по проиндексированным страницам с возможностью фильтрации по сайту и пагинации.</li>
 * </ul>
 * </p>
 * <p>
 * Используемые сервисы:
 * <ul>
 *     <li>{@link StatisticsService} — получение статистики.</li>
 *     <li>{@link IndexingService} — управление процессом индексирования и индексация отдельных страниц.</li>
 *     <li>{@link LemmaIndexer} — сохранение лемм проиндексированных страниц.</li>
 *     <li>{@link SearchService} — выполнение поисковых запросов.</li>
 * </ul>
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
@Slf4j
public class ApiController {

    private final StatisticsService statisticsService;

    private final IndexingService indexingService;

    private final LemmaIndexer lemmaIndexer;

    private final SearchService searchService;

    /**
     * Получение текущей статистики индексирования.
     *
     * @return {@link ResponseEntity} с объектом {@link StatisticsResponse}.
     */
    @GetMapping("/statistics")
    public ResponseEntity<StatisticsResponse> statistics() {
        return ResponseEntity.ok(statisticsService.getStatistics());
    }

    /**
     * Запуск процесса индексирования всех сайтов из конфигурации.
     *
     * @return {@link ResponseEntity} с объектом {@link IndexingResponse}, содержащим статус запуска.
     */
    @GetMapping("/startIndexing")
    public ResponseEntity<IndexingResponse> startIndexing() {
        return ResponseEntity.ok(indexingService.startIndexing());
    }

    /**
     * Остановка текущего процесса индексирования.
     *
     * @return {@link ResponseEntity} с объектом {@link IndexingResponse}, содержащим статус остановки.
     */
    @GetMapping("/stopIndexing")
    public ResponseEntity<IndexingResponse> stopIndexing() {
        return ResponseEntity.ok(indexingService.stopIndexing());
    }

    /**
     * Индексирование отдельной страницы по URL.
     *
     * @param url URL страницы для индексации.
     * @return {@link ResponseEntity} с объектом {@link IndexingResponse}.
     *         <ul>
     *             <li>400 Bad Request — если URL некорректен.</li>
     *             <li>404 Not Found — если страница не найдена или не была проиндексирована.</li>
     *             <li>200 OK — успешная индексация страницы.</li>
     *         </ul>
     */
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

        lemmaIndexer.saveAllLemmas(page);
        return ResponseEntity.ok().body(new IndexingResponse());
    }

    /**
     * Поиск по проиндексированным страницам.
     *
     * @param query  поисковый запрос.
     * @param site   URL сайта для фильтрации результатов (необязательно).
     * @param offset смещение для пагинации (по умолчанию 0).
     * @param limit  количество результатов на страницу (по умолчанию 10).
     * @return {@link ResponseEntity} с объектом {@link SearchResponse}.
     *         <ul>
     *             <li>400 Bad Request — если поиск не дал результатов.</li>
     *             <li>200 OK — успешный поиск с найденными результатами.</li>
     *         </ul>
     */
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
