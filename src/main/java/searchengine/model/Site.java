package searchengine.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.time.LocalDateTime;

import static jakarta.persistence.EnumType.STRING;
import static jakarta.persistence.GenerationType.IDENTITY;

/**
 * Сущность Site представляет сайт, который индексируется поисковой системой.
 * <p>
 * Хранит основную информацию о сайте, его статусе индексации, времени последнего обновления
 * статуса, а также об ошибках, если они возникали во время индексации.
 * </p>
 *
 * <p>Поля:</p>
 * <ul>
 *     <li>id — уникальный идентификатор сайта</li>
 *     <li>status — текущий статус индексации (SiteStatus)</li>
 *     <li>statusTime — время последнего изменения статуса</li>
 *     <li>lastError — описание последней ошибки индексации (если есть)</li>
 *     <li>url — URL сайта, должен быть уникальным</li>
 *     <li>name — название сайта</li>
 * </ul>
 *
 * <p>Пример использования:</p>
 * <pre>
 *     Site site = new Site();
 *     site.setUrl("https://example.com");
 *     site.setName("Example");
 *     site.setStatus(SiteStatus.INDEXING);
 *     site.setStatusTime(LocalDateTime.now());
 * </pre>
 */
@Entity
@Table(name = "site")
@Getter
@Setter
@ToString
@EqualsAndHashCode
public class Site implements Serializable {

    /** Уникальный идентификатор сайта */
    @Id
    @GeneratedValue(strategy = IDENTITY)
    @Column(name = "id")
    private int id;

    /** Текущий статус индексации сайта */
    @Column(name = "status", nullable = false)
    @Enumerated(STRING)
    private SiteStatus status;

    /** Время последнего изменения статуса */
    @Column(name = "status_time", nullable = false)
    private LocalDateTime statusTime;

    /** Последняя ошибка индексации, если была */
    @Column(name = "last_error", columnDefinition = "TEXT")
    private String lastError;

    /** URL сайта, должен быть уникальным */
    @Column(name = "url", nullable = false, unique = true)
    private String url;

    /** Название сайта */
    @Column(name = "name", nullable = false)
    private String name;
}
