package searchengine.services;

import searchengine.model.Lemma;
import searchengine.model.Site;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;

public interface LemmaService {

//    void saveAllLemmas(Page page);

    void upsertLemmasInBatch(String batch);

    Map<String, Integer> collectLemmas(String text);

    ConcurrentMap<String, Set<String>> getLemmaForms();

    int getLemmaFrequency(String lemma);

    List<Lemma> findAllLemmasByLemma(String s);

    List<Lemma> findAllByLemmaInAndSite(List<String> lemmas, int siteId);

    List<Lemma> findAllByLemma(String lemma);

    Optional<Lemma> findLemmaByLemmaAndSite(String lemma, Site site);

    Integer countLemmasBySite(Site site);

    Integer countAllLemmas();

    void deleteAllLemmasBySite(Site site);
}
