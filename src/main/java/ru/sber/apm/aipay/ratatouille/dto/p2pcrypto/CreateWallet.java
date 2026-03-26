package ru.sber.apm.aipay.ratatouille.dto.p2pcrypto;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import lombok.experimental.Accessors;

/**
 * Запрос на создание кошелька
 */
@Getter
@Setter
@Accessors(chain = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CreateWallet {

    /**
     * Валюта кошелька (ETH, BTC, USDT, TON)
     */
    @NotBlank
    private String coin;
}
