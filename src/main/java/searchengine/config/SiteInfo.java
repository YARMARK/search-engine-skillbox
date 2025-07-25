package searchengine.config;

import lombok.Getter;
import lombok.Setter;

import java.util.Objects;

@Setter
@Getter
public class SiteInfo {

    private String url;

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
