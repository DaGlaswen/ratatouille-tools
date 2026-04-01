package ru.sber.apm.aipay.ratatouille.dto.smartring;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import lombok.experimental.Accessors;

@Getter
@Setter
@Accessors(chain = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ExternalDataHeartRateResponseDto {

    private String date;
    private Integer heartRate;
}