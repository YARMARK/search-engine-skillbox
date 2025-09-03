package searchengine.services.indexing;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Service;
import searchengine.config.SearchConfig;
import searchengine.dto.serach.SearchDto;
import searchengine.dto.serach.SearchResponse;
import searchengine.model.Lemma;
import searchengine.model.Page;
import searchengine.model.Site;
import searchengine.model.SiteStatus;
import searchengine.services.LemmaService;
import searchengine.services.PageService;
import searchengine.services.SearchIndexService;
import searchengine.services.SearchService;
import searchengine.services.SiteService;
import searchengine.services.SnippetService;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Реализация поискового сервиса.
 * Отвечает за валидацию запроса, извлечение и фильтрацию лемм,
 * поиск релевантных страниц, вычисление релевантности
 * и формирование итоговых результатов поиска.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class SearchServiceImpl implements SearchService {

    private final LemmaService lemmaService;

    private final SiteService siteService;

    private final SearchIndexService searchIndexService;

    private final PageService pageService;

    private final SearchConfig searchConfig;

    private final SnippetService snippetService;

    /**
     * Выполняет полный цикл обработки поискового запроса:
     * <ul>
     *     <li>Валидация входных параметров</li>
     *     <li>Извлечение и фильтрация лемм</li>
     *     <li>Поиск кандидатов страниц</li>
     *     <li>Вычисление релевантности</li>
     *     <li>Формирование DTO с результатами</li>
     * </ul>
     *
     * @param query  текст запроса
     * @param site   URL сайта (если указан, поиск ограничивается этим сайтом)
     * @param offset смещение результатов (пагинация)
     * @param limit  максимальное количество результатов
     * @return {@link SearchResponse} с найденными результатами или ошибкой
     */
    @Override
    public SearchResponse search(String query, String site, Integer offset, Integer limit) {
        // Валидация и нормализация входных параметров
        SearchResponse validationResult = validateRequest(query, site);
        if (!validationResult.isResult()) {
            return validationResult;
        }

        // Извлечение и фильтрация лемм
        List<String> lemmas = extractAndFilterLemmas(query.trim());
        if (!hasValidLemmas(lemmas)) {
            return createErrorResponse("Все леммы исключены из запроса");
        }

        // Поиск релевантных страниц
        List<Page> pages = findCandidatePages(lemmas, site != null ? site.trim() : null);
        if (!hasSearchResults(pages)) {
            return createEmptyResponse();
        }

        // Расчет релевантности и создание DTO
        List<SearchDto> searchResults = scoreAndBuildDtos(pages, query.trim(), lemmas, limit, offset);
        return createSuccessResponse(searchResults, pages.size());
    }

    /**
     * Валидирует поисковый запрос и проверяет наличие проиндексированных сайтов.
     *
     * @param query  поисковый запрос
     * @param site   URL сайта (может быть null)
     * @return {@link SearchResponse} с ошибкой при неуспехе либо пустой успешный ответ
     */
    private SearchResponse validateRequest(String query, String site) {
        if (query == null || query.trim().isEmpty()) {
            return createErrorResponse("Задан пустой поисковый запрос");
        }

        // Если указан конкретный сайт, проверяем только его статус
        if (site != null) {
            if (!isSiteIndexed(site)) {
                return createErrorResponse("Cайт не найден или еще не проиндексирован");
            }
        } else {
            // Если сайт не указан, проверяем что есть хотя бы один проиндексированный сайт
            List<Site> indexedSites = siteService.findAllSites().stream()
                    .filter(s -> s.getStatus() == SiteStatus.INDEXED)
                    .toList();
            
            if (indexedSites.isEmpty()) {
                return createErrorResponse("Нет проиндексированных сайтов для поиска");
            }
        }

        return new SearchResponse(); // Успешная валидация
    }

    /**
     * Извлекает леммы из запроса и отфильтровывает слишком частотные.
     *
     * @param query нормализованный текст запроса
     * @return список лемм
     */
    private List<String> extractAndFilterLemmas(String query) {
        List<String> lemmas = extractLemmas(query);
        if (lemmas.isEmpty()) {
            return lemmas; // Возвращаем пустой список, если не удалось извлечь леммы
        }
        return filterLemmas(lemmas);
    }

    /**
     * Ищет кандидатов страниц по заданным леммам.
     *
     * @param lemmas  список лемм
     * @param siteUrl URL сайта (если null — поиск по всем сайтам)
     * @return список страниц-кандидатов
     */
    private List<Page> findCandidatePages(List<String> lemmas, String siteUrl) {
        return findRelevantPages(lemmas, siteUrl);
    }

    private boolean hasSearchResults(List<Page> pages) {
        return !pages.isEmpty();
    }

    private boolean hasValidLemmas(List<String> lemmas) {
        return !lemmas.isEmpty();
    }

    /**
     * Вычисляет релевантность найденных страниц и строит список DTO.
     *
     * @param pages   найденные страницы
     * @param query   исходный поисковый запрос
     * @param lemmas  список лемм
     * @param limit   лимит количества результатов
     * @param offset  смещение для пагинации
     * @return список объектов {@link SearchDto} с данными результатов
     */
    private List<SearchDto> scoreAndBuildDtos(List<Page> pages, String query, List<String> lemmas, Integer limit, Integer offset) {
        // Нормализация параметров пагинации
        int normalizedOffset = Math.max(0, offset);
        int normalizedLimit = Math.max(1, limit);
        
        // Создаем локальный набор словоформ для текущего запроса
        Set<String> queryForms = createQueryFormsSet(lemmas);
        
        return calculateRelevanceAndCreateDtos(pages, query, lemmas, normalizedLimit, normalizedOffset, queryForms);
    }

    private Set<String> createQueryFormsSet(List<String> lemmas) {
        Set<String> queryForms = new HashSet<>();
        for (String lemma : lemmas) {
            Set<String> forms = lemmaService.getLemmaForms().get(lemma.toLowerCase());
            if (forms != null) {
                queryForms.addAll(forms);
            }
        }
        return queryForms;
    }

    /**
     * Извлекает все леммы из строки запроса.
     *
     * @param query поисковый запрос
     * @return список лемм
     */
    private List<String> extractLemmas(String query) {
        Map<String, Integer> lemmaMap = lemmaService.collectLemmas(query);
        return new ArrayList<>(lemmaMap.keySet());
    }

    /**
     * Фильтрует леммы, которые встречаются слишком часто.
     *
     * @param lemmas список всех извлечённых лемм
     * @return отфильтрованный список лемм
     */
    private List<String> filterLemmas(List<String> lemmas) {
        double threshold = searchConfig.getFrequencyThreshold();
        int totalPages = pageService.countAllPages();

        return lemmas.stream()
                .filter(lemma -> {
                    int frequency = lemmaService.getLemmaFrequency(lemma);
                    return frequency < totalPages * threshold;
                })
                .sorted(Comparator.comparingInt(lemmaService::getLemmaFrequency))
                .collect(Collectors.toList());
    }
    
    private List<Page> findRelevantPages(List<String> lemmas, String siteUrl) {
        Set<Page> result = new HashSet<>();

        if (siteUrl == null) {
            log.debug("Searching across all indexed sites");
            // Получаем только проиндексированные сайты
            List<Site> indexedSites = siteService.findAllSites().stream()
                    .filter(s -> s.getStatus() == SiteStatus.INDEXED)
                    .toList();
            for (String lemma : lemmas) {
                Set<Page> pagesWithLemma = new HashSet<>();
                // Ищем страницы только на проиндексированных сайтах
                for (Site site : indexedSites) {
                    pagesWithLemma.addAll(pageService.findAllPagesByLemmaAndSite(lemma, site));
                }

                if (result.isEmpty()) {
                    result.addAll(pagesWithLemma);
                } else {
                    result.retainAll(pagesWithLemma);
                }
                if (result.isEmpty()) {
                    break;
                }
            }
        } else {
            log.debug("Searching on site: {}", siteUrl);
            Site indexedSite = siteService.findSiteByUrl(siteUrl);            if (indexedSite != null && indexedSite.getStatus() == SiteStatus.INDEXED) {
                for (String lemma : lemmas) {
                    Set<Page> pagesWithLemma = new HashSet<>(pageService.findAllPagesByLemmaAndSite(lemma, indexedSite));
                    if (result.isEmpty()) {
                        result.addAll(pagesWithLemma);
                    } else {
                        result.retainAll(pagesWithLemma);
                    }
                    if (result.isEmpty()) {
                        break;
                    }
                }
            } else {
                log.warn("Site not found or not indexed: {}", siteUrl);
            }
        }

        return new ArrayList<>(result);
    }

    /**
     * Выполняет окончательный расчёт релевантности и формирует DTO результатов.
     *
     * @param pages    список найденных страниц
     * @param query    исходный запрос
     * @param lemmas   список лемм
     * @param limit    ограничение результатов
     * @param offset   смещение
     * @param querySet множество словоформ для лемм
     * @return список объектов {@link SearchDto}
     */
    private List<SearchDto> calculateRelevanceAndCreateDtos(List<Page> pages, String query, List<String> lemmas, int limit, int offset, Set<String> querySet) {
        Map<Page, Float> relevancesMap = calculateAbsoluteRelevances(pages, lemmas);
        float maxAbsoluteRelevance = relevancesMap.values().stream()
                .max(Float::compare)
                .orElse(0.0f);

        List<SearchDto> searchResults = new ArrayList<>();
        for (Map.Entry<Page, Float> entry : relevancesMap.entrySet()) {
            Page page = entry.getKey();
            float absoluteRelevance = entry.getValue();

            // Parse HTML once per page
            Document doc = Jsoup.parse(page.getContent());
            String title = snippetService.extractTitle(doc);
            String bodyText = doc.text();

            SearchDto searchDto = SearchDto.builder()
                    .site(page.getSite().getUrl())
                    .siteName(page.getSite().getName())
                    .uri(page.getPath())
                    .title(title)
                    .snippet(snippetService.generateSnippet(bodyText, query, querySet))
                    .relevance(maxAbsoluteRelevance == 0.0f ? 0.0f : absoluteRelevance / maxAbsoluteRelevance)
                    .build();
            searchResults.add(searchDto);
        }

        searchResults.sort((dto1, dto2) -> Float.compare(dto2.getRelevance(), dto1.getRelevance()));

        int start = Math.min(offset, searchResults.size());
        int end = Math.min(offset + limit, searchResults.size());
        return searchResults.subList(start, end);
    }

    private boolean isSiteIndexed(String siteUrl) {
        Site site = siteService.findSiteByUrl(siteUrl);
        return site != null && site.getStatus() == SiteStatus.INDEXED;
    }

    private SearchResponse createErrorResponse(String error) {
        SearchResponse response = new SearchResponse();
        response.setResult(false);
        response.setError(error);
        return response;
    }

    private SearchResponse createEmptyResponse() {
        SearchResponse response = new SearchResponse();
        response.setResult(true);
        response.setCount(0);
        response.setData(Collections.emptyList());
        return response;
    }

    private SearchResponse createSuccessResponse(List<SearchDto> searchResults, int size) {
        SearchResponse response = new SearchResponse();
        response.setResult(true);
        response.setCount(size);
        response.setData(searchResults);
        return response;
    }

    /**
     * Рассчитывает абсолютную релевантность для страниц по набору лемм.
     *
     * @param pages  список страниц
     * @param lemmas список лемм
     * @return отображение страниц и их абсолютной релевантности
     */
    private Map<Page, Float> calculateAbsoluteRelevances(List<Page> pages, List<String> lemmas) {
        Map<Page, Float> relevancesMap = new HashMap<>();
        if (pages.isEmpty()) {
            return relevancesMap;
        }

        List<Lemma> lemmaEntities = new ArrayList<>();
        for (String l : lemmas) {
            lemmaEntities.addAll(lemmaService.findAllByLemma(l));
        }

        Map<Integer, Page> pageById = pages.stream().collect(Collectors.toMap(Page::getId, p -> p));
        List<Integer> pageIds = new ArrayList<>(pageById.keySet());

        if (!lemmaEntities.isEmpty() && !pageIds.isEmpty()) {
            searchengine.repository.projection.PageRankSum[] sums = searchIndexService
                    .sumRankByPageForLemmas(pageIds, lemmaEntities)
                    .toArray(new searchengine.repository.projection.PageRankSum[0]);
            for (searchengine.repository.projection.PageRankSum prs : sums) {
                Page page = pageById.get(prs.getPageId());
                if (page != null) {
                    relevancesMap.put(page, prs.getSumRank());
                }
            }
        }

        for (Page page : pages) {
            relevancesMap.putIfAbsent(page, 0.0f);
        }

        return relevancesMap;
    }
}
