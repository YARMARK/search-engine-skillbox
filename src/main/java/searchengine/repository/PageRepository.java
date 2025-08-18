package searchengine.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;
import searchengine.model.Page;
import searchengine.model.Site;

import java.util.List;

public interface PageRepository extends JpaRepository<Page, Integer> {

    boolean existsPageByPath(String path);

    void deletePageByPath(String url);

    Page findByPath(String url);

    int countBySite(Site site);

    @Transactional(readOnly = true)
    List<Page> findAllBySite(Site site);

    @Query("SELECT p FROM Page p WHERE p.content LIKE %:query% AND p.site = :site")
    List<Page> searchByQueryAndSite(String query, Site site, Pageable pageable);

    @Query("SELECT p FROM Page p WHERE p.content LIKE %:query%")
    List<Page> searchByQuery(String query, Pageable pageable);

    @Modifying
    @Query("DELETE FROM Page p WHERE p.site = :site")
    void deleteAllPagesBySite(Site site);
}
