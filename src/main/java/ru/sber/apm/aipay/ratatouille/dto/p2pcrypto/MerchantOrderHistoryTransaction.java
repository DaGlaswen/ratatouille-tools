package ru.sber.apm.aipay.ratatouille.dto.p2pcrypto;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import lombok.experimental.Accessors;
import tools.jackson.databind.PropertyNamingStrategies;
import tools.jackson.databind.annotation.JsonNaming;

/**
 * История транзакций мерчанта
 */
@Getter
@Setter
@Accessors(chain = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class MerchantOrderHistoryTransaction {

    /**
     * Валюта монеты
     */
    @NotBlank
    private String coin;

    /**
     * Количество монет в минимальных единицах
     */
    @NotBlank
    private String coinCount;

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
     * Сумма в валюте мерчанта
     */
    @NotBlank
    private String merchantAmount;

    /**
     * ID мерчанта
     */
    @NotBlank
    private String merchantId;

    /**
     * ID кошелька мерчанта
     */
    @NotBlank
    private String merchantWalletId;

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
