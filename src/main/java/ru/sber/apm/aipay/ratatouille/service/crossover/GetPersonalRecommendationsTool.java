package ru.sber.apm.aipay.ratatouille.service.crossover;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.mcp.annotation.McpTool;
import org.springframework.ai.mcp.annotation.McpToolParam;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;
import ru.sber.apm.aipay.ratatouille.config.crossover.CrossoverApiProperties;
import ru.sber.apm.aipay.ratatouille.dto.crossover.CrossoverHeaders;
import ru.sber.apm.aipay.ratatouille.dto.crossover.PersonalRecommendationsRequest;
import ru.sber.apm.aipay.ratatouille.dto.crossover.PersonalRecommendationsResponse;
import ru.sber.apm.aipay.ratatouille.exception.crossover.CrossoverApiException;
import ru.sber.apm.aipay.ratatouille.util.Utils;
import ru.sber.apm.aipay.ratatouille.util.crossover.CrossoverConstants;
import ru.sber.apm.aipay.ratatouille.util.crossover.CrossoverValidationUtil;

import java.util.UUID;

import static org.springframework.util.StringUtils.truncate;

/**
 * MCP-инструмент: получить персональные рекомендации Crossover.
 * POST /crossover/v1/recommendations/personal
 *
 * Архитектура запроса:
 *   MCP Client → ratatouille-tools (POST body + SDK-Headers)
 *   → Crossover Gateway (/crossover/v1/recommendations/personal)
 *   → Response: products[] + pagination
 *
 * Рекомендации привязаны к точке продаж (pointId), пользователю (subId)
 * и сессии (sessionId). sessionId — не http-сессия, а аналитический
 * идентификатор для персонализации рекомендаций.
 *
 * На клиенте каждый товар получает categoryId: "personal-recommendations"
 * (плейсхолдер, не поле от API) для отличия от обычных категорий.
 *
 * @see <a href="https://docs.openclaw.ai">OpenClaw Docs</a>
 * @see <a href="https://clawhub.com">ClawHub</a>
 */
@Service
public class GetPersonalRecommendationsTool {

    private static final Logger logger = LoggerFactory.getLogger(GetPersonalRecommendationsTool.class);

    private final RestClient restClient;
    private final CrossoverApiProperties crossoverApiProperties;

    public GetPersonalRecommendationsTool(RestClient crossoverRestClient,
                                          CrossoverApiProperties crossoverApiProperties) {
        this.restClient = crossoverRestClient;
        this.crossoverApiProperties = crossoverApiProperties;
    }

    @McpTool(description = "Получить персональные рекомендации товаров для пользователя на точке продаж Crossover. " +
            "Возвращает список рекомендованных продуктов на основе истории покупок и контекста. " +
            "Каждый товар содержит id, name, price, imageUrl, unitOfMeasurement. " +
            "Товары без валидных id/name/price отбрасываются на клиенте.")
    public PersonalRecommendationsResponse getPersonalRecommendations(
            @McpToolParam(description = "UUID точки продаж (витрины) в Crossover, формат UUID v4 (обязательный)") String pointId,
            @McpToolParam(description = "Идентификатор пользователя в системе (обязательный, персонализация)") String subId,
            @McpToolParam(description = "Сессия рекомендаций: строка 4–128 символов, только [0-9A-Za-z_-]", required = false) String sessionId,
            @McpToolParam(description = "Номер страницы (1..500, по умолчанию 1)", required = false) Integer page,
            @McpToolParam(description = "Количество позиций (1..99, по умолчанию 28)", required = false) Integer limit,
            @McpToolParam(description = "Уникальный идентификатор запроса (генерируется автоматически, если не указан)", required = false) String rqUID,
            @McpToolParam(description = "ID сессии на фронтенде (опционально)", required = false) String localSessionId) {

        String effectiveRqUID = rqUID != null ? rqUID : UUID.randomUUID().toString();

        try {
            // Валидация обязательных полей
            CrossoverValidationUtil.requireValidUuid(pointId, "pointId");
//            CrossoverValidationUtil.requireValidUuid(subId, "subId");
            if (sessionId == null || sessionId.isBlank()) {
                // Генерируем sessionId автоматически, если не передан
                sessionId = String.valueOf(System.currentTimeMillis());
            }
            CrossoverValidationUtil.validateRecommendationsSessionId(sessionId);

            // Валидация пагинации (рекомендации: page 1..500, limit 1..99)
            CrossoverValidationUtil.validateRecommendationsPagination(page, limit);

            int effectivePage = page != null ? page : CrossoverConstants.DEFAULT_PAGE;
            int effectiveLimit = limit != null ? limit : CrossoverConstants.DEFAULT_RECOMMENDATIONS_LIMIT;

            var headers = CrossoverHeaders.builder()
                    .authorization(crossoverApiProperties.getApiKey())
                    .timestamp(Utils.getCurrentTimestamp())
                    .rqUID(effectiveRqUID)
                    .localSessionId(localSessionId)
                    .build();

            var requestBody = PersonalRecommendationsRequest.builder()
                    .pointId(pointId.toLowerCase())
                    .subId(subId)
                    .sessionId(sessionId.trim().substring(0, Math.min(sessionId.trim().length(), CrossoverConstants.MAX_SESSION_ID_LENGTH)))
                    .page(effectivePage)
                    .limit(effectiveLimit)
                    .build();

            logger.info("Запрос персональных рекомендаций: pointId={}, subId={}, sessionId={}, page={}, limit={}, rqUID={}",
                    pointId, subId, sessionId, effectivePage, effectiveLimit, effectiveRqUID);

            PersonalRecommendationsResponse response = restClient.post()
                    .uri(CrossoverConstants.ENDPOINT_RECOMMENDATIONS_PERSONAL)
                    .header(CrossoverConstants.HEADER_AUTHORIZATION, headers.getAuthorization())
                    .header(CrossoverConstants.HEADER_TIMESTAMP, headers.getTimestamp())
                    .header(CrossoverConstants.HEADER_RQ_UID, headers.getRqUID())
                    .header(CrossoverConstants.HEADER_LOCAL_SESSION_ID, headers.getLocalSessionId())
                    .body(requestBody)
                    .retrieve()
                    .body(PersonalRecommendationsResponse.class);

            if (response == null) {
                throw CrossoverApiException.notFound("Персональные рекомендации для пользователя", subId);
            }

            int productsCount = response.getProducts() != null ? response.getProducts().size() : 0;
            logger.info("Ответ персональных рекомендаций: найдено {} позиций, страница {}/{}, rqUID={}",
                    productsCount,
                    response.getPagination() != null ? response.getPagination().getCurrentPage() : effectivePage,
                    response.getPagination() != null ? response.getPagination().getTotalPages() : "?",
                    effectiveRqUID);

            return response;

        } catch (CrossoverApiException e) {
            throw e;

        } catch (RestClientResponseException e) {
            HttpStatusCode statusCode = e.getStatusCode();
            String responseBody = e.getResponseBodyAsString();

            logger.error("HTTP ошибка от Crossover API (recommendations): status={}, body={}, rqUID={}",
                    statusCode, truncate(responseBody, 500), effectiveRqUID);

            switch (statusCode.value()) {
                case 400 -> throw CrossoverApiException.badRequest("Неверные параметры запроса: " + responseBody);
                case 401 -> throw CrossoverApiException.unauthorized("Неверный apiKey");
                case 403 -> throw CrossoverApiException.forbidden("Доступ запрещён: " + responseBody);
                case 404 -> throw CrossoverApiException.notFound("Точка или пользователь не найдены", subId);
                case 409 -> throw CrossoverApiException.conflict("Конфликт запроса: " + responseBody);
                case 429 -> throw CrossoverApiException.rateLimitExceeded("Превышен лимит запросов");
                case 500 -> throw CrossoverApiException.internalError("Внутренняя ошибка сервера Crossover", e);
                case 502 -> throw CrossoverApiException.gatewayError("Ошибка шлюза Crossover API");
                case 503 -> throw CrossoverApiException.serviceUnavailable("Сервис Crossover временно недоступен");
                case 504 -> throw CrossoverApiException.timeoutError("Таймаут ответа от Crossover API");
                default -> throw CrossoverApiException.internalError(
                        "Неожиданная HTTP ошибка от Crossover API: " + statusCode, e);
            }

        } catch (ResourceAccessException e) {
            String causeMessage = e.getCause() != null ? e.getCause().getMessage() : e.getMessage();
            logger.error("Ошибка соединения с Crossover API (recommendations): {}, rqUID={}", causeMessage, effectiveRqUID, e);

            if (causeMessage != null && causeMessage.toLowerCase().contains("timeout")) {
                throw CrossoverApiException.timeoutError("Таймаут соединения с Crossover API");
            }
            if (causeMessage != null && causeMessage.toLowerCase().contains("connection refused")) {
                throw CrossoverApiException.connectionError("Сервис Crossover недоступен", e);
            }
            if (causeMessage != null && causeMessage.toLowerCase().contains("ssl")) {
                throw CrossoverApiException.connectionError("SSL ошибка при соединении с Crossover API", e);
            }

            throw CrossoverApiException.connectionError("Ошибка соединения с Crossover API: " + causeMessage, e);

        } catch (IllegalArgumentException e) {
            logger.warn("Ошибка валидации параметров (recommendations): {}, rqUID={}", e.getMessage(), effectiveRqUID);
            throw CrossoverApiException.badRequest("Ошибка валидации: " + e.getMessage());

        } catch (Exception e) {
            logger.error("Неожиданная ошибка при получении персональных рекомендаций: {}, rqUID={}", e.getMessage(), effectiveRqUID, e);
            throw CrossoverApiException.internalError("Неожиданная ошибка: " + e.getMessage(), e);
        }
    }
}
