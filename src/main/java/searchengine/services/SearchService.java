package searchengine.services;

import searchengine.dto.serach.SearchResponse;

/**
 * Интерфейс поискового сервиса.
 * Определяет контракт для выполнения поисковых запросов по проиндексированным сайтам.
 */
public interface SearchService {

    /**
     * Выполняет поиск по запросу с возможным ограничением по конкретному сайту.
     *
     * @param query  текст поискового запроса
     * @param site   необязательный URL сайта (если null — поиск выполняется по всем сайтам)
     * @param offset смещение (с какого результата начинать выдачу)
     * @param limit  ограничение на количество результатов
     * @return {@link SearchResponse} с результатами поиска или сообщением об ошибке
     */
    SearchResponse search(String query, String site, Integer offset, Integer limit);
}
