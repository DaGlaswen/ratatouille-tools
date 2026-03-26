package ru.sber.apm.aipay.ratatouille.dto.crossover;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;

import java.util.UUID;

@Getter
@Setter
@Accessors(chain = true)
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class Product {

    @NotNull
    private String id;

    @NotBlank
    private String name;

    @NotNull
    @Min(0)
    private Long price;

    private String imageUrl;
}