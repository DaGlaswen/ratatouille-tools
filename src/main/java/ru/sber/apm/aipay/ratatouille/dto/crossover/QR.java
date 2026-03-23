package ru.sber.apm.aipay.ratatouille.dto.crossover;

import lombok.*;
import lombok.experimental.Accessors;

@Getter
@Setter
@Accessors(chain = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QR {

    private String verificationCode;
}