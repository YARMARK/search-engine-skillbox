package searchengine.services.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.stereotype.Service;
import searchengine.dto.serach.SearchDto;
import searchengine.dto.serach.SearchResponse;
import searchengine.model.Page;
import searchengine.model.SearchIndex;
import searchengine.model.Site;
import searchengine.model.SiteStatus;
import searchengine.repository.PageRepository;
import searchengine.repository.SiteRepository;
import searchengine.services.IndexingService;
import searchengine.services.LemmaService;
import searchengine.services.SearchService;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class SearchServiceImpl implements SearchService {

    private final IndexingService indexingService;

    private final LemmaService lemmaService;

    private final SiteRepository siteRepository;

    private final Set<String> queryLemmasFormsSet = new HashSet<>();
    private final PageRepository pageRepository;

    @Override
    public SearchResponse search(String query, String site, Integer offset, Integer limit) {
        if (query == null || query.trim().isEmpty()) {
            return createErrorResponse("Задан пустой поисковый запрос");
        }
        if (site != null && !isSiteIndexed(site)) {
            return createErrorResponse("Cайт не найден или еще не проиндексирован");
        } else {
            if (siteRepository.findAll().isEmpty() || siteRepository.findAll().stream().anyMatch(e -> !e.getStatus().equals(SiteStatus.INDEXED))) {
                SearchResponse response = new SearchResponse();
                response.setResult(false);
                response.setError("Как минимум один из сайтов еще не проиндексирован");
                return response;
            }
        }

        List<String> lemmas = extractLemmas(query);
        if (lemmas.isEmpty()) {
            return createErrorResponse("Не удалось извлечь леммы из запроса");
        }

        lemmas = filterLemmas(lemmas);
        if (lemmas.isEmpty()) {
            return createErrorResponse("Все леммы исключены из запроса");
        }

        List<Page> pages = findRelevantPages(lemmas, site);
        if (pages.isEmpty()) {
            return createEmptyResponse();
        }

        // Проходим по каждой лемме и добавляем все ее формы в querySet
        for (String lemma : lemmas) {
            Set<String> forms = lemmaService.getLemmaForms().get(lemma.toLowerCase());
            if (forms != null) {
                queryLemmasFormsSet.addAll(forms);
            }
        }

        List<SearchDto> searchResults = calculateRelevanceAndCreateDtos(pages, query, lemmas, limit, offset, queryLemmasFormsSet);
        queryLemmasFormsSet.clear();
        return createSuccessResponse(searchResults, pages.size());
    }

    private List<String> extractLemmas(String query) {
        Map<String, Integer> lemmaMap = lemmaService.collectLemmas(query);
        return new ArrayList<>(lemmaMap.keySet());
    }

    private List<String> filterLemmas(List<String> lemmas) {
        final double threshold = 0.7; // Примерное значение, необходимо подстроить
        int totalPages = (int) pageRepository.count();

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
            log.info("Searching across all indexed sites");
            for (String lemma : lemmas) {
                Set<Page> pagesWithLemma = new HashSet<>(lemmaService.findAllPagesByLemmas(lemmaService.findAllLemmasByLemma(lemma)));
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
            log.info("Searching on site: {}", siteUrl);
            Site indexedSite = siteRepository.findByUrl(siteUrl);
            if (indexedSite != null && indexedSite.getStatus() == SiteStatus.INDEXED) {
                for (String lemma : lemmas) {
                    Set<Page> pagesWithLemma = new HashSet<>(lemmaService.findAllPagesByLemmaAndSite(lemma, indexedSite));
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


    private List<SearchDto> calculateRelevanceAndCreateDtos(List<Page> pages, String query, List<String> lemmas, int limit, int offset, Set<String> querySet) {
        Map<Page, Float> relevancesMap = calculateAbsoluteRelevances(pages, lemmas);
        float maxAbsoluteRelevance = relevancesMap.values().stream()
                .max(Float::compare)
                .orElse(0.0f);

        List<SearchDto> searchResults = new ArrayList<>();
        for (Map.Entry<Page, Float> entry : relevancesMap.entrySet()) {
            Page page = entry.getKey();
            float absoluteRelevance = entry.getValue();
            SearchDto searchDto = SearchDto.builder()
                    .site(page.getSite().getUrl())
                    .siteName(page.getSite().getName())
                    .uri(page.getPath())
                    .title(extractTitle(page.getContent()))
                    .snippet(generateSnippet(page.getContent(), query, querySet))
                    .relevance(absoluteRelevance / maxAbsoluteRelevance)
                    .build();
            searchResults.add(searchDto);
        }

        searchResults.sort((dto1, dto2) -> Float.compare(dto2.getRelevance(), dto1.getRelevance()));

        // Учитываем offset и limit
        int start = Math.min(offset, searchResults.size());
        int end = Math.min(offset + limit, searchResults.size());
        return searchResults.subList(start, end);
    }


    private boolean isSiteIndexed(String siteUrl) {
        Site site = siteRepository.findByUrl(siteUrl);
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

    private String extractTitle(String content) {
        Document doc = Jsoup.parse(content);
        Element titleElement = doc.select("title").first();
        return titleElement != null ? titleElement.text() : "";
    }


    public String generateSnippet(String htmlContent, String query, Set<String> querySet) {
        int snippetLength = 200;
        Document doc = Jsoup.parse(htmlContent);
        String lowerCaseQuery = query.toLowerCase();
        String bodyText = doc.text();
        String lowerCaseContent = bodyText.toLowerCase();

        List<String> queryLemmas = extractLemmas(query);

        // Найти полное совпадение
        int fullMatchIndex = lowerCaseContent.indexOf(lowerCaseQuery);
        if (fullMatchIndex != -1) {
            String snippet = createSnippet(bodyText, fullMatchIndex, lowerCaseQuery.length(), snippetLength);
            querySet.add(query);
            snippet = highlightLemmas(snippet, querySet);
            return "..." + snippet + "...";
        }

        // Найти совпадение по леммам
        for (String queryLemma : querySet) {
            int lemmaIndex = lowerCaseContent.indexOf(queryLemma.toLowerCase());
            if (lemmaIndex != -1) {
                String snippet = createSnippet(bodyText, lemmaIndex, queryLemma.length(), snippetLength);
                snippet = highlightLemmas(snippet, querySet);
                return "..." + snippet + "...";
            }
        }

        return "ничего не найдено";
    }

    private String createSnippet(String text, int matchIndex, int matchLength, int snippetLength) {
        int snippetStart = Math.max(0, matchIndex - snippetLength / 2);
        int snippetEnd = Math.min(text.length(), matchIndex + matchLength + snippetLength / 2);
        String snippet = text.substring(snippetStart, snippetEnd);

        if (snippetStart > 0) {
            int spaceIndex = snippet.indexOf(" ");
            if (spaceIndex != -1) {
                snippet = snippet.substring(spaceIndex + 1);
            }
        }
        if (snippetEnd < text.length()) {
            int spaceIndex = snippet.lastIndexOf(" ");
            if (spaceIndex != -1) {
                snippet = snippet.substring(0, spaceIndex);
            }
        }

        return snippet;
    }

    // Вспомогательный метод для выделения лемм в сниппете
    private String highlightLemmas(String snippet, Set<String> querySet) {
        Pattern multiWordPattern = Pattern.compile(".*\\s+.*"); // Если в строке есть хотя бы один пробел,
        // не завися от того, какие символы вокруг пробела - строка многословная.


        Set<String> multiWordsQuery = querySet.stream()
                .filter(s -> multiWordPattern.matcher(s).matches())
                .collect(Collectors.toSet());

        // Сначала выделяем многословные выражения
        for (String phrase : multiWordsQuery) {
            snippet = snippet.replaceAll("(?i)" + Pattern.quote(phrase), "<b>" + phrase + "</b>");
        }

        // Разбиваем snippet на слова
        String[] words = snippet.split("\\s+");
        StringBuilder highlightedSnippet = new StringBuilder();

        for (String word : words) {
            // Сохраняем оригинальное слово и преобразуем его в нижний регистр
            String cleanWord = word.replaceAll("[^a-zA-Zа-яА-Я]", "").toLowerCase();

            // Проверяем, если очищенное слово есть в querySet
            if (querySet.contains(cleanWord) && !word.contains("<b>")) { // чтобы избежать двойного выделения
                highlightedSnippet.append("<b>").append(word).append("</b>");
            } else {
                highlightedSnippet.append(word);
            }
            highlightedSnippet.append(" ");
        }

        // Удаляем последний пробел
        return highlightedSnippet.toString().trim();
    }

    private float calculateAbsoluteRelevance(Page pages, List<String> lemmas) {
        List<SearchIndex> indices = lemmaService.findAllIndicesByWebPage(pages);
        float sumOfRanks = 0.0f;
        for (SearchIndex index : indices) {
            if (lemmas.contains(index.getLemma().getLemma())) {
                sumOfRanks += index.getRank();
            }
        }
        return sumOfRanks;
    }

    private Map<Page, Float> calculateAbsoluteRelevances(List<Page> pages, List<String> lemmas) {
        Map<Page, Float> relevancesMap = new HashMap<>();
        for (Page page : pages) {
            float absoluteRelevance = calculateAbsoluteRelevance(page, lemmas);
            relevancesMap.put(page, absoluteRelevance);
        }
        return relevancesMap;
    }
}

