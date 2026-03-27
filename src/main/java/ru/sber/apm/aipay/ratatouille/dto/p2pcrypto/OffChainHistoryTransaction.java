package ru.sber.apm.aipay.ratatouille.dto.p2pcrypto;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import lombok.experimental.Accessors;
import tools.jackson.databind.PropertyNamingStrategies;
import tools.jackson.databind.annotation.JsonNaming;

/**
 * Off-chain транзакция (перевод между кошельками внутри системы)
 */
@Getter
@Setter
@Accessors(chain = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class OffChainHistoryTransaction {

    /**
     * Валюта монеты
     */
    @NotBlank
    private String coin;

    /**
     * Дата создания
     */
    @NotBlank
    private String created;

    /**
     * Адрес отправителя
     */
    @NotBlank
    private String from;

    /**
     * Адрес получателя
     */
    @NotBlank
    private String to;

    /**
     * Сумма в минимальных единицах
     */
    @NotBlank
    private String value;
}
