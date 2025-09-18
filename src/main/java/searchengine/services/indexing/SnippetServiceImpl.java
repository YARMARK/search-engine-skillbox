package searchengine.services.indexing;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.stereotype.Service;
import searchengine.config.SearchConfig;
import searchengine.services.SnippetService;

import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Реализация {@link SnippetService} для генерации сниппетов и подсветки текста.
 * <p>
 * Извлекает заголовки из HTML и создает сниппеты с подсветкой найденных терминов.
 * </p>
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SnippetServiceImpl implements SnippetService {

    private final SearchConfig searchConfig;

    @Override
    public String extractTitle(Document doc) {
        Element titleElement = doc.select("title").first();
        return titleElement != null ? titleElement.text() : "";
    }

    @Override
    public String generateSnippet(String bodyText, String query, Set<String> querySet) {
        int snippetLength = searchConfig.getSnippetLength();
        String lowerCaseQuery = query.toLowerCase();
        String lowerCaseContent = bodyText.toLowerCase();

        int fullMatchIndex = lowerCaseContent.indexOf(lowerCaseQuery);
        if (fullMatchIndex != -1) {
            String snippet = createSnippet(bodyText, fullMatchIndex, lowerCaseQuery.length(), snippetLength);
            querySet.add(query);
            snippet = highlightLemmas(snippet, querySet);
            return "..." + snippet + "...";
        }

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

    private String highlightLemmas(String snippet, Set<String> querySet) {
        Pattern multiWordPattern = Pattern.compile(".*\\s+.*");

        Set<String> multiWordsQuery = querySet.stream()
                .filter(s -> multiWordPattern.matcher(s).matches())
                .collect(Collectors.toSet());

        for (String phrase : multiWordsQuery) {
            snippet = snippet.replaceAll("(?i)" + Pattern.quote(phrase), "<b>" + phrase + "</b>");
        }

        String[] words = snippet.split("\\s+");
        StringBuilder highlightedSnippet = new StringBuilder();

        for (String word : words) {

            String cleanWord = word.replaceAll("[^a-zA-Zа-яА-Я]", "").toLowerCase();

            if (querySet.contains(cleanWord) && !word.contains("<b>")) { // чтобы избежать двойного выделения
                highlightedSnippet.append("<b>").append(word).append("</b>");
            } else {
                highlightedSnippet.append(word);
            }
            highlightedSnippet.append(" ");
        }

        return highlightedSnippet.toString().trim();
    }
} 
