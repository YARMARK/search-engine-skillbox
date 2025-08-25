package searchengine.services;

import searchengine.dto.response.IndexingResponse;
import searchengine.model.Page;

/**
 * Сервис для управления процессом индексации сайтов.
 * <p>
 * Обеспечивает операции запуска, обработки и завершения индексации сайтов и страниц.
 * </p>
 */
public interface IndexingService {

    /**
     * Запускает процесс индексации всех уникальных сайтов из списка.
     *
     * @return {@link IndexingResponse} с информацией о начале индексации
     */
    IndexingResponse startIndexing();

    /**
     * Останавливает процесс индексирования всех сайтов.
     *
     * @return Объект IndexingResponse с результатом операции.
     */
    IndexingResponse stopIndexing();

    /**
     * Проверяет, запущено ли индексирование хотя бы одного сайта.
     *
     * @return true или false
     */
    boolean isIndexingRunning();

    /**
     * Индексирует страницу по указанному URL.
     *
     * @param pageUrl URL страницы для индексации
     * @return объект {@link Page}
     *
     */
    Page indexPage(String pageUrl);
}
