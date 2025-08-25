package searchengine.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import searchengine.model.Lemma;
import searchengine.model.Site;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * Репозиторий для работы с сущностью {@link Lemma}.
 *
 * Предоставляет стандартные CRUD операции благодаря {@link JpaRepository},
 * а также дополнительные методы для поиска, подсчета и удаления лемм по различным критериям.
 */
public interface LemmaRepository extends JpaRepository<Lemma, Integer> {

    /**
     * Находит все леммы с указанным текстом.
     *
     * @param lemma текст леммы
     * @return список лемм с указанным текстом
     */
    List<Lemma> findAllByLemma(String lemma);

    /**
     * Находит лемму по тексту и сайту.
     *
     * @param lemma текст леммы
     * @param site сайт, к которому принадлежит лемма
     * @return Optional с найденной леммой или пустой, если лемма не найдена
     */
    @Query("SELECT l FROM Lemma l WHERE l.lemma = :lemma AND l.site = :site")
    Optional<Lemma> findByLemmaAndSite(@Param("lemma") String lemma, @Param("site") Site site);

    /**
     * Находит все леммы с текстами из указанной коллекции для конкретного сайта.
     *
     * @param lemmas коллекция текстов лемм
     * @param siteId идентификатор сайта
     * @return список найденных лемм
     */
    @Query(value = """
            SELECT * FROM lemma
            WHERE lemma IN (:lemmas) AND site_id = :siteId
            """, nativeQuery = true)
    List<Lemma> findAllByLemmaInAndSite(@Param("lemmas") Collection<String> lemmas,
                                        @Param("siteId") int siteId);

    /**
     * Считает количество лемм для конкретного сайта.
     *
     * @param site сайт, по которому выполняется подсчет
     * @return количество лемм
     */
    @Query("SELECT COUNT(l.lemma) FROM Lemma l WHERE l.site = :site")
    Integer countLemmasBySite(Site site);

    /**
     * Считает общее количество лемм во всех сайтах.
     *
     * @return общее количество лемм
     */
    @Query("SELECT COUNT(l.lemma) FROM Lemma l")
    Integer countAllLemmas();

    /**
     * Удаляет все леммы, принадлежащие указанному сайту.
     *
     * @param site сайт, леммы которого нужно удалить
     */
    @Modifying
    @Query("DELETE FROM Lemma l WHERE l.site = :site")
    void deleteAllLemmasBySite(Site site);
}
