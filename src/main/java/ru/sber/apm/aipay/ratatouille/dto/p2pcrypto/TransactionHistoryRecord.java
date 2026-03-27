package ru.sber.apm.aipay.ratatouille.dto.p2pcrypto;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.Accessors;
import tools.jackson.databind.PropertyNamingStrategies;
import tools.jackson.databind.annotation.JsonNaming;

/**
 * Запись истории транзакций
 */
@Getter
@Setter
@Accessors(chain = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class TransactionHistoryRecord {

    /**
     * ID записи истории
     */
    @NotBlank
    private String id;

    /**
     * Тип транзакции (OFF_CHAIN_TRANSFER_IN, COIN_SELL, COIN_BUY, IN, OUT, MERCHANT_ORDER)
     */
    @NotBlank
    private String type;

    /**
     * ID пользователя
     */
    @NotBlank
    private String userId;

    /**
     * ID кошелька
     */
    @NotBlank
    private String walletId;

    /**
     * Транзакция (может быть разных типов)
     */
    @Valid
    private Object transaction;

    /**
     * Дата создания записи
     */
    @NotBlank
    private String created;

    /**
     * Дата обновления записи
     */
    @NotBlank
    private String updated;
}
