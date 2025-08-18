package searchengine.services;

import org.springframework.data.repository.query.Param;
import searchengine.model.Lemma;
import searchengine.model.Page;
import searchengine.model.SearchIndex;
import searchengine.model.Site;

import java.util.Collection;
import java.util.List;

public interface SearchIndexService {

    void saveAllIndexes(Collection<SearchIndex> indexes);

    void deleteIndexByPage(Page page);

    List<SearchIndex> findIndexesByPage(Page page);

    List<SearchIndex> findAllIndexesByLemma(@Param("lemma") Lemma lemma);

    List<SearchIndex> findAllIndexesByLemmas(@Param("lemmas") List<Lemma> lemmas);

    void deleteAllIndexesBySite(@Param("site") Site site);

    List<SearchIndex> findAllIndicesByPage(Page page);
}
