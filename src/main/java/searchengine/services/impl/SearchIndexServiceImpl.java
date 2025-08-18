package searchengine.services.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import searchengine.model.Lemma;
import searchengine.model.Page;
import searchengine.model.SearchIndex;
import searchengine.model.Site;
import searchengine.repository.SearchIndexRepository;
import searchengine.services.SearchIndexService;

import java.util.Collection;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class SearchIndexServiceImpl implements SearchIndexService {

    private final SearchIndexRepository searchIndexRepository;

    @Override
    @Transactional
    public void saveAllIndexes(Collection<SearchIndex> indexes) {
        log.info("Saving indexes batch");
        searchIndexRepository.saveAll(indexes);
    }

    @Override
    @Transactional
    public void deleteIndexByPage(Page page) {
        searchIndexRepository.deleteByPage(page);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SearchIndex> findIndexesByPage(Page page) {
        return searchIndexRepository.findByPage(page);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SearchIndex> findAllIndexesByLemma(Lemma lemma) {
        return searchIndexRepository.findAllByLemma(lemma);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SearchIndex> findAllIndexesByLemmas(List<Lemma> lemmas) {
        return searchIndexRepository.findAllByLemmas(lemmas);
    }

    @Override
    @Transactional
    public void deleteAllIndexesBySite(Site site) {
        searchIndexRepository.deleteAllIndexesBySite(site);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SearchIndex> findAllIndicesByPage(Page page) {
        return searchIndexRepository.findByPage(page);
    }
}
