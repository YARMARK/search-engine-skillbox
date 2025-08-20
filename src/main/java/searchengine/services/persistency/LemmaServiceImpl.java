package searchengine.services.persistency;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.exception.LockAcquisitionException;
import org.springframework.dao.CannotAcquireLockException;
import org.springframework.dao.DeadlockLoserDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import searchengine.model.Lemma;
import searchengine.model.Site;
import searchengine.morpholgy.LemmaFinder;
import searchengine.repository.LemmaRepository;
import searchengine.services.LemmaService;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;

@Service
@RequiredArgsConstructor
@Slf4j
public class LemmaServiceImpl implements LemmaService {

    private final LemmaRepository lemmaRepository;

    private LemmaFinder lemmaFinder;

    private final JdbcTemplate jdbcTemplate;

    private String upserLemmaInBatch = """
                INSERT INTO lemma (lemma, frequency, site_id)
                VALUES (?, ?, ?)
                ON DUPLICATE KEY UPDATE frequency = frequency + VALUES(frequency)
            """;

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
    public void upsertLemmasInBatch(List<Map.Entry<String, Integer>> batch, int siteId) {

        jdbcTemplate.batchUpdate(upserLemmaInBatch, batch, batch.size(), (ps, entry) -> {
            ps.setString(1, entry.getKey());   // lemma
            ps.setInt(2, entry.getValue());    // frequency
            ps.setInt(3, siteId);              // site_id
        });
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
    @Transactional(readOnly = true)
    public int getLemmaFrequency(String lemma) {
        return lemmaRepository.findAllByLemma(lemma).size();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Lemma> findAllLemmasByLemma(String s) {
        return lemmaRepository.findAllByLemma(s);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Lemma> findAllByLemmaInAndSite(List<String> lemmas, int siteId) {
        return lemmaRepository.findAllByLemmaInAndSite(lemmas, siteId);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Lemma> findLemmaByLemmaAndSite(String lemmaText, Site site) {
        return lemmaRepository.findByLemmaAndSite(lemmaText, site);
    }

    @Override
    @Transactional(readOnly = true)
    public Integer countLemmasBySite(Site site) {
        return lemmaRepository.countLemmasBySite(site);
    }

    @Override
    @Transactional(readOnly = true)
    public Integer countAllLemmas() {
        return lemmaRepository.countAllLemmas();
    }

    @Override
    @Transactional
    public void deleteAllLemmasBySite(Site site) {
        lemmaRepository.deleteAllLemmasBySite(site);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Lemma> findAllByLemma(String lemma) {
        return lemmaRepository.findAllByLemma(lemma);
    }

//    @Override
//    public void saveAllLemmas(Page page) {
//        Map<String, Integer> lemmasWithCount = lemmaFinder.collectLemmas(page.getContent());
//        Site site = page.getSite();
//
//        for (Map.Entry<String, Integer> entry : lemmasWithCount.entrySet()) {
//            // TODO мб вынести вот это в одну транзацкию
//            String lemmaText = entry.getKey();
//            int count = entry.getValue();
//
//            lemmaRepository.upsertLemma(lemmaText, site.getId(), count);
//
//            Optional<Lemma> optionalLemma = lemmaRepository.findByLemmaAndSite(lemmaText, site);
//
//            if (!optionalLemma.isPresent()) {
//                log.error("Lemma '" + lemmaText + "' not found");
//                continue;
//            }
//
//            createIndex(optionalLemma.get(), page, count);
//        }
//    }
//
//    public SearchIndex createIndex(Lemma lemma, Page page, int count) {
//        SearchIndex index = new SearchIndex();
//        index.setLemma(lemma);
//        index.setPage(page);
//        index.setRank(count);
//        return searchIndexRepository.save(index);
//    }
}
