package ru.sber.apm.aipay.ratatouille.dto.crossover;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;

import java.util.List;

/**
 * DTO для ответа /crossover/v1/recommendations/personal
 * Персональные рекомендации Crossover.
 */
@Getter
@Setter
@Accessors(chain = true)
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PersonalRecommendationsResponse {

    private List<ProductRec> products;
    private Pagination pagination;

    /**
     * Элемент персональной рекомендации (расширенный Product).
     */
    @Getter
    @Setter
    @Accessors(chain = true)
    @SuperBuilder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class ProductRec {
        /** ID товара в каталоге точки (обязателен для UI) */
        private String id;
        /** Название (обязателен для UI) */
        private String name;
        /** Цена; в UI берётся Math.floor(price) */
        private Long price;
        /** Опционально, картинка */
        private String imageUrl;
        /** Опционально, единица измерения (шт, мл и т.д.) */
        private String unitOfMeasurement;
        /** Опционально; тип рекомендации от шлюза (в клиенте не используется) */
        private String recType;
    }

    /**
     * Стандартная пагинация.
     */
    @Getter
    @Setter
    @Accessors(chain = true)
    @SuperBuilder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class Pagination {
        private Integer totalPages;
        private Integer currentPage;
        private Integer totalItems;
        private Integer itemsPerPage;
    }
}
