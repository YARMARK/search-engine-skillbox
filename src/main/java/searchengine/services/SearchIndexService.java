package searchengine.services;

import searchengine.model.Lemma;
import searchengine.model.Page;
import searchengine.model.SearchIndex;
import searchengine.model.Site;
import searchengine.repository.projection.PageRankSum;

import java.util.Collection;
import java.util.List;

/**
 * Сервис для работы с поисковыми индексами {@link SearchIndex}.
 * <p>
 * Обеспечивает операции сохранения, удаления и поиска индексов,
 * связанных с {@link Page}, {@link Lemma} и {@link Site}.
 * </p>
 */
public interface SearchIndexService {

    /**
     * Сохраняет коллекцию поисковых индексов в базе данных.
     *
     * @param indexes коллекция {@link SearchIndex} для сохранения
     */
    void saveAllIndexes(Collection<SearchIndex> indexes);

    /**
     * Удаляет все индексы, связанные с указанной страницей.
     *
     * @param page объект {@link Page}, индексы которого нужно удалить
     */
    void deleteIndexByPage(Page page);

    /**
     * Находит все индексы для конкретной страницы.
     *
     * @param page объект {@link Page}, для которого нужно найти индексы
     * @return список {@link SearchIndex}, связанных с данной страницей
     */
    List<SearchIndex> findIndexesByPage(Page page);

    /**
     * Находит все индексы для конкретной леммы.
     *
     * @param lemma объект {@link Lemma}, для которого нужно найти индексы
     * @return список {@link SearchIndex}, связанных с данной леммой
     */
    List<SearchIndex> findAllIndexesByLemma(Lemma lemma);

    /**
     * Находит все индексы для списка лемм.
     *
     * @param lemmas список объектов {@link Lemma}, для которых нужно найти индексы
     * @return список {@link SearchIndex}, связанных с указанными леммами
     */
    List<SearchIndex> findAllIndexesByLemmas(List<Lemma> lemmas);

    /**
     * Удаляет все индексы, связанные с конкретным сайтом.
     *
     * @param site объект {@link Site}, индексы которого нужно удалить
     */
    void deleteAllIndexesBySite(Site site);

    /**
     * Находит все индексы для конкретной страницы.
     *
     * @param page объект {@link Page}, для которого нужно найти индексы
     * @return список {@link SearchIndex}, связанных с данной страницей
     */
    List<SearchIndex> findAllIndicesByPage(Page page);

    /**
     * Возвращает сумму рангов по страницам для набора страниц и набора лемм.
     */
    List<PageRankSum> sumRankByPageForLemmas(Collection<Integer> pageIds, Collection<Lemma> lemmas);
}
