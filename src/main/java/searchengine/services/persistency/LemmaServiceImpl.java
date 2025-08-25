package searchengine.services.persistency;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
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

/**
 * {@inheritDoc}
 * <p>
 * Обеспечивает извлечение, сохранение, поиск и удаление лемм,
 * а также взаимодействие с базой данных через {@link LemmaRepository} и {@link JdbcTemplate}.
 * </p>
 */
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

    /**
     * Инициализация LemmaFinder после создания бина.
     * <p>
     * Используется аннотация @PostConstruct для выполнения инициализации после инъекции зависимостей.
     * </p>
     *
     * @throws RuntimeException если инициализация LemmaFinder не удалась
     */
    @PostConstruct
    public void init() {
        try {
            lemmaFinder = LemmaFinder.getInstance();
        } catch (IOException e) {
            throw new RuntimeException("Failed to initialize LemmaFinder", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public void upsertLemmasInBatch(List<Map.Entry<String, Integer>> batch, int siteId) {
        jdbcTemplate.batchUpdate(upserLemmaInBatch, batch, batch.size(), (ps, entry) -> {
            ps.setString(1, entry.getKey());
            ps.setInt(2, entry.getValue());
            ps.setInt(3, siteId);
        });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Map<String, Integer> collectLemmas(String text) {
        return lemmaFinder.collectLemmas(text);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ConcurrentMap<String, Set<String>> getLemmaForms() {
        return lemmaFinder.getLemmaFormsMap();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public int getLemmaFrequency(String lemma) {
        return lemmaRepository.findAllByLemma(lemma).size();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public List<Lemma> findAllByLemmaInAndSite(List<String> lemmas, int siteId) {
        return lemmaRepository.findAllByLemmaInAndSite(lemmas, siteId);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public Optional<Lemma> findLemmaByLemmaAndSite(String lemmaText, Site site) {
        return lemmaRepository.findByLemmaAndSite(lemmaText, site);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public void deleteAllLemmasBySite(Site site) {
        lemmaRepository.deleteAllLemmasBySite(site);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public List<Lemma> findAllByLemma(String lemma) {
        return lemmaRepository.findAllByLemma(lemma);
    }
}
