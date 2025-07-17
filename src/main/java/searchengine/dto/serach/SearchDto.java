package searchengine.dto.serach;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SearchDto {

    private String site;

    private String siteName;

    private String uri;

    private String title;

    private String snippet;

    private float relevance;

}
