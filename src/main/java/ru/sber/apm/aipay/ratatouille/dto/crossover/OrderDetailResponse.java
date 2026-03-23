package ru.sber.apm.aipay.ratatouille.dto.crossover;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.Accessors;
import java.util.List;

@Getter
@Setter
@Accessors(chain = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderDetailResponse {

    @Valid
    private List<OrderItem> products;

    @Getter
    @Setter
    @Accessors(chain = true)
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrderItem {
        @NotNull
        @Min(1)
        private Integer quantity;

        @NotNull
        @Valid
        private Product product;
    }
}