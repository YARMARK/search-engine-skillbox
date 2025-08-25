package searchengine.dto.serach;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * DTO, представляющий результат поиска по сайту.
 * Используется для передачи данных в ответе на поисковый запрос.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SearchDto {

    /**
     * URL сайта, на котором была найдена страница.
     */
    private String site;

    /**
     * Имя сайта (например, название из конфигурации).
     */
    private String siteName;

    /**
     * Относительный путь страницы на сайте.
     */
    private String uri;

    /**
     * Заголовок страницы.
     */
    private String title;

    /**
     * Сниппет — фрагмент текста со страницы, содержащий совпадения по запросу.
     */
    private String snippet;

    /**
     * Релевантность страницы в рамках поисковой выдачи
     * (нормализованное значение от 0 до 1).
     */
    private float relevance;
}
