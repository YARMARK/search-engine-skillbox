package searchengine.services;

import org.springframework.data.domain.Pageable;
import searchengine.model.Lemma;
import searchengine.model.Page;
import searchengine.model.Site;

import java.util.List;

public interface PageService {

    boolean existsPageByPath(String path);

    void deletePageByPath(String url);

    Page findPageByPath(String url);

    int countPageBySite(Site site);

    int countAllPages();

    List<Page> findAllPagesBySite(Site site);

    List<Page> searchPageByQueryAndSite(String query, Site site, Pageable pageable);

    List<Page> searchPageByQuery(String query, Pageable pageable);

    void deleteAllPagesBySite(Site site);

    Page savePage(Page page);

    List<Page> findAllPagesByLemmaAndSite(String lemma, Site site);

    List<Page> findAllPagesByLemmas(List<Lemma> lemmas);
}
