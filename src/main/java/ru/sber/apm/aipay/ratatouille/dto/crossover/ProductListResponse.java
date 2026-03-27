package ru.sber.apm.aipay.ratatouille.dto.crossover;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.Valid;
import lombok.*;
import lombok.experimental.Accessors;

import java.util.List;

@Getter
@Setter
@Accessors(chain = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ProductListResponse {

    @Valid
    private List<Product> products;

    @Valid
    private ProductListResponse.Pagination pagination;

    @Getter
    @Setter
    @Accessors(chain = true)
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class Pagination {
        private Integer currentPage;
        private Integer totalPages;
        private Integer totalItems;
        private Integer itemsPerPage;
    }
}
