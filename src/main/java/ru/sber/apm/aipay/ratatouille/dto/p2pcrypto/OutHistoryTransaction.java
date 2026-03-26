package ru.sber.apm.aipay.ratatouille.dto.p2pcrypto;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import lombok.experimental.Accessors;

/**
 * Исходящая транзакция (вывод средств с кошелька)
 */
@Getter
@Setter
@Accessors(chain = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class OutHistoryTransaction {

    /**
     * Gas для транзакции
     */
    private Integer gas;

    /**
     * Цена gas
     */
    private String gasPrice;

    /**
     * Статус транзакции (SENT, WAITING_FOR_CONFIRMATION, CONFIRMED)
     */
    private String state;

    /**
     * Время транзакции
     */
    @NotBlank
    private String time;

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

    /**
     * Сумма вывода с комиссией
     */
    private String withdrawValue;

    /**
     * Валюта монеты
     */
    @NotBlank
    private String coin;

    /**
     * Комиссия
     */
    private String commission;
}
