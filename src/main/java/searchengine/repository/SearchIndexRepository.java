package searchengine.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import searchengine.model.Lemma;
import searchengine.model.Page;
import searchengine.model.SearchIndex;
import searchengine.model.Site;
import searchengine.repository.projection.PageRankSum;

import java.util.Collection;
import java.util.List;

/**
 * Репозиторий для работы с сущностью {@link SearchIndex}.
 *
 * Предоставляет стандартные CRUD операции благодаря {@link JpaRepository},
 * а также дополнительные методы для поиска и удаления индексов по страницам, леммам и сайтам.
 */
public interface SearchIndexRepository extends JpaRepository<SearchIndex, Integer> {

    /**
     * Удаляет все индексы, связанные с указанной страницей.
     *
     * @param page страница, индексы которой нужно удалить
     */
    void deleteByPage(Page page);

    /**
     * Находит все индексы, связанные с указанной леммой.
     *
     * @param lemma лемма
     * @return список индексов
     */
    @Query("SELECT i FROM index_table i WHERE i.lemma = :lemma")
    List<SearchIndex> findAllByLemma(@Param("lemma") Lemma lemma);

    /**
     * Находит все индексы, связанные с любыми из указанных лемм.
     *
     * @param lemmas список лемм
     * @return список индексов
     */
    @Query("SELECT i FROM index_table i WHERE i.lemma IN :lemmas")
    List<SearchIndex> findAllByLemmas(@Param("lemmas") List<Lemma> lemmas);

    /**
     * Удаляет все индексы, связанные со страницами указанного сайта.
     *
     * @param site сайт, индексы которого нужно удалить
     */
    @Modifying
    @Query("DELETE FROM index_table i WHERE i.page.site = :site")
    void deleteAllIndexesBySite(@Param("site") Site site);

    /**
     * Возвращает сумму рангов по страницам для набора страниц и набора лемм.
     * Группирует по page_id.
     *
     */
    @Query("SELECT i.page.id as pageId, SUM(i.rank) as sumRank " +
            "FROM index_table i " +
            "WHERE i.page.id IN :pageIds AND i.lemma IN :lemmas " +
            "GROUP BY i.page.id")
    List<PageRankSum> sumRankByPageForLemmas(@Param("pageIds") Collection<Integer> pageIds,
                                             @Param("lemmas") Collection<Lemma> lemmas);
}
