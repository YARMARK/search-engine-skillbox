package searchengine.services;

import searchengine.model.Lemma;
import searchengine.model.Page;
import searchengine.model.Site;

import java.util.List;

/**
 * Сервис для работы со страницами {@link Page}.
 * <p>
 * Обеспечивает операции проверки существования, поиска, сохранения,
 * удаления и подсчёта страниц, а также взаимодействие с базой данных.
 * </p>
 */
public interface PageService {

    /**
     * Проверяет, существует ли страница с указанным путем.
     *
     * @param path путь страницы
     * @return true, если страница существует, иначе false
     */
    boolean existsPageByPath(String path);

    /**
     * Удаляет страницу.
     *
     * @param page страница
     */
    void deletePage(Page page);

    /**
     * Находит страницу по указанному URL.
     *
     * @param url путь страницы
     * @return объект Page или null, если страница не найдена
     */
    Page findPageByPath(String url);

    /**
     * Подсчитывает количество страниц, принадлежащих конкретному сайту.
     *
     * @param site объект Site
     * @return количество страниц на сайте
     */
    int countPageBySite(Site site);

    /**
     * Подсчитывает общее количество страниц в базе данных.
     *
     * @return общее количество страниц
     */
    int countAllPages();

    /**
     * Удаляет все страницы, принадлежащие конкретному сайту.
     *
     * @param site объект Site
     */
    void deleteAllPagesBySite(Site site);

    /**
     * Сохраняет страницу в базе данных.
     *
     * @param page объект Page для сохранения
     * @return сохранённый объект Page
     */
    Page savePage(Page page);

    /**
     * Находит все страницы по лемме и сайту.
     *
     * @param lemma строка леммы
     * @param site объект Site
     * @return список страниц, содержащих указанную лемму
     */
    List<Page> findAllPagesByLemmaAndSite(String lemma, Site site);

    /**
     * Находит все страницы по списку лемм.
     *
     * @param lemmas список объектов Lemma
     * @return список страниц, содержащих указанные леммы
     */
    List<Page> findAllPagesByLemmas(List<Lemma> lemmas);
}
