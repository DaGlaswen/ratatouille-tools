package ru.sber.apm.aipay.ratatouille.dto.crossover;

import lombok.*;
import lombok.experimental.Accessors;

@Getter
@Setter
@Accessors(chain = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ErrorResponse {

    private Integer errorCode;

    private String error;

    private String message;
}