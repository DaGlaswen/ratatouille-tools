package ru.sber.apm.aipay.ratatouille.dto.crossover;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
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
public class MerchantInfo {

    @NotNull
    private String pointId;

    @NotBlank
    private String name;

    private String logoUrl;

    private String address;

    private String qrData;

    @Valid
    private InfoWidget infoWidget;

    @Valid
    private Status status;

    @Valid
    private List<Category> category;

    @Getter
    @Setter
    @Accessors(chain = true)
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class InfoWidget {
        private String description;
        private String picture;
    }

    @Getter
    @Setter
    @Accessors(chain = true)
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Status {
        private Boolean active;
        private String reason;
    }
}