package ru.sber.apm.aipay.ratatouille.dto.crossover;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;

import java.util.List;

/**
 * DTO для запроса персональных рекомендаций Crossover.
 * POST /crossover/v1/recommendations/personal
 *
 * Сервер принимает pointId, subId, sessionId (обязательные) и page, limit (опциональные).
 * Рекомендации привязаны к точке продаж (pointId) и пользователю (subId)
 * через сессию рекомендаций (sessionId).
 */
@Getter
@Setter
@Accessors(chain = true)
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PersonalRecommendationsRequest {

    /** UUID точки продаж (обязательный, идентификатор витрины) */
    private String pointId;

    /** Идентификатор пользователя в вашей системе (обязательный, персонализация) */
    private String subId;

    /** Сессия для рекомендаций: 4–128 символов, только [0-9A-Za-z_-] (обязательный) */
    private String sessionId;

    /** Номер страницы; 1…500 (по умолчанию 1) */
    private Integer page;

    /** Кол-во позиций; 1…99 (по умолчанию 28) */
    private Integer limit;
}