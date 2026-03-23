package ru.sber.apm.aipay.ratatouille.dto.p2pcrypto;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import lombok.experimental.Accessors;

/**
 * Общий баланс в фиатной валюте
 */
@Getter
@Setter
@Accessors(chain = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class WalletTotalBalance {

    /**
     * Код фиатной валюты (USD, RUB, TRY)
     */
    @NotBlank
    private String fiatCode;

    /**
     * Общая сумма в фиатной валюте (в копейках/центах)
     */
    @NotBlank
    private String totalFiatValue;
}