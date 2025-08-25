package searchengine.dto.serach;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * DTO, представляющий ответ на поисковый запрос.
 * Содержит информацию об успешности поиска, количество найденных результатов
 * и список найденных страниц.
 */
@Getter
@Setter
public class SearchResponse {

    /**
     * Флаг результата выполнения поиска.
     * true — поиск выполнен успешно,
     * false — произошла ошибка.
     */
    private boolean result = true;

    /**
     * Сообщение об ошибке, если {@code result = false}.
     * Может быть {@code null}, если ошибки не было.
     */
    private String error;

    /**
     * Количество найденных страниц по запросу.
     */
    private Integer count;

    /**
     * Список результатов поиска в виде DTO.
     */
    private List<SearchDto> data;
}
