package searchengine.util;

import lombok.experimental.UtilityClass;

import java.util.Set;

/**
 * Утилитный класс для работы с URL.
 * <p>
 * Содержит методы для проверки, относится ли URL к файлу по расширению.
 */
@UtilityClass
public class UrlUtil {

    private static final Set<String> SKIPPED_FILE_EXTENSIONS = Set.of(
            ".jpg", ".jpeg", ".png", ".gif", ".webp", ".pdf",
            ".eps", ".xlsx", ".doc", ".pptx", ".docx"
    );

    /**
     * Проверяет, оканчивается ли ссылка на одно из известных расширений файлов.
     *
     * @param link ссылка для проверки
     * @return {@code true}, если ссылка указывает на файл, иначе {@code false}
     */
    public static boolean isFile(String link) {
        String lowerCaseLink = link.toLowerCase();
        return SKIPPED_FILE_EXTENSIONS.stream()
                .anyMatch(lowerCaseLink::endsWith);
    }
}
