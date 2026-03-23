package ru.sber.apm.aipay.ratatouille.dto.p2pcrypto;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.Accessors;

import java.util.List;
import java.util.UUID;

/**
 * Информация о кошельке пользователя
 */
@Getter
@Setter
@Accessors(chain = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Wallet {

    /**
     * UUID кошелька
     */
    @NotNull
    private UUID id;

    /**
     * UUID пользователя - владельца кошелька
     */
    @NotNull
    private UUID userId;

    /**
     * Валюта кошелька (ETH, BTC, USDT, TON)
     */
    @NotBlank
    private String coin;

    /**
     * Статус кошелька (ACTIVE, BLOCKED, etc.)
     */
    @NotBlank
    private String status;

    /**
     * Массив балансов кошелька в разных статусах
     */
    @Valid
    private List<Balance> balances;

    /**
     * Курсы обмена для кошелька
     */
    @Valid
    private List<WalletExchangeRate> exchangeRates;
}