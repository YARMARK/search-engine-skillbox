package searchengine.facade;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.CannotAcquireLockException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.UnexpectedRollbackException;
import searchengine.model.Lemma;
import searchengine.model.Page;
import searchengine.model.SearchIndex;
import searchengine.model.Site;
import searchengine.services.LemmaService;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;

@Component
@RequiredArgsConstructor
@Slf4j
public class LemmaFacade {

    private final LemmaService lemmaService;

    private static final int MAX_RETRIES = 5;

    public void saveAllLemmas(Page page) {
        int attempts = 0;
        while (attempts < MAX_RETRIES) {
            try {
                lemmaService.saveAllLemmas(page);
                return;
            } catch (CannotAcquireLockException | UnexpectedRollbackException e) {
                attempts++;
                log.warn("Retrying saveAllLemmas (attempt {}/{}), cause: {}", attempts, MAX_RETRIES, e.getMessage());
                if (attempts >= MAX_RETRIES) throw e;
                try {
                    Thread.sleep(100); // небольшая пауза
                } catch (InterruptedException ignored) {
                    Thread.currentThread().interrupt();
                }
            }
        }
    }

    public List<SearchIndex> findAllIndicesByPage(Page page) {
        return lemmaService.findAllIndicesByPage(page);
    }

    public Map<String, Integer> collectLemmas(String text) {
        return lemmaService.collectLemmas(text);
    }

    public ConcurrentMap<String, Set<String>> getLemmaForms() {
        return lemmaService.getLemmaForms();
    }

    public int getLemmaFrequency(String lemma) {
        return lemmaService.getLemmaFrequency(lemma);
    }

    public List<Lemma> findAllLemmasByLemma(String s) {
        return lemmaService.findAllLemmasByLemma(s);
    }

    public List<Page> findAllPagesByLemmaAndSite(String lemmaText, Site site) {
        return lemmaService.findAllPagesByLemmaAndSite(lemmaText, site);
    }

    public List<Page> findAllPagesByLemmas(List<Lemma> lemmas) {
       return lemmaService.findAllPagesByLemmas(lemmas);
    }
}
