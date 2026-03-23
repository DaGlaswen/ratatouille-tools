package ru.sber.apm.aipay.ratatouille.dto.crossover;

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
public class OrderListResponse {

    @Valid
    private List<Order> orders;

    @Valid
    private Pagination pagination;

    @Getter
    @Setter
    @Accessors(chain = true)
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Pagination {
        private Integer currentPage;
        private Integer totalPages;
        private Integer totalItems;
        private Integer itemsPerPage;
    }
}