package searchengine.services.impl;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import searchengine.model.Lemma;
import searchengine.model.Page;
import searchengine.model.SearchIndex;
import searchengine.model.Site;
import searchengine.morpholgy.LemmaFinder;
import searchengine.repository.LemmaRepository;
import searchengine.repository.SearchIndexRepository;
import searchengine.services.LemmaService;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class LemmaServiceImpl implements LemmaService {

    private final LemmaRepository lemmaRepository;

    private final SearchIndexRepository searchIndexRepository;

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
    @Transactional
    public void saveAllLemmas(Page page) {
        Map<String, Integer> lemmasWithCount = lemmaFinder.collectLemmas(page.getContent());
        Site site = page.getSite();

        for (Map.Entry<String, Integer> entry : lemmasWithCount.entrySet()) {
            String lemmaText = entry.getKey();
            int count = entry.getValue();

            // saveOrUpdateLemma инкрементирует frequency, если лемма уже есть
            Lemma lemma = lemmaRepository.saveOrUpdateLemma(lemmaText, site);

            // сохраняем индекс
            SearchIndex index = new SearchIndex();
            index.setLemma(lemma);
            index.setPage(page);
            index.setRank((float) count);
            searchIndexRepository.save(index);
        }
    }

    @Override
    public List<SearchIndex> findAllIndicesByWebPage(Page page) {
        return searchIndexRepository.findByPage(page);
    }

    @Override
    public Map<String, Integer> collectLemmas(String text) {
        return lemmaFinder.collectLemmas(text);
    }

    @Override
    public ConcurrentMap<String, Set<String>> getLemmaForms() {
        return lemmaFinder.getLemmaFormsMap();
    }

    @Override
    public int getLemmaFrequency(String lemma) {
        return lemmaRepository.findAllByLemma(lemma).size();
    }

    @Override
    public Lemma findByLemmaAndSite(String s, Site site) {
        return lemmaRepository.findByLemmaAndSite(s, site);
    }

    @Override
    public List<Lemma> findAllLemmasByLemma(String s) {
        return lemmaRepository.findAllByLemma(s);
    }

    @Override
    public List<Page> findAllPagesByLemmaAndSite(String lemmaText, Site site) {
        Lemma lemma = lemmaRepository.findByLemmaAndSite(lemmaText, site);
        if (lemma == null) {
            log.warn("Lemma not found: '{}' for site: {}", lemmaText, site.getUrl());
            return Collections.emptyList();
        }

        List<SearchIndex> indices = searchIndexRepository.findAllByLemma(lemma);
        if (indices.isEmpty()) {
            log.warn("No indices found for lemma: '{}' on site: {}", lemmaText, site.getUrl());
            return Collections.emptyList();
        }

        return indices.stream()
                .map(SearchIndex::getPage)
                .distinct()
                .collect(Collectors.toList());
    }

    @Override
    public List<Page> findAllPagesByLemmas(List<Lemma> lemmas) {
        if (lemmas == null || lemmas.isEmpty()) {
            return Collections.emptyList();
        }

        return searchIndexRepository.findAllByLemmas(lemmas).stream()
                .map(SearchIndex::getPage)
                .distinct()
                .toList();
    }
}
