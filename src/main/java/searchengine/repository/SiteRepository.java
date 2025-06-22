package searchengine.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;
import searchengine.model.Site;
import searchengine.model.SiteStatus;

import java.util.List;

public interface SiteRepository extends JpaRepository<Site, Integer> {

    Site findByUrl(String url);

    List<Site> findByStatus(SiteStatus status);

    @Transactional
    void deleteByUrl(String url);
}
