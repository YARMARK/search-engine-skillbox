package searchengine.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import searchengine.model.Site;
import searchengine.model.SiteStatus;

import java.util.List;

/**
 * Репозиторий для работы с сущностью {@link Site}.
 *
 * Предоставляет стандартные CRUD операции благодаря {@link JpaRepository},
 * а также дополнительные методы для поиска и удаления сайтов по URL и статусу.
 */
public interface SiteRepository extends JpaRepository<Site, Integer> {

    /**
     * Находит сайт по его URL.
     *
     * @param url URL сайта
     * @return объект {@link Site} или {@code null}, если сайт не найден
     */
    Site findByUrl(String url);

    /**
     * Находит все сайты с указанным статусом.
     *
     * @param status статус сайта
     * @return список сайтов с указанным статусом
     */
    List<Site> findByStatus(SiteStatus status);

    /**
     * Удаляет сайт по его URL.
     *
     * @param url URL сайта, который нужно удалить
     */
    void deleteByUrl(String url);
}
