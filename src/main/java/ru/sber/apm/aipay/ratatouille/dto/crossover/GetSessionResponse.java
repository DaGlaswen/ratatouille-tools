package ru.sber.apm.aipay.ratatouille.dto.crossover;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import lombok.experimental.Accessors;

@Getter
@Setter
@Accessors(chain = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class GetSessionResponse {

    @NotBlank
    private String sessionId;

    @NotBlank
    private String orderId;
}
