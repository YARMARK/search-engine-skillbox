package searchengine.dto.response;

import lombok.Getter;
import lombok.Setter;


/**
 * Класс ответа для операций индексирования.
 * <p>
 * Используется для передачи информации о результате выполнения операции индексирования страницы или сайтов.
 * </p>
 *
 * <p>Поля:</p>
 * <ul>
 *     <li>{@code result} — флаг успешности операции. {@code true}, если операция выполнена успешно, {@code false} в случае ошибки.</li>
 *     <li>{@code error} — сообщение об ошибке, если операция завершилась неудачей; {@code null} при успешной операции.</li>
 * </ul>
 *
 * <p>Конструкторы:</p>
 * <ul>
 *     <li>{@link #IndexingResponse()} — создаёт объект с успешным результатом (result = true, error = null).</li>
 *     <li>{@link #IndexingResponse(String)} — создаёт объект с ошибкой (result = false, error = указанное сообщение).</li>
 * </ul>
 */
@Getter
@Setter
public class IndexingResponse {

    /** Флаг успешности операции индексирования. */
    private boolean result;

    /** Сообщение об ошибке при неудачной операции. */
    private String error;

    /**
     * Конструктор по умолчанию. Создаёт успешный ответ (result = true).
     */
    public IndexingResponse() {
        result = true;
        error = null;
    }

    /**
     * Конструктор с сообщением об ошибке.
     *
     * @param error сообщение об ошибке
     */
    public IndexingResponse(String error) {
        result = false;
        this.error = error;
    }
}
