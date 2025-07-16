 - open in view to false
 - spring-boot-configuration-processor - используется для генерации метаданных автоподстановки в application.properties или application.yml при работе с кастомными конфигурационными классами в Spring Boot. Если ты создаёшь собственные классы конфигурации, помеченные аннотацией @ConfigurationProperties, то Spring Boot может автоматически сгенерировать метаданные, чтобы IDE (например, IntelliJ IDEA) могла:
Подсказывать названия параметров; Проверять типы значений; Показывать документацию прямо в application.yml/application.properties.

- добавть start/stop index черех бд
- добавить thread manager
- транзакции
- exceptionHandler
- добавить аудит
- actuator
- emitter
- swagger

SELECT path, COUNT(*) as count
FROM page
GROUP BY path
HAVING count(*) > 1;

