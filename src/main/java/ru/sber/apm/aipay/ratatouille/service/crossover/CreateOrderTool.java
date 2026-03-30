package ru.sber.apm.aipay.ratatouille.service.crossover;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.mcp.annotation.McpTool;
import org.springframework.ai.mcp.annotation.McpToolParam;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.client.*;
import ru.sber.apm.aipay.ratatouille.config.crossover.CrossoverApiProperties;
import ru.sber.apm.aipay.ratatouille.dto.crossover.CrossoverHeaders;
import ru.sber.apm.aipay.ratatouille.dto.crossover.QR;
import ru.sber.apm.aipay.ratatouille.dto.crossover.RegCartRequest;
import ru.sber.apm.aipay.ratatouille.exception.crossover.CrossoverApiException;
import ru.sber.apm.aipay.ratatouille.util.Utils;
import ru.sber.apm.aipay.ratatouille.util.crossover.CrossoverConstants;

import java.util.List;

import static org.springframework.util.StringUtils.truncate;

@Service
public class CreateOrderTool {

    private static final Logger logger = LoggerFactory.getLogger(CreateOrderTool.class);

    private final RestClient restClient;
    private final CrossoverApiProperties crossoverApiProperties;

    public CreateOrderTool(RestClient crossoverRestClient, CrossoverApiProperties crossoverApiProperties) {
        this.restClient = crossoverRestClient;
        this.crossoverApiProperties = crossoverApiProperties;
    }

    @McpTool(description = "Сформировать заказ по корзине товаров и получить QR-код для оплаты")
    public QR createOrder(
            @McpToolParam(description = "SubID клиента в SberId в Crossover (обязательный)") String subId,
            @McpToolParam(description = "ID заказа в Crossover (обязательный)") String orderId,
            @McpToolParam(description = "ID точки в Crossover (обязательный)") String pointId,
            @McpToolParam(description = "Общая стоимость корзины в копейках (обязательный)") Integer totalAmount,
            @McpToolParam(description = "Список товаров в корзине: каждый элемент содержит productId, quantity, amount", required = false) List<CartItemParam> items,
            @McpToolParam(description = "ID клиента в 2GIS (опционально)", required = false) String partnerClientId,
            @McpToolParam(description = "ID сессии в 2GIS (опционально)", required = false) String partnerSessionId,
            @McpToolParam(description = "Комментарий к заказу (опционально)", required = false) String comment,
            @McpToolParam(description = "Уникальный идентификатор запроса (по умолчанию генерируется автоматически)", required = false) String rqUID,
            @McpToolParam(description = "ID сессии на фронтенде (опционально)", required = false) String localSessionId) {

        String effectiveRqUID = rqUID != null ? rqUID : java.util.UUID.randomUUID().toString();

        try {
            var headers = CrossoverHeaders.builder()
                    .authorization(crossoverApiProperties.getApiKey())
                    .timestamp(Utils.getCurrentTimestamp())
                    .rqUID(effectiveRqUID)
                    .localSessionId(localSessionId)
                    .build();

            var request = RegCartRequest.builder()
                    .subId(subId)
                    .orderId(orderId)
                    .pointId(pointId)
                    .totalAmount(totalAmount)
                    .partnerClientId(partnerClientId)
                    .partnerSessionId(partnerSessionId)
                    .comment(comment)
                    .items(items != null ? items.stream()
                            .map(p -> RegCartRequest.CartItem.builder()
                                    .productId(p.productId())
                                    .quantity(p.quantity())
                                    .amount(p.amount())
                                    .build())
                            .toList() : null)
                    .build();

            logger.info("Создание заказа: orderId={}, subId={}, pointId={}, totalAmount={}, itemsCount={}, rqUID={}",
                    orderId, subId, pointId, totalAmount,
                    request.getItems() != null ? request.getItems().size() : 0,
                    effectiveRqUID);

            QR response = restClient.post()
                    .uri(CrossoverConstants.ENDPOINT_ORDER_CREATE)
                    .header(CrossoverConstants.HEADER_AUTHORIZATION, headers.getAuthorization())
                    .header(CrossoverConstants.HEADER_TIMESTAMP, headers.getTimestamp())
                    .header(CrossoverConstants.HEADER_RQ_UID, headers.getRqUID())
                    .header(CrossoverConstants.HEADER_LOCAL_SESSION_ID, headers.getLocalSessionId())
                    .body(request)
                    .retrieve()
                    .body(QR.class);

            logger.info("Заказ создан: verificationCode={}, rqUID={}",
                    response != null ? response.getVerificationCode() : null,
                    effectiveRqUID);

            return response;
        } catch (CrossoverApiException e) {
            // Пробрасываем наши кастомные исключения дальше
            throw e;

        } catch (RestClientResponseException e) {
            // Обработка HTTP ошибок от API (4xx, 5xx)
            HttpStatusCode statusCode = e.getStatusCode();
            String responseBody = e.getResponseBodyAsString();

            logger.error("HTTP ошибка от Crossover API: status={}, body={}, rqUID={}", statusCode, truncate(responseBody), effectiveRqUID);

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
            logger.error("Ошибка соединения с Crossover API: {}, rqUID={}", causeMessage, effectiveRqUID, e);

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
            logger.warn("Ошибка валидации параметров: {}, rqUID={}", e.getMessage(), effectiveRqUID);
            throw CrossoverApiException.badRequest("Ошибка валидации: " + e.getMessage());

        } catch (Exception e) {
            // Все остальные неожиданные ошибки
            logger.error("Неожиданная ошибка при создании заказа: {}, rqUID={}", e.getMessage(), effectiveRqUID, e);
            throw CrossoverApiException.internalError("Неожиданная ошибка: " + e.getMessage(), e);
        }
    }

    /**
     * Вспомогательный record для передачи товаров в корзине через MCP
     */
    public record CartItemParam(
            String productId,
            Integer quantity,
            Integer amount
    ) {}
}
