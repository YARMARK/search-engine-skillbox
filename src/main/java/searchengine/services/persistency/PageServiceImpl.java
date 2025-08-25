package searchengine.services.persistency;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import searchengine.model.Lemma;
import searchengine.model.Page;
import searchengine.model.SearchIndex;
import searchengine.model.Site;
import searchengine.repository.PageRepository;
import searchengine.services.LemmaService;
import searchengine.services.PageService;
import searchengine.services.SearchIndexService;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * {@inheritDoc}
 * <p>
 * В этой реализации используется {@link PageRepository} для работы с базой данных,
 * а также класс {@link SearchIndexService} и {@link LemmaService} для работы с индексами и леммами.
 * </p>
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PageServiceImpl implements PageService {

    private final PageRepository pageRepository;

    private final SearchIndexService searchIndexService;

    private final LemmaService lemmaService;

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public boolean existsPageByPath(String path) {
        log.debug("Deleting page by path: {}", path);
        return pageRepository.existsPageByPath(path);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public void deletePage(Page page) {
        pageRepository.delete(page);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public Page findPageByPath(String url) {
        return pageRepository.findByPath(url);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public int countPageBySite(Site site) {
        return pageRepository.countBySite(site);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public void deleteAllPagesBySite(Site site) {
        log.info("Deleting all pages for site: {}", site.getUrl());
        pageRepository.deleteAllPagesBySite(site);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public Page savePage(Page page) {
        log.debug("Saving page: {}", page.getPath());
        return pageRepository.save(page);
    }
    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public List<Page> findAllPagesByLemmaAndSite(String lemma, Site site) {
        Optional<Lemma> byLemmaAndSite = lemmaService.findLemmaByLemmaAndSite(lemma, site);
        if (!byLemmaAndSite.isPresent()) {
            log.warn("Lemma not found: '{}' for site: {}", lemma, site.getUrl());
            return Collections.emptyList();
        }

        List<SearchIndex> indices = searchIndexService.findAllIndexesByLemma(byLemmaAndSite.get());
        if (indices.isEmpty()) {
            log.warn("No indices found for lemma: '{}' on site: {}", lemma, site.getUrl());
            return Collections.emptyList();
        }

        return indices.stream()
                .map(SearchIndex::getPage)
                .distinct()
                .toList();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public List<Page> findAllPagesByLemmas(List<Lemma> lemmas) {
        if (lemmas == null || lemmas.isEmpty()) {
            return Collections.emptyList();
        }

        return searchIndexService.findAllIndexesByLemmas(lemmas).stream()
                .map(SearchIndex::getPage)
                .distinct()
                .toList();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int countAllPages(){
        return (int) pageRepository.count();
    }
}
