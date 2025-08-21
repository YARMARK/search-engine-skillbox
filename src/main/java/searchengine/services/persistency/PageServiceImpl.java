package searchengine.services.persistency;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
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

@Service
@RequiredArgsConstructor
@Slf4j
public class PageServiceImpl implements PageService {

    private final PageRepository pageRepository;

    private final SearchIndexService searchIndexService;

    private final LemmaService lemmaService;

    @Override
    @Transactional(readOnly = true)
    public boolean existsPageByPath(String path) {
        log.debug("Deleting page by path: {}", path);
        return pageRepository.existsPageByPath(path);
    }

    @Override
    @Transactional
    public void deletePageByPath(String url) {
        pageRepository.deletePageByPath(url);
    }

    @Override
    @Transactional(readOnly = true)
    public Page findPageByPath(String url) {
        return pageRepository.findByPath(url);
    }

    @Override
    @Transactional(readOnly = true)
    public int countPageBySite(Site site) {
        return pageRepository.countBySite(site);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Page> findAllPagesBySite(Site site) {
        return pageRepository.findAllBySite(site);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Page> searchPageByQueryAndSite(String query, Site site, Pageable pageable) {
        return pageRepository.searchByQueryAndSite(query, site, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Page> searchPageByQuery(String query, Pageable pageable) {
        return pageRepository.searchByQuery(query, pageable);
    }

    @Override
    @Transactional
    public void deleteAllPagesBySite(Site site) {
        log.info("Deleting all pages for site: {}", site.getUrl());
        pageRepository.deleteAllPagesBySite(site);
    }

    @Override
    @Transactional
    public Page savePage(Page page) {
        log.debug("Saving page: {}", page.getPath());
        return pageRepository.save(page);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Page> findAllPagesByLemmaAndSite(String lemma, Site site){
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

    public int countAllPages(){
        return (int) pageRepository.count();
    }
}
