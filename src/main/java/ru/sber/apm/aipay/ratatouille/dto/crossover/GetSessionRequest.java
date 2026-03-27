package ru.sber.apm.aipay.ratatouille.dto.crossover;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.Accessors;

@Getter
@Setter
@Accessors(chain = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class GetSessionRequest {

    @NotNull
    private String redirectUri;

    @NotNull
    private String dataQr;

    @NotNull
    private String scenario;
}
