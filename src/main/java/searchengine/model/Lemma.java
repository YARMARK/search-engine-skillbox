package searchengine.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;

import static jakarta.persistence.GenerationType.IDENTITY;

/**
 * Сущность Lemma представляет лемму (основную форму слова),
 * встречающуюся на сайте, и её частоту использования.
 * <p>
 * Класс привязан к таблице {@code lemma} в базе данных.
 * Каждая лемма относится к конкретному сайту через связь Many-to-One.
 * </p>
 *
 * <p>Пример использования:</p>
 * <pre>
 *     Lemma lemma = new Lemma();
 *     lemma.setLemma("пример");
 *     lemma.setFrequency(10);
 * </pre>
 *
 * <p>Сравнение лемм происходит по частоте их появления.</p>
 */
@Entity
@Table(name = "lemma")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class Lemma implements Serializable, Comparable<Lemma> {

    /** Уникальный идентификатор леммы */
    @Id
    @GeneratedValue(strategy = IDENTITY)
    @Column(name = "id", nullable = false)
    private int id;

    /** Сайт, на котором встречается лемма */
    @ManyToOne
    @JoinColumn(name = "site_id", nullable = false)
    private Site site;

    /** Текстовое представление леммы */
    @Column(name = "lemma", nullable = false)
    private String lemma;

    /** Частота появления леммы на сайте */
    @Column(name = "frequency", nullable = false)
    private int frequency;

    @Override
    public int compareTo(Lemma o) {
        if (frequency > o.getFrequency()) {
            return 1;
        } else if (frequency < o.getFrequency()) {
            return -1;
        }
        return 0;
    }
}
