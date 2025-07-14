package searchengine.util;

import lombok.experimental.UtilityClass;

import java.util.Set;

@UtilityClass
public class UrlUtil {

    private static final Set<String> SKIPPED_FILE_EXTENSIONS = Set.of(
            ".jpg", ".jpeg", ".png", ".gif", ".webp", ".pdf",
            ".eps", ".xlsx", ".doc", ".pptx", ".docx"
    );

    private static final String[] URL_PREFIXES = {
            "http://www.", "https://www.", "http://", "https://", "www."
    };

    public static boolean isFile(String link) {
        String lowerCaseLink = link.toLowerCase();
        return SKIPPED_FILE_EXTENSIONS.stream()
                .anyMatch(lowerCaseLink::endsWith);
    }

    public static String getCleanedBaseUrl(String url) {
        for (String prefix : URL_PREFIXES) {
            if (url.startsWith(prefix)) {
                return url.substring(prefix.length());
            }
        }
        return url;
    }
}
