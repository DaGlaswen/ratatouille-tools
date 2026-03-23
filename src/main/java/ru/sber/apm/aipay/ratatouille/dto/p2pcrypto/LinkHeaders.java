package ru.sber.apm.aipay.ratatouille.dto.p2pcrypto;

import lombok.*;
import lombok.experimental.Accessors;
import jakarta.validation.constraints.NotBlank;

/**
 * Общие заголовки для запросов к LINK API
 */
@Getter
@Setter
@Accessors(chain = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LinkHeaders {

    /**
     * RQUID - уникальный UUID запроса (обязательный)
     */
    @NotBlank
    private String rquid;

    /**
     * Фабричный метод для создания заголовков с генерацией RQUID
     */
    public static LinkHeaders of() {
        return LinkHeaders.builder()
                .rquid(java.util.UUID.randomUUID().toString())
                .build();
    }

    public static LinkHeaders of(String rquid) {
        return LinkHeaders.builder()
                .rquid(rquid != null ? rquid : java.util.UUID.randomUUID().toString())
                .build();
    }
}