package ru.sber.apm.aipay.ratatouille.dto.crossover;

import com.fasterxml.jackson.annotation.JsonInclude;
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
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CartItem {

    @NotNull
    private String productId;

    private String name;

    private String imageUrl;

    @NotNull
    @Min(0)
    private Long price;

    @NotNull
    @Min(1)
    private Integer quantity;
}