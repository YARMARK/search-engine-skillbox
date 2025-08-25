package searchengine.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;
import searchengine.model.Page;
import searchengine.model.Site;

import java.util.List;

/**
 * Репозиторий для работы с сущностью {@link Page}.
 *
 * Предоставляет стандартные CRUD операции благодаря {@link JpaRepository},
 * а также дополнительные методы для проверки существования, поиска, подсчета и удаления страниц.
 */
public interface PageRepository extends JpaRepository<Page, Integer> {

    /**
     * Проверяет, существует ли страница с указанным путем.
     *
     * @param path путь страницы
     * @return {@code true}, если страница существует, иначе {@code false}
     */
    boolean existsPageByPath(String path);

    /**
     * Находит страницу по пути.
     *
     * @param url путь страницы
     * @return найденная страница
     */
    Page findByPath(String url);

    /**
     * Считает количество страниц, принадлежащих указанному сайту.
     *
     * @param site сайт
     * @return количество страниц
     */
    int countBySite(Site site);

    /**
     * Удаляет все страницы, принадлежащие указанному сайту.
     *
     * @param site сайт, страницы которого нужно удалить
     */
    @Modifying
    @Query("DELETE FROM Page p WHERE p.site = :site")
    void deleteAllPagesBySite(Site site);
}

