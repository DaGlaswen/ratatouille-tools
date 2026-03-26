package ru.sber.apm.aipay.ratatouille.dto.crossover;

import jakarta.validation.constraints.Min;
import lombok.*;
import lombok.experimental.Accessors;
import java.util.UUID;

@Getter
@Setter
@Accessors(chain = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Category {

    private String id;

    private String name;

    @Min(0)
    private Integer size;
}