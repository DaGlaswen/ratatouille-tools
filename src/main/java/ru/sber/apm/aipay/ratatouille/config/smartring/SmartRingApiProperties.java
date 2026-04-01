package ru.sber.apm.aipay.ratatouille.config.smartring;

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
@ConfigurationProperties(prefix = "smartring.api")
@Validated
public class SmartRingApiProperties {

    @NotBlank
    private String baseUrl;

    @NotBlank
    private String uuid;

    @NotNull
    private Integer timeoutMs = 30000;
}