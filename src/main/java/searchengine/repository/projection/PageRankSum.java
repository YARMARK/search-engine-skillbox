package searchengine.repository.projection;

/**
 * Проекция для получения суммы рангов лемм на странице.
 * Используется в запросах репозитория для оптимизации
 * и возврата только необходимых данных.
 */
public interface PageRankSum {

    /**
     * Идентификатор страницы.
     *
     * @return уникальный ID страницы
     */
    Integer getPageId();

    /**
     * Суммарный ранг всех лемм на странице.
     *
     * @return сумма значений рангов
     */
    Float getSumRank();
}
