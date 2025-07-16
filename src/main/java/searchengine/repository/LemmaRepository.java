package searchengine.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import searchengine.model.Lemma;
import searchengine.model.Site;

import java.util.List;

public interface LemmaRepository extends JpaRepository<Lemma, Integer> {

    List<Lemma> findALLByLemma(String lemma);

    List<Lemma> findAllBySite(Site site);

    @Query("SELECT COUNT (l.lemma) FROM Lemma l WHERE l.site = :site")
    Integer countLemmasByWebSite(Site site);

    @Query("SELECT COUNT (l.lemma) FROM Lemma l")
    Integer countAllLemmas();
}
