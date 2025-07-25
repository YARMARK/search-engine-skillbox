package searchengine.services;

import searchengine.model.Lemma;
import searchengine.model.Page;
import searchengine.model.SearchIndex;
import searchengine.model.Site;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;

public interface LemmaService {

    void saveAllLemmas(Page page);

    Map<String, Integer> collectLemmas(String text);

    ConcurrentMap<String, Set<String>> getLemmaForms();

    int getLemmaFrequency(String lemma);

    List<Page> findAllPagesByLemmas(List<Lemma> lemmas);

    List<Lemma> findAllLemmasByLemma(String s);

    List<Page> findAllPagesByLemmaAndSite(String s, Site site);

    List<SearchIndex> findAllIndicesByPage(Page page);
}
