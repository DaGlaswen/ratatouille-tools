package ru.sber.apm.aipay.ratatouille.dto.p2pcrypto;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import lombok.experimental.Accessors;

/**
 * C2C транзакция (покупка/продажа между пользователями)
 */
@Getter
@Setter
@Accessors(chain = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class C2CHistoryTransaction {

    /**
     * ID пользователя-продавца
     */
    @NotBlank
    private String sellerId;

    /**
     * ID кошелька продавца
     */
    @NotBlank
    private String sellerWalletId;

    /**
     * ID пользователя-покупателя
     */
    @NotBlank
    private String buyerUserId;

    /**
     * ID кошелька покупателя
     */
    @NotBlank
    private String buyerWalletId;

    /**
     * Сумма в фиатной валюте
     */
    private Integer fiatValue;

    /**
     * Сумма комиссии
     */
    private Integer commissionValue;

    /**
     * Валюта монеты
     */
    @NotBlank
    private String coin;

    /**
     * Сумма в минимальных единицах монеты
     */
    @NotBlank
    private String value;

    /**
     * Статус C2C транзакции
     */
    @NotBlank
    private String c2cStatus;

    /**
     * Состояние транзакции
     */
    @NotBlank
    private String state;

    /**
     * Номер карты (маскированный)
     */
    @NotBlank
    private String cardNumber;

    /**
     * Платежная система
     */
    @NotBlank
    private String cardSystemType;

    /**
     * Дата создания
     */
    @NotBlank
    private String created;

    /**
     * Дата обновления
     */
    @NotBlank
    private String updated;

    /**
     * ID транзакции
     */
    @NotBlank
    private String id;
}
