package searchengine.services;

import searchengine.model.Site;
import searchengine.model.SiteStatus;

import java.util.List;

/**
 * Сервис для работы с сайтами {@link Site}.
 * <p>
 * Обеспечивает операции сохранения, обновления, поиска и удаления сайтов.
 * </p>
 */
public interface SiteService {

    /**
     * Находит сайт по его URL.
     *
     * @param url URL сайта
     * @return объект Site с указанным URL или null, если сайт не найден
     */
    Site findSiteByUrl(String url);

    /**
     * Находит список сайтов по их статусу.
     *
     * @param status статус сайта
     * @return список объектов Site с указанным статусом
     */
    List<Site> findSiteByStatus(SiteStatus status);

    /**
     * Сохраняет сайт в базе данных.
     *
     * @param site объект Site для сохранения
     * @return сохраненный объект Site
     */
    Site saveSite(Site site);

    /**
     * Находит и возвращает список всех сайтов в базе данных.
     *
     * @return список всех объектов Site
     */
    List<Site> findAllSites();
}
