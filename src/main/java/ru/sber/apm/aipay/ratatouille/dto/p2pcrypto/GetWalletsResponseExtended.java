package ru.sber.apm.aipay.ratatouille.dto.p2pcrypto;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.Valid;
import lombok.*;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * Ответ на запрос списка кошельков пользователя
 */
@Getter
@Setter
@Accessors(chain = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class GetWalletsResponseExtended {

    /**
     * Общий баланс во всех фиатных валютах
     */
    @Valid
    private List<WalletTotalBalance> totalBalance;

    /**
     * Список кошельков пользователя
     */
    @Valid
    private List<WalletExtended> wallets;
}