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

    // ─── Дефолты для SDK-заголовков MCP-контекста ───

    /** appName: по умолчанию "ratatouille-mcp" */
    private String appName = "ratatouille-mcp";

    /** deviceName: по умолчанию "openclaw-gateway" */
    private String deviceName = "openclaw-gateway";

    /** SDK-версия: по умолчанию "1.0.0" */
    private String sdkVersion = "1.0.0";

    /** OS-строка: по умолчанию "server" */
    private String os = "server";

    /** X-System-Id */
    private String xSystemId = "SBERPAY_SDK";

    /** x-pod-sticky: по умолчанию пустая строка (отправляется только если задана) */
    private String xPodSticky = "e425baa5ac1f1d3917a7aee1358b43f3";

    /**
     * Cookie сессии (если есть — будет передан в запросе)
     */
    private String cookie;
}
