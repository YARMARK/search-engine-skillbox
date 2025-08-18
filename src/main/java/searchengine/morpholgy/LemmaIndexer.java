package searchengine.morpholgy;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import searchengine.model.Lemma;
import searchengine.model.Page;
import searchengine.model.SearchIndex;
import searchengine.services.LemmaService;
import searchengine.services.SearchIndexService;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class LemmaIndexer {

    private final LemmaService lemmasService;

    private final SearchIndexService searchIndexService;

    public void saveAllLemmas(Page page) {
        Map<String, Integer> lemmasWithCount = lemmasService.collectLemmas(page.getContent());
        int siteId = page.getSite().getId();

        if (lemmasWithCount.isEmpty()) {
            log.info("No lemmas for saving (siteId={})", siteId);
            return;
        }

        List<Map.Entry<String, Integer>> entries = new ArrayList<>(lemmasWithCount.entrySet());

        int batchSize = 30;
        int totalBatches = (int) Math.ceil((double) entries.size() / batchSize);

        for (int i = 0; i < entries.size(); i += batchSize) {
            List<Map.Entry<String, Integer>> batch = entries.subList(i, Math.min(i + batchSize, entries.size()));

            saveBatch(batch, siteId);

            log.info("{} from {} bathes are saved({} lemmas)",
                    (i / batchSize) + 1, totalBatches, batch.size());

            saveIndexes(batch, page);
        }

        log.info("All batches are saved fro siteId={}", siteId);

    }

    private void saveBatch(List<Map.Entry<String, Integer>> batch, int siteId) {
        String values = batch.stream()
                .map(entry -> "('" + entry.getKey().replace("'", "''") + "', "
                              + entry.getValue() + ", " + siteId + ")")
                .collect(Collectors.joining(","));

        lemmasService.upsertLemmasInBatch(values);
    }

    private void saveIndexes(List<Map.Entry<String, Integer>> batch, Page page){
        List<SearchIndex> indexes = createIndexes(batch, page);
        searchIndexService.saveAllIndexes(indexes);
    }

    private List<SearchIndex> createIndexes(List<Map.Entry<String, Integer>> batch, Page page) {
        log.info("Crete searchIndexes for batch");
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

    public SearchIndex createIndex(Lemma lemma, Page page, int count) {
        SearchIndex index = new SearchIndex();
        index.setLemma(lemma);
        index.setPage(page);
        index.setRank(count);
        return index;
    }
}
