package searchengine.morpholgy;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.CannotAcquireLockException;
import org.springframework.dao.DeadlockLoserDataAccessException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import searchengine.model.Lemma;
import searchengine.model.Page;
import searchengine.model.SearchIndex;
import searchengine.services.LemmaService;
import searchengine.services.SearchIndexService;
import searchengine.manager.SiteScopedLockManager;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

/**
 * Компонент для индексирования лемм и сохранения поисковых индексов.
 * <p>
 * Этот класс отвечает за:
 * <ul>
 *     <li>Сбор лемм из текста страницы.</li>
 *     <li>Сохранение лемм в базу данных партиями.</li>
 *     <li>Создание и сохранение поисковых индексов (SearchIndex) для лемм.</li>
 *     <li>Обеспечение сериализации операций для одного сайта, чтобы минимизировать вероятность дедлоков.</li>
 * </ul>
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class LemmaIndexer {

    private final LemmaService lemmasService;

    private final SearchIndexService searchIndexService;

    private final SiteScopedLockManager siteScopedLockManager;

    /**
     * Сохраняет все леммы из содержимого страницы в базе данных.
     * <p>
     * Леммы обрабатываются партиями по 20 элементов и сохраняются с учетом блокировки по сайту.
     * Это позволяет избежать конфликтов между параллельными транзакциями для одной страницы.
     *
     * @param page страница, из которой собираются леммы
     */
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public void saveAllLemmas(Page page) {
        log.info("Start saving lemmas from pageId={}", page.getId());
        Map<String, Integer> lemmasWithCount = lemmasService.collectLemmas(page.getContent());
        int siteId = page.getSite().getId();

        if (lemmasWithCount.isEmpty()) {
            log.info("No lemmas for saving (siteId={})", siteId);
            return;
        }

        List<Map.Entry<String, Integer>> entries = new ArrayList<>(lemmasWithCount.entrySet());
        entries.sort(Comparator.comparing(Map.Entry::getKey));

        int batchSize = 20;
        int totalBatches = (int) Math.ceil((double) entries.size() / batchSize);

        for (int i = 0; i < entries.size(); i += batchSize) {
            List<Map.Entry<String, Integer>> batch = entries.subList(i, Math.min(i + batchSize, entries.size()));
            try {
                siteScopedLockManager.executeWithLock(siteId, () -> processSingleBatch(batch, page));
                log.debug("{} from {} batches are saved({} lemmas)",
                        (i / batchSize) + 1, totalBatches, batch.size());
            } catch (Exception e) {
                log.error("Failed to process batch for siteId={}, pageId={} after all retries. The transaction will be rolled back.",
                        page.getSite().getId(), page.getId(), e);
                throw e;
            }
        }

        log.info("All batches are saved fro siteId={}", siteId);

    }

    /**
     * Обрабатывает и сохраняет одну партию лемм в базе данных.
     * <p>
     * Метод выполняется в новой транзакции с уровнем изоляции READ_COMMITTED и
     * использует механизм повторных попыток при ошибках блокировок или дедлоков.
     *
     * @param batch список пар <лемма, количество>
     * @param page  страница, к которой относятся леммы
     */
    @Retryable(
            value = {CannotAcquireLockException.class, DeadlockLoserDataAccessException.class},
            maxAttempts = 7,
            backoff = @Backoff(delay = 500, maxDelay = 1500, random = true)
    )
    @Transactional(propagation = Propagation.REQUIRES_NEW, isolation = Isolation.READ_COMMITTED)
    public void processSingleBatch(List<Map.Entry<String, Integer>> batch, Page page) {
        lemmasService.upsertLemmasInBatch(batch, page.getSite().getId());
        saveIndexes(batch, page);
    }

    /**
     * Создает и сохраняет поисковые индексы для заданной партии лемм.
     *
     * @param batch список пар <лемма, количество>
     * @param page  страница, к которой относятся леммы
     */
    private void saveIndexes(List<Map.Entry<String, Integer>> batch, Page page) {
        List<SearchIndex> indexes = createIndexes(batch, page);
        searchIndexService.saveAllIndexes(indexes);
    }

    /**
     * Создает поисковые индексы для заданной партии лемм и страницы.
     *
     * @param batch список пар <лемма, количество>
     * @param page  страница, к которой относятся леммы
     * @return список объектов SearchIndex
     */
    private List<SearchIndex> createIndexes(List<Map.Entry<String, Integer>> batch, Page page) {
        log.debug("Creating search indexes for batch (pageId={}, siteId={}, size={})",
                page.getId(), page.getSite().getId(), batch.size());
        List<Lemma> lemmas = lemmasService.findAllByLemmaInAndSite(batch.stream()
                .map(Map.Entry::getKey)
                .toList(), page.getSite().getId());

        return lemmas.stream()
                .map(lemma -> {
                    int count = batch.stream()
                            .filter(e -> e.getKey().equals(lemma.getLemma()))
                            .map(Map.Entry::getValue)
                            .findFirst()
                            .orElse(0);
                    return createIndex(lemma, page, count);
                })
                .toList();
    }

    /**
     * Создает объект поискового индекса для конкретной леммы и страницы.
     *
     * @param lemma лемма
     * @param page  страница
     * @param count количество вхождений леммы на странице
     * @return объект SearchIndex
     */
    public SearchIndex createIndex(Lemma lemma, Page page, int count) {
        SearchIndex index = new SearchIndex();
        index.setLemma(lemma);
        index.setPage(page);
        index.setRank(count);
        return index;
    }
}
