package ru.sber.apm.aipay.ratatouille.dto.crossover;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.Accessors;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Accessors(chain = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegCartRequest {

    @NotNull
    private String subId;

    private String partnerClientId;

    private String partnerSessionId;

    @NotNull
    private String orderId;

    @NotNull
    private Integer totalAmount;

    @NotNull
    private String pointId;

    private String comment;

    @Valid
    private List<RegCartRequest.CartItem> items;

    @Getter
    @Setter
    @Accessors(chain = true)
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CartItem {
        @NotNull
        private String productId;

        @NotNull
        @Min(0)
        private Integer quantity;

        @NotNull
        @Min(0)
        private Integer amount;
    }
}
