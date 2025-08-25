package searchengine.util;

import lombok.experimental.UtilityClass;

import java.util.regex.Pattern;

/**
 * Утилитный класс для валидации URL-адресов.
 * <p>
 * Содержит метод {@link #isValidUrl(String)}, который проверяет,
 * соответствует ли строка корректному формату URL.
 */
@UtilityClass
public class PatternValidationUtil {

    private static final Pattern URL_PATTERN = Pattern.compile(
            "^(https?://)?(www\\.)?([\\w\\-]+\\.)+[a-zA-Z]{2,}(/[\\w\\-./?%&=]*)?$"
    );

    /**
     * Проверяет, является ли переданная строка корректным URL.
     *
     * @param url строка для проверки
     * @return {@code true}, если строка является валидным URL, иначе {@code false}
     */
    public static boolean isValidUrl(String url) {
        if (url == null || url.trim().isEmpty()) {
            return false;
        }
        return URL_PATTERN.matcher(url).matches();
    }
}
