package searchengine.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import searchengine.model.Site;
import searchengine.model.SiteStatus;

import java.util.List;

public interface SiteRepository extends JpaRepository<Site, Integer> {

    Site findByUrl(String url);

    List<Site> findByStatus(SiteStatus status);

    void deleteByUrl(String url);
}
