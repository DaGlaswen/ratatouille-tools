package ru.sber.apm.aipay.ratatouille.dto.p2pcrypto;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.Accessors;
import tools.jackson.databind.PropertyNamingStrategies;
import tools.jackson.databind.annotation.JsonNaming;

import java.util.UUID;

/**
 * Баланс кошелька в определённом статусе
 */
@Getter
@Setter
@Accessors(chain = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class Balance {

    /**
     * UUID баланса
     */
    @NotNull
    private UUID id;

    /**
     * Тип баланса (FREE, VERIFICATION, HOLD_ON_TICKET, HOLD_ON_PROCESSING, SOLD, WITHDRAW)
     */
    @NotBlank
    private String type;

    /**
     * Количество монеты на балансе в минимальных единицах (wei для ETH, satoshi для BTC)
     */
    @NotBlank
    private String value;
}