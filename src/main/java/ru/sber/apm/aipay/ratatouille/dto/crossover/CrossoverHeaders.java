package ru.sber.apm.aipay.ratatouille.dto.crossover;

import lombok.*;
import lombok.experimental.Accessors;
import jakarta.validation.constraints.NotBlank;
import ru.sber.apm.aipay.ratatouille.exception.crossover.CrossoverApiException;

/**
 * Общие заголовки для запросов к Crossover API
 */
@Getter
@Setter
@Accessors(chain = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CrossoverHeaders {

    /**
     * API KEY для авторизации
     */
    @NotBlank
    private String authorization;

    /**
     * Время отправки запроса в формате ISO 8601 (обязательный)
     */
    @NotBlank
    private String timestamp;

    /**
     * Уникальный идентификатор запроса (обязательный)
     */
    @NotBlank
    private String rqUID;

    /**
     * Уникальный идентификатор сессии на фронтенде (опциональный)
     */
    private String localSessionId;

    /**
     * Фабричный метод для создания заголовков с генерацией недостающих полей
     */
    public static CrossoverHeaders of(@NotBlank String apiKey) {
        if (apiKey == null || apiKey.isBlank()) {
            throw CrossoverApiException.internalError("API ключ не настроен в конфигурации", null);
        }
        return CrossoverHeaders.builder()
                .authorization("Bearer: " + apiKey)
                .timestamp(java.time.Instant.now().toString())
                .rqUID(java.util.UUID.randomUUID().toString())
                .build();
    }
}