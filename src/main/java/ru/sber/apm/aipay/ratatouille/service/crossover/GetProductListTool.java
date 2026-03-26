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
import ru.sber.apm.aipay.ratatouille.dto.crossover.ProductListResponse;
import ru.sber.apm.aipay.ratatouille.exception.crossover.CrossoverApiException;
import ru.sber.apm.aipay.ratatouille.util.crossover.CrossoverConstants;
import ru.sber.apm.aipay.ratatouille.util.crossover.CrossoverValidationUtil;

import java.util.UUID;

import static org.springframework.util.StringUtils.truncate;

@Service
public class GetProductListTool {

    private static final Logger logger = LoggerFactory.getLogger(GetProductListTool.class);

    private final RestClient restClient;
    private final CrossoverApiProperties crossoverApiProperties;

    public GetProductListTool(RestClient crossoverRestClient, CrossoverApiProperties crossoverApiProperties) {
        this.restClient = crossoverRestClient;
        this.crossoverApiProperties = crossoverApiProperties;
    }

    // TODO try catch или exception handler
    @McpTool(description = "Получить каталог товаров партнера с поддержкой пагинации и фильтрации по категории")
    public ProductListResponse getProductList(
            @McpToolParam(description = "ID партнера в кроссовере (обязательный)") String pointId,
            @McpToolParam(description = "Номер страницы (по умолчанию 1)", required = false) Integer page,
            @McpToolParam(description = "Количество товаров на странице (1-100, по умолчанию 20)", required = false) Integer limit,
            @McpToolParam(description = "ID категории для фильтрации товаров (опционально)", required = false) String categoryId,
            @McpToolParam(description = "Уникальный идентификатор запроса (по умолчанию генерируется автоматически)", required = false) String rqUID,
            @McpToolParam(description = "ID сессии на фронтенде (опционально)", required = false) String localSessionId) {

        try {
            // Валидация пагинации
            CrossoverValidationUtil.validatePagination(
                    page != null ? page : CrossoverConstants.DEFAULT_PAGE,
                    limit != null ? limit : CrossoverConstants.DEFAULT_LIMIT);

            var headers = CrossoverHeaders.builder()
                    .authorization(crossoverApiProperties.getApiKey())
                    .timestamp(java.time.Instant.now().toString())
                    .rqUID(rqUID != null ? rqUID : java.util.UUID.randomUUID().toString())
                    .localSessionId(localSessionId)
                    .build();

            logger.info("Запрос каталога товаров: pointId={}, page={}, limit={}, categoryId={}",
                    pointId, page, limit, categoryId);

            ProductListResponse response = restClient.get()
                    .uri(uriBuilder -> {
                        uriBuilder.path(CrossoverConstants.ENDPOINT_PRODUCT_LIST)
                                .queryParam(CrossoverConstants.PARAM_POINT_ID, pointId)
                                .queryParam(CrossoverConstants.PARAM_PAGE, page != null ? page : CrossoverConstants.DEFAULT_PAGE)
                                .queryParam(CrossoverConstants.PARAM_LIMIT, limit != null ? limit : CrossoverConstants.DEFAULT_LIMIT);

                        if (categoryId != null && !categoryId.isBlank()) {
                            UUID parsedId = CrossoverValidationUtil.parseUuidSafe(categoryId);
                            if (parsedId != null) {
                                uriBuilder.queryParam(CrossoverConstants.PARAM_CATEGORY_ID, parsedId);
                            }
                        }
                        return uriBuilder.build();
                    })
                    .header(CrossoverConstants.HEADER_AUTHORIZATION, headers.getAuthorization())
                    .header(CrossoverConstants.HEADER_TIMESTAMP, headers.getTimestamp())
                    .header(CrossoverConstants.HEADER_RQ_UID, headers.getRqUID())
                    .header(CrossoverConstants.HEADER_LOCAL_SESSION_ID, headers.getLocalSessionId())
                    .retrieve()
                    .body(ProductListResponse.class);

            if (response == null) {
                throw CrossoverApiException.notFound("Список продуктов из точки", pointId);
            }

            logger.info("Ответ каталога товаров: найдено {} товаров, страница {}/{}, всего {} записей",
                    response.getProducts() != null ? response.getProducts().size() : 0,
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
                case 404 -> throw CrossoverApiException.notFound("Точка", pointId);
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