package ru.sber.apm.aipay.ratatouille.config.crossover;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "crossover.api")
@Validated
public class CrossoverApiProperties {

    /**
     * Базовый URL API
     */
    @NotBlank
    private String baseUrl;

    /**
     * API ключ партнера
     */
    @NotBlank
    private String apiKey;

    /**
     * Таймаут запросов в миллисекундах
     */
    @NotNull
    private Integer timeoutMs = 30000;
}