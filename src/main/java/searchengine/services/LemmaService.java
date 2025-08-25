package searchengine.services;

import searchengine.model.Lemma;
import searchengine.model.Site;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;

/**
 * Сервис для работы с леммами {@link Lemma}.
 * <p>
 * Предоставляет методы для извлечения лемм из текста, сохранения их в базе данных,
 * поиска по леммам и подсчета частоты появления.
 * </p>
 */
public interface LemmaService {

    /**
     * Сохраняет список лемм в базе данных в батчевом режиме.
     * <p>
     * Если лемма уже существует, увеличивает её частоту.
     * </p>
     *
     * @param batch  список пар "лемма - количество вхождений"
     * @param siteId идентификатор сайта
     */
    void upsertLemmasInBatch(List<Map.Entry<String, Integer>> batch, int siteId);

    /**
     * Извлекает леммы из переданного текста.
     *
     * @param text текст для анализа
     * @return карта, где ключ — лемма, значение — количество вхождений
     */
    Map<String, Integer> collectLemmas(String text);

    /**
     * Возвращает все формы лемм.
     *
     * @return ConcurrentMap, где ключ — лемма, значение — множество её форм
     */
    ConcurrentMap<String, Set<String>> getLemmaForms();

    /**
     * Возвращает частоту появления указанной леммы в базе данных.
     *
     * @param lemma текст леммы
     * @return количество вхождений леммы
     */
    int getLemmaFrequency(String lemma);

    /**
     * Находит все леммы по списку текстов и идентификатору сайта.
     *
     * @param lemmas список текстов лемм
     * @param siteId идентификатор сайта
     * @return список найденных лемм
     */
    List<Lemma> findAllByLemmaInAndSite(List<String> lemmas, int siteId);

    /**
     * Находит все леммы по точному тексту.
     *
     * @param lemma текст леммы
     * @return список найденных лемм
     */
    List<Lemma> findAllByLemma(String lemma);

    /**
     * Находит лемму по тексту и сайту.
     *
     * @param lemma текст леммы
     * @param site  объект сайта
     * @return Optional, содержащий найденную лемму или пустой, если не найдено
     */
    Optional<Lemma> findLemmaByLemmaAndSite(String lemma, Site site);

    /**
     * Удаляет все леммы, связанные с указанным сайтом.
     *
     * @param site объект сайта
     */
    void deleteAllLemmasBySite(Site site);
}
