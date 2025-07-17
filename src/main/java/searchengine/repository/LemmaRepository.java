package searchengine.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;
import searchengine.model.Lemma;
import searchengine.model.Site;

import java.util.List;

public interface LemmaRepository extends JpaRepository<Lemma, Integer> {

    List<Lemma> findAllByLemma(String lemma);

    @Query("SELECT l FROM Lemma l WHERE l.lemma = :lemma AND l.site = :site")
    Lemma findByLemmaAndSite(@Param("lemma") String lemma, @Param("Site") Site site);

    List<Lemma> findAllBySite(Site site);

    @Query("SELECT COUNT (l.lemma) FROM Lemma l WHERE l.site = :site")
    Integer countLemmasByWebSite(Site site);

    @Query("SELECT COUNT (l.lemma) FROM Lemma l")
    Integer countAllLemmas();

    @Modifying
    @Transactional
    @Query(value = "INSERT INTO lemma (lemma, frequency, site_id) " +
                   "VALUES (:lemma, 1, :webSite) " +
                   "ON DUPLICATE KEY UPDATE frequency = frequency + 1",
            nativeQuery = true)
    Lemma saveOrUpdateLemma(@Param("lemma") String lemma, @Param("webSite") Site site);
}
