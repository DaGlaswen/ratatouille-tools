package ru.sber.apm.aipay.ratatouille.dto.p2pcrypto;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.*;
import lombok.experimental.Accessors;

/**
 * Запрос на снятие средств с кошелька
 */
@Getter
@Setter
@Accessors(chain = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class WithdrawRequest {

    /**
     * Адрес получателя в блокчейне (0x... для EVM)
     */
    @NotBlank
    @Pattern(regexp = "^0x[a-fA-F0-9]{40}$", message = "Неверный формат адреса. Ожидается 0x + 40 hex символов")
    private String to;

    /**
     * Количество монет в минимальных единицах (wei для ETH, satoshi для BTC)
     */
    @NotBlank
    @Pattern(regexp = "^\\d+$", message = "Значение должно быть положительным целым числом")
    private String value;
}