package ru.sber.apm.aipay.ratatouille.dto.p2pcrypto;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import lombok.experimental.Accessors;

/**
 * Входящая транзакция (пополнение кошелька извне)
 */
@Getter
@Setter
@Accessors(chain = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class InHistoryTransaction {

    /**
     * Адрес отправителя
     */
    @NotBlank
    private String from;

    /**
     * Gas для транзакции
     */
    private Integer gas;

    /**
     * Цена gas
     */
    private String gasPrice;

    /**
     * Время транзакции
     */
    @NotBlank
    private String time;

    /**
     * Сумма в минимальных единицах
     */
    @NotBlank
    private String value;

    /**
     * Статус транзакции (WAITING_FOR_CONFIRMATION, CONFIRMED)
     */
    private String state;

    /**
     * Валюта монеты
     */
    @NotBlank
    private String coin;
}
