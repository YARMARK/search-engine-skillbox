package searchengine.services.impl;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import searchengine.model.Lemma;
import searchengine.model.Page;
import searchengine.model.SearchIndex;
import searchengine.model.Site;
import searchengine.morpholgy.LemmaFinder;
import searchengine.repository.LemmaRepository;
import searchengine.repository.SearchIndexRepository;
import searchengine.services.LemmaService;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class LemmaServiceImpl implements LemmaService {

    private final LemmaRepository lemmaRepository;

    private final SearchIndexRepository indexRepository;

    private LemmaFinder lemmaFinder;

    @PostConstruct
    public void init() {
        try {
            lemmaFinder = LemmaFinder.getInstance();
        } catch (IOException e) {
            throw new RuntimeException("Failed to initialize LemmaFinder", e);
        }
    }


    @Override
    public void saveAllLemmas(Page page) {
        List<Lemma> lemmas;
        final int[] frequency = new int[1];
        Map<String, Integer> map = lemmaFinder.collectLemmas(page.getContent());
        for (Map.Entry<String, Integer> entry : map.entrySet()) {
            lemmas = lemmaRepository.findALLByLemma(entry.getKey());
            if (lemmas.size() > 0) {
                lemmas.forEach(l -> {
                    if (l != null && l.getSite().equals(page.getSite())) {
                        frequency[0] = l.getFrequency();
                        frequency[0]++;
                        l.setFrequency(frequency[0]);
                        lemmaRepository.save(l);

                    }
                });
            } else {
                frequency[0] = 1;
                lemmas.add(new Lemma());
                lemmas.get(0).setLemma(entry.getKey());
                lemmas.get(0).setFrequency(frequency[0]);
                lemmas.get(0).setSite(page.getSite());
                lemmaRepository.save(lemmas.get(0));
            }

            SearchIndex index = new SearchIndex();
            index.setLemma(lemmas.get(0));
            index.setPage(page);
            index.setRank(entry.getValue().floatValue());
            indexRepository.save(index);
        }
    }

    @Override
    public List<Lemma> findAllBySite(Site site) {
        return lemmaRepository.findAllBySite(site);
    }
}
