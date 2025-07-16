package searchengine.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;
import searchengine.model.Page;
import searchengine.model.Site;

import java.util.List;

public interface PageRepository extends JpaRepository<Page, Integer> {

    @Transactional
    void deleteBySiteUrl(String siteUrl);

    @Transactional
    void deleteBySite(Site site);

    @Transactional
    boolean existsPageByPath(String path);

    @Transactional
    void deletePageByPath(String url);

    @Transactional
    Page findByPath(String url);

    @Transactional(readOnly = true)
    Long getCountBySite(Site site);

    @Transactional(readOnly = true)
    int countBySite(Site webSite);

    @Transactional(readOnly = true)
    List<Page> findAllBySite(Site site);
}
