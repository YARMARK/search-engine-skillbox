package searchengine.dto.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class IndexingResponse {

    private boolean result;

    private String error;

    public IndexingResponse() {
        result = true;
        error = null;
    }

    public IndexingResponse(String error) {
        result = false;
        this.error = error;
    }
}
