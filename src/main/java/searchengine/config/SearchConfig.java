package searchengine.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Конфигурация для поискового сервиса.
 * <p>
 * Содержит настройки, которые ранее были "числами" в коде.
 * </p>
 */
@Component
@ConfigurationProperties(prefix = "search")
@Data
public class SearchConfig {

    /**
     * Порог частотности для фильтрации лемм.
     * Леммы с частотой выше этого порога (относительно общего количества страниц) исключаются из поиска.
     */
    private double frequencyThreshold = 0.7;

    /**
     * Максимальная длина сниппета в символах.
     */
    private int snippetLength = 200;
} 
