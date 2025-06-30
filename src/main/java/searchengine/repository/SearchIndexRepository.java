package searchengine.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;
import searchengine.model.Lemma;
import searchengine.model.Page;
import searchengine.model.SearchIndex;

import java.util.List;

public interface SearchIndexRepository extends JpaRepository<SearchIndex, Integer> {

    SearchIndex findFirstByLemmaAndPage(Lemma lemma, Page page);

    List<SearchIndex> findAllByPage(Page page);

    @Transactional
    void deleteByPage(Page page);
}
