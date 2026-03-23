package ru.sber.apm.aipay.ratatouille.dto.crossover;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.Accessors;

import java.util.UUID;

@Getter
@Setter
@Accessors(chain = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CartItem {

    @NotNull
    @org.hibernate.validator.constraints.UUID
    private UUID productId;

    private String name;

    private String imageUrl;

    @NotNull
    @Min(0)
    private Long price;

    @NotNull
    @Min(1)
    private Integer quantity;
}