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
import ru.sber.apm.aipay.ratatouille.dto.crossover.OrderListResponse;
import ru.sber.apm.aipay.ratatouille.exception.crossover.CrossoverApiException;
import ru.sber.apm.aipay.ratatouille.util.crossover.CrossoverConstants;
import ru.sber.apm.aipay.ratatouille.util.crossover.CrossoverValidationUtil;

import java.util.UUID;

import static org.springframework.util.StringUtils.truncate;

@Service
public class GetOrderListTool {

    private static final Logger logger = LoggerFactory.getLogger(GetOrderListTool.class);

    private final RestClient restClient;
    private final CrossoverApiProperties crossoverApiProperties;

    public GetOrderListTool(RestClient crossoverRestClient, CrossoverApiProperties crossoverApiProperties) {
        this.restClient = crossoverRestClient;
        this.crossoverApiProperties = crossoverApiProperties;
    }

    @McpTool(description = "Получить историю покупок клиента с поддержкой пагинации")
    public OrderListResponse getOrderList(
            @McpToolParam(description = "UUID ID клиента (обязательный)") String subId,
            @McpToolParam(description = "UUID ID партнера (опционально)", required = false) String extBranchId,
            @McpToolParam(description = "Номер страницы (по умолчанию 1)", required = false) Integer page,
            @McpToolParam(description = "Количество записей на странице (1-100, по умолчанию 20)", required = false) Integer limit,
            @McpToolParam(description = "Уникальный идентификатор запроса (по умолчанию генерируется автоматически)", required = false) String rqUID,
            @McpToolParam(description = "ID сессии на фронтенде (опционально)", required = false) String localSessionId) {

        try {
            var parsedSubId = CrossoverValidationUtil.requireValidUuid(subId, "subId");
            UUID parsedExtBranchId = extBranchId != null ? CrossoverValidationUtil.parseUuidSafe(extBranchId) : null;

            // Валидация пагинации
            CrossoverValidationUtil.validatePagination(
                    page != null ? page : CrossoverConstants.DEFAULT_PAGE,
                    limit != null ? limit : CrossoverConstants.DEFAULT_LIMIT);

            var headers = CrossoverHeaders.builder()
                    .authorization("Bearer " + crossoverApiProperties.getApiKey())
                    .timestamp(java.time.Instant.now().toString())
                    .rqUID(rqUID != null ? rqUID : java.util.UUID.randomUUID().toString())
                    .localSessionId(localSessionId)
                    .build();

            logger.info("Запрос истории заказов: subId={}, extBranchId={}, page={}, limit={}",
                    parsedSubId, parsedExtBranchId, page, limit);

            OrderListResponse response = restClient.get()
                    .uri(uriBuilder -> {
                        uriBuilder.path(CrossoverConstants.ENDPOINT_ORDER_LIST)
                                .queryParam(CrossoverConstants.PARAM_SUB_ID, parsedSubId)
                                .queryParam(CrossoverConstants.PARAM_PAGE, page != null ? page : CrossoverConstants.DEFAULT_PAGE)
                                .queryParam(CrossoverConstants.PARAM_LIMIT, limit != null ? limit : CrossoverConstants.DEFAULT_LIMIT);

                        if (parsedExtBranchId != null) {
                            uriBuilder.queryParam(CrossoverConstants.PARAM_EXT_BRANCH_ID, parsedExtBranchId);
                        }
                        return uriBuilder.build();
                    })
                    .header(CrossoverConstants.HEADER_AUTHORIZATION, headers.getAuthorization())
                    .header(CrossoverConstants.HEADER_TIMESTAMP, headers.getTimestamp())
                    .header(CrossoverConstants.HEADER_RQ_UID, headers.getRqUID())
                    .header(CrossoverConstants.HEADER_LOCAL_SESSION_ID, headers.getLocalSessionId())
                    .retrieve()
                    .body(OrderListResponse.class);

            if (response == null) {
                throw CrossoverApiException.notFound("Партнер", extBranchId);
            }
            
            logger.info("Ответ истории заказов: найдено {} заказов, страница {}/{}, всего {} записей",
                    response.getOrders() != null ? response.getOrders().size() : 0,
                    response.getPagination() != null ? response.getPagination().getCurrentPage() : 1,
                    response.getPagination() != null ? response.getPagination().getTotalPages() : 1,
                    response.getPagination() != null ? response.getPagination().getTotalItems() : 0);

            return response;
        } catch (CrossoverApiException e) {
            // Пробрасываем наши кастомные исключения дальше
            throw e;

        } catch (RestClientResponseException e) {
            // Обработка HTTP ошибок от API (4xx, 5xx)
            HttpStatusCode statusCode = e.getStatusCode();
            String responseBody = e.getResponseBodyAsString();

            logger.error("HTTP ошибка от Crossover API: status={}, body={}", statusCode, truncate(responseBody));

            switch (statusCode.value()) {
                case 400 -> throw CrossoverApiException.badRequest("Неверные параметры запроса: " + responseBody);
                case 401 -> throw CrossoverApiException.unauthorized("Неверный apiKey");
                case 403 -> throw CrossoverApiException.forbidden("Доступ запрещён: " + responseBody);
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
            // Ошибки соединения (nginx down, timeout, DNS)
            String causeMessage = e.getCause() != null ? e.getCause().getMessage() : e.getMessage();
            logger.error("Ошибка соединения с Crossover API: {}", causeMessage, e);

            if (causeMessage != null && causeMessage.toLowerCase().contains("timeout")) {
                throw CrossoverApiException.timeoutError("Таймаут соединения с Crossover API");
            }
            if (causeMessage != null && causeMessage.toLowerCase().contains("connection refused")) {
                throw CrossoverApiException.connectionError("Сервис Crossover недоступен (nginx down)", e);
            }
            if (causeMessage != null && causeMessage.toLowerCase().contains("ssl")) {
                throw CrossoverApiException.connectionError("SSL ошибка при соединении с Crossover API", e);
            }

            throw CrossoverApiException.connectionError("Ошибка соединения с Crossover API: " + causeMessage, e);

        } catch (IllegalArgumentException e) {
            // Ошибки валидации URI, параметров
            logger.warn("Ошибка валидации параметров: {}", e.getMessage());
            throw CrossoverApiException.badRequest("Ошибка валидации: " + e.getMessage());

        } catch (Exception e) {
            // Все остальные неожиданные ошибки
            logger.error("Неожиданная ошибка при получении информации о партнере: {}", e.getMessage(), e);
            throw CrossoverApiException.internalError("Неожиданная ошибка: " + e.getMessage(), e);
        }
    }
}