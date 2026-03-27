package ru.sber.apm.aipay.ratatouille.dto.p2pcrypto;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import lombok.experimental.Accessors;
import tools.jackson.databind.PropertyNamingStrategies;
import tools.jackson.databind.annotation.JsonNaming;

/**
 * Курс обмена для кошелька
 */
@Getter
@Setter
@Accessors(chain = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class WalletExchangeRate {

    /**
     * Код криптовалюты (ETH, BTC, USDT, TON)
     */
    @NotBlank
    private String coin;

    /**
     * Код фиатной валюты (USD, RUB, TRY)
     */
    @NotBlank
    private String fiatCode;

    /**
     * Стоимость одной целой монеты в фиатной валюте (в минимальных единицах)
     */
    @NotBlank
    private String oneCoinPrice;

    /**
     * Эквивалент в фиатной валюте
     */
    @NotBlank
    private String value;
}