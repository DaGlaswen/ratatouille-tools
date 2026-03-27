package ru.sber.apm.aipay.ratatouille.dto.p2pcrypto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import lombok.experimental.Accessors;

/**
 * Ответ на запрос адреса депозита
 */
@Getter
@Setter
@Accessors(chain = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class GetDepositeAddressResponse {

    /**
     * Адрес для пополнения кошелька (blockchain address)
     */
    @NotBlank
    @JsonProperty("deposite_address")
    private String depositeAddress;
}