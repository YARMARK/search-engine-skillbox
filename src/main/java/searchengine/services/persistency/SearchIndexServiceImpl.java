package searchengine.services.persistency;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import searchengine.model.Lemma;
import searchengine.model.Page;
import searchengine.model.SearchIndex;
import searchengine.model.Site;
import searchengine.repository.SearchIndexRepository;
import searchengine.repository.projection.PageRankSum;
import searchengine.services.SearchIndexService;

import java.util.Collection;
import java.util.List;

/**
 * Реализация {@link SearchIndexService} для работы с поисковыми индексами.
 * <p>
 * Обеспечивает сохранение, удаление и поиск индексов,
 * а также взаимодействие с базой данных через {@link SearchIndexRepository}.
 * </p>
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SearchIndexServiceImpl implements SearchIndexService {

    private final SearchIndexRepository searchIndexRepository;

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public void saveAllIndexes(Collection<SearchIndex> indexes) {
        log.info("Saving {} indexes", indexes.size());
        searchIndexRepository.saveAll(indexes);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public void deleteIndexByPage(Page page) {
        log.debug("Deleting indexes for page: {}", page.getPath());
        searchIndexRepository.deleteByPage(page);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public List<SearchIndex> findIndexesByPage(Page page) {
        return searchIndexRepository.findByPage(page);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public List<SearchIndex> findAllIndexesByLemma(Lemma lemma) {
        return searchIndexRepository.findAllByLemma(lemma);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public List<SearchIndex> findAllIndexesByLemmas(List<Lemma> lemmas) {
        return searchIndexRepository.findAllByLemmas(lemmas);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public void deleteAllIndexesBySite(Site site) {
        log.info("Deleting all indexes for site: {}", site.getUrl());
        searchIndexRepository.deleteAllIndexesBySite(site);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public List<SearchIndex> findAllIndicesByPage(Page page) {
        return searchIndexRepository.findByPage(page);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public List<PageRankSum> sumRankByPageForLemmas(Collection<Integer> pageIds, Collection<Lemma> lemmas) {
        return searchIndexRepository.sumRankByPageForLemmas(pageIds, lemmas);
    }
}
