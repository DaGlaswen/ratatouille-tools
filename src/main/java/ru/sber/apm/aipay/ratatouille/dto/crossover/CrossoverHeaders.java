package ru.sber.apm.aipay.ratatouille.dto.crossover;

import lombok.*;
import lombok.experimental.Accessors;
import jakarta.validation.constraints.NotBlank;

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
//
//    public CrossoverHeaders setAuthorization(String apiKey) {
//        this.authorization = "Bearer: " + apiKey;
//        return this;
//    }
}