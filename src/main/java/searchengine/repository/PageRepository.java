package searchengine.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;
import searchengine.model.Page;
import searchengine.model.Site;

public interface PageRepository extends JpaRepository<Page, Integer> {

    @Transactional
    void deleteBySiteUrl(String siteUrl);

    @Transactional
    void deleteBySite(Site site);

    @Transactional
    boolean existsPageByPath(String siteUrl);

    @Transactional
    void deletePageByPath(String url);
}
