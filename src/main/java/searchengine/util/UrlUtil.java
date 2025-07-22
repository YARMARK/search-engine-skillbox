package searchengine.util;

import lombok.experimental.UtilityClass;

import java.util.Set;

@UtilityClass
public class UrlUtil {

    private static final Set<String> SKIPPED_FILE_EXTENSIONS = Set.of(
            ".jpg", ".jpeg", ".png", ".gif", ".webp", ".pdf",
            ".eps", ".xlsx", ".doc", ".pptx", ".docx"
    );

    public static boolean isFile(String link) {
        String lowerCaseLink = link.toLowerCase();
        return SKIPPED_FILE_EXTENSIONS.stream()
                .anyMatch(lowerCaseLink::endsWith);
    }
}
