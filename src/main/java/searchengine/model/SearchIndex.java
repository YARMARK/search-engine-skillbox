package searchengine.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;

/**
 * Сущность SearchIndex представляет индекс поиска, связывающий страницу и лемму.
 * <p>
 * Каждый объект SearchIndex содержит информацию о том, на какой странице
 * встречается конкретная лемма и с каким "весом" (rank) она учитывается
 * в поисковой выдаче.
 * </p>
 *
 * <p>Связи:</p>
 * <ul>
 *     <li>Page — страница сайта, на которой встречается лемма (Many-to-One)</li>
 *     <li>Lemma — лемма, встречающаяся на странице (Many-to-One)</li>
 * </ul>
 *
 * <p>Пример использования:</p>
 * <pre>
 *     SearchIndex index = new SearchIndex();
 *     index.setPage(page);
 *     index.setLemma(lemma);
 *     index.setRank(0.75f);
 * </pre>
 */
@Entity(name = "index_table")
@Getter
@Setter
@EqualsAndHashCode(exclude = {"id"})
@ToString(exclude = {"id"})
public class SearchIndex implements Serializable {

    /** Уникальный идентификатор записи индекса */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private int id;

    /** Страница сайта, на которой встречается лемма */
    @ManyToOne
    @JoinColumn(name = "page_id", nullable = false)
    private Page page;

    /** Лемма, которая встречается на странице */
    @ManyToOne
    @JoinColumn(name = "lemma_id", nullable = false)
    private Lemma lemma;

    /** Ранг леммы на данной странице для поисковой выдачи */
    @Column(name = "index_rank", nullable = false)
    private float rank;
}
