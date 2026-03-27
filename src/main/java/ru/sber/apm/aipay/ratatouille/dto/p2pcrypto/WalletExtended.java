package ru.sber.apm.aipay.ratatouille.dto.p2pcrypto;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.Accessors;
import tools.jackson.databind.PropertyNamingStrategies;
import tools.jackson.databind.annotation.JsonNaming;

import java.util.List;
import java.util.UUID;

/**
 * Расширенная информация о кошельке
 */
@Getter
@Setter
@Accessors(chain = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class WalletExtended {

    @NotNull
    private UUID id;

    @NotNull
    private UUID userId;

    @NotBlank
    private String coin;

    @NotBlank
    private String status;

    @Valid
    private List<Balance> balances;

    @Valid
    private List<WalletExchangeRate> exchangeRates;
}