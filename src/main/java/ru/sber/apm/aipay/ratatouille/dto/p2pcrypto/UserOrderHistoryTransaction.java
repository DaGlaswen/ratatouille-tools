package ru.sber.apm.aipay.ratatouille.dto.p2pcrypto;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import lombok.experimental.Accessors;
import tools.jackson.databind.PropertyNamingStrategies;
import tools.jackson.databind.annotation.JsonNaming;

/**
 * История транзакций пользователя (ордера)
 */
@Getter
@Setter
@Accessors(chain = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class UserOrderHistoryTransaction {

    /**
     * Дата создания
     */
    @NotBlank
    private String created;

    /**
     * Валюта
     */
    @NotBlank
    private String currency;

    /**
     * ID транзакции
     */
    @NotBlank
    private String id;

    /**
     * Сумма мерчанта
     */
    @NotBlank
    private String merchantAmount;

    /**
     * ID мерчанта
     */
    @NotBlank
    private String merchantId;

    /**
     * Название мерчанта
     */
    @NotBlank
    private String merchantName;

    /**
     * ID карты пользователя
     */
    @NotBlank
    private String userCardId;

    /**
     * ID пользователя
     */
    @NotBlank
    private String userId;

    /**
     * Сумма
     */
    @NotBlank
    private String value;
}
