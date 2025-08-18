package searchengine.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import searchengine.model.Lemma;
import searchengine.model.Page;
import searchengine.model.SearchIndex;
import searchengine.model.Site;

import java.util.List;

public interface SearchIndexRepository extends JpaRepository<SearchIndex, Integer> {

    void deleteByPage(Page page);

    List<SearchIndex> findByPage(Page page);

    @Query("SELECT i FROM index_table i WHERE i.lemma = :lemma")
    List<SearchIndex> findAllByLemma(@Param("lemma") Lemma lemma);

    @Query("SELECT i FROM index_table i WHERE i.lemma IN :lemmas")
    List<SearchIndex> findAllByLemmas(@Param("lemmas") List<Lemma> lemmas);

    @Modifying
    @Query("DELETE FROM index_table i WHERE i.page.site = :site")
    void deleteAllIndexesBySite(@Param("site") Site site);
}
