package searchengine.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;

import static jakarta.persistence.FetchType.EAGER;
import static jakarta.persistence.GenerationType.IDENTITY;
/**
 * Сущность Page представляет страницу сайта, которая была проиндексирована.
 * <p>
 * Класс привязан к таблице {@code page} в базе данных. Каждая страница связана
 * с конкретным сайтом через связь Many-to-One.
 * </p>
 *
 * <p>Пример использования:</p>
 * <pre>
 *     Page page = new Page();
 *     page.setPath("/example-page");
 *     page.setCode(200);
 *     page.setContent("&lt;html&gt;...&lt;/html&gt;");
 * </pre>
 */
@Entity
@Table(name = "page")
@Getter
@Setter
@ToString
@EqualsAndHashCode
public class Page implements Serializable {

    /** Уникальный идентификатор страницы */
    @Id
    @GeneratedValue(strategy = IDENTITY)
    @Column(name = "id")
    private int id;

    /** Сайт, к которому относится данная страница */
    @ManyToOne(fetch = EAGER)
    @JoinColumn(name = "site_id", nullable = false, referencedColumnName = "id")
    private Site site;

    /** Путь страницы на сайте (URL) */
    @Column(name = "path", nullable = false, columnDefinition = "TEXT")
    private String path;

    /** HTTP-код ответа страницы (например, 200, 404) */
    @Column(name = "code", nullable = false)
    private int code;

    /** Контент страницы в виде HTML или текста */
    @Column(name = "content", nullable = false, columnDefinition = "MEDIUMTEXT")
    private String content;
}
