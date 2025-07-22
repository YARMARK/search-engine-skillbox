package searchengine.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;
import searchengine.model.Lemma;
import searchengine.model.Site;

import java.util.List;
import java.util.Optional;

public interface LemmaRepository extends JpaRepository<Lemma, Integer> {

    List<Lemma> findAllByLemma(String lemma);

    @Query("SELECT l FROM Lemma l WHERE l.lemma = :lemma AND l.site = :site")
    Optional<Lemma> findByLemmaAndSite(@Param("lemma") String lemma, @Param("site") Site site);

    List<Lemma> findAllBySite(Site site);

    @Query("SELECT COUNT (l.lemma) FROM Lemma l WHERE l.site = :site")
    Integer countLemmasByWebSite(Site site);

    @Query("SELECT COUNT (l.lemma) FROM Lemma l")
    Integer countAllLemmas();

    @Transactional
    @Modifying
    @Query(value = """
            INSERT INTO lemma (lemma, frequency, site_id)
            VALUES (:lemma, :count, :siteId)
            ON DUPLICATE KEY UPDATE frequency = frequency + :count
            """, nativeQuery = true)
    void upsertLemma(@Param("lemma") String lemma, @Param("siteId") int siteId, @Param("count") int count);

    @Transactional
    @Modifying
    @Query("DELETE FROM Lemma l WHERE l.site = :site")
    void deleteAllLemmasBySite(Site site);
}
