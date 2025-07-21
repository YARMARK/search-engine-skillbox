package searchengine.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
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
}
