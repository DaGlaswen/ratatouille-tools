package ru.sber.apm.aipay.ratatouille.config.p2pcrypto;

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
@ConfigurationProperties(prefix = "link.api")
@Validated
public class LinkApiProperties {

    @NotBlank
    private String baseUrl;

    @NotNull
    private Integer timeoutMs = 30000;

//    @NotBlank
//    private String sslBundle = "link-mtls";
}