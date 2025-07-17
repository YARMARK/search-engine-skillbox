package searchengine.dto.serach;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class SearchResponse {

        private boolean result = true;

        private String error;

        private Integer count;

        private List<SearchDto> data;
}
