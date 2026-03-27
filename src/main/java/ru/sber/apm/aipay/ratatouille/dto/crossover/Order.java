package ru.sber.apm.aipay.ratatouille.dto.crossover;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.*;
import lombok.experimental.Accessors;

@Getter
@Setter
@Accessors(chain = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Order {

    private String orderId;

    private String orderTime;

    @Valid
    private Order.MerchantInfo merchantInfo;

    private String verificationCode;

    private String status;

    @Min(0)
    private Integer totalItems;

    @Min(0)
    private Long totalAmount;

    private String description;

    private String localSessionId;

    @Getter
    @Setter
    @Accessors(chain = true)
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class MerchantInfo {
        private String pointId;
        private String name;
        private String logoUrl;
        private String address;
    }
}
