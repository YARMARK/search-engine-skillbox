package searchengine.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import searchengine.model.Lemma;
import searchengine.model.Site;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface LemmaRepository extends JpaRepository<Lemma, Integer> {

    List<Lemma> findAllByLemma(String lemma);

    @Query("SELECT l FROM Lemma l WHERE l.lemma = :lemma AND l.site = :site")
    Optional<Lemma> findByLemmaAndSite(@Param("lemma") String lemma, @Param("site") Site site);

    @Query(value = """
            SELECT * FROM lemma
            WHERE lemma IN (:lemmas) AND site_id = :siteId
            """, nativeQuery = true)
    List<Lemma> findAllByLemmaInAndSite(@Param("lemmas") Collection<String> lemmas,
                                        @Param("siteId") int siteId);

    @Query("SELECT COUNT (l.lemma) FROM Lemma l WHERE l.site = :site")
    Integer countLemmasBySite(Site site);

    @Query("SELECT COUNT (l.lemma) FROM Lemma l")
    Integer countAllLemmas();

//    @Modifying
//    @Query(value = """
//            INSERT INTO lemma (lemma, frequency, site_id)
//            VALUES :values
//            ON DUPLICATE KEY UPDATE frequency = frequency + VALUES(frequency)
//            """, nativeQuery = true)
//    void upsertLemma(@Param("values") String values);

    @Modifying
    @Query("DELETE FROM Lemma l WHERE l.site = :site")
    void deleteAllLemmasBySite(Site site);
}
