package searchengine.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Конфигурационный класс для хранения настроек индексации сайтов.
 * Значения подгружаются из application.yml/properties с префиксом {@code indexing-settings}.
 *
 * <p>Используется для управления списком сайтов, а также настройками краулера
 * (user-agent, referrer, параллельность потоков и максимальное число
 * одновременно индексируемых сайтов).</p>
 */
@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "indexing-settings")
public class SitesList {

    /**
     * Список сайтов, которые необходимо индексировать.
     */
    private List<SiteInfo> sites;

    /**
     * Referrer, который будет указываться при выполнении HTTP-запросов к сайтам.
     */
    private String referrer;

    /**
     * User-Agent, который будет указываться при запросах к сайтам.
     */
    private String userAgent;

    /**
     * Количество потоков для параллельного выполнения задач краулера.
     * По умолчанию равно количеству доступных процессорных ядер.
     */
    private int crawlerParallelism = Runtime.getRuntime().availableProcessors();

    /**
     * Максимальное количество сайтов, индексируемых одновременно.
     * По умолчанию — 2.
     */
    private int maxConcurrentSites = 2;
}
