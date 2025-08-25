package searchengine.services;

import org.jsoup.nodes.Document;

import java.util.Set;

/**
 * Сервис для генерации сниппетов и подсветки текста.
 * <p>
 * Отвечает за извлечение заголовков из HTML и создание сниппетов с подсветкой
 * найденных терминов.
 * </p>
 */
public interface SnippetService {

    /**
     * Извлекает заголовок страницы из HTML-документа.
     *
     * @param doc распаршенный HTML-документ
     * @return текст заголовка или пустая строка, если заголовок не найден
     */
    String extractTitle(Document doc);

    /**
     * Генерирует сниппет с подсветкой найденных терминов.
     *
     * @param bodyText текст страницы без HTML-разметки
     * @param query поисковый запрос
     * @param querySet набор словоформ для подсветки
     * @return сниппет с подсветкой или "ничего не найдено"
     */
    String generateSnippet(String bodyText, String query, Set<String> querySet);
} 
