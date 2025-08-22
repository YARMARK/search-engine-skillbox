package searchengine.config;

import lombok.Getter;
import lombok.Setter;

import java.util.Objects;

/**
 * DTO-класс для описания информации о сайте, который необходимо индексировать.
 * Используется в конфигурации {@link SitesList}.
 */
@Setter
@Getter
public class SiteInfo {

    /**
     * Название сайта (для отображения в результатах поиска).
     */
    private String url;

    /**
     * Базовый URL сайта, с которого начинается индексация.
     */
    private String name;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SiteInfo info = (SiteInfo) o;
        return Objects.equals(url, info.url) && Objects.equals(name, info.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(url, name);
    }
}
