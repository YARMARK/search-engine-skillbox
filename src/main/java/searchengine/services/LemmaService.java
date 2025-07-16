package searchengine.services;

import searchengine.model.Lemma;
import searchengine.model.Page;
import searchengine.model.Site;

import java.util.List;

public interface LemmaService {

    void saveAllLemmas(Page page);

    List<Lemma> findAllBySite(Site site);
}
