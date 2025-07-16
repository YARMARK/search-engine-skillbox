package searchengine.util;

import lombok.experimental.UtilityClass;

import java.util.regex.Pattern;

@UtilityClass
public class PatternValidationUtil {

    private static final Pattern URL_PATTERN = Pattern.compile(
            "^(https?://)?(www\\.)?([\\w\\-]+\\.)+[a-zA-Z]{2,}(/[\\w\\-./?%&=]*)?$"
    );

    public static boolean isValidUrl(String url) {
        if (url == null || url.trim().isEmpty()) {
            return false;
        }
        return URL_PATTERN.matcher(url).matches();
    }
}
