package ru.sber.apm.aipay.ratatouille.service.crossover;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.mcp.annotation.McpTool;
import org.springframework.ai.mcp.annotation.McpToolParam;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import ru.sber.apm.aipay.ratatouille.dto.crossover.CrossoverHeaders;
import ru.sber.apm.aipay.ratatouille.dto.crossover.QR;
import ru.sber.apm.aipay.ratatouille.dto.crossover.RegCartRequest;
import ru.sber.apm.aipay.ratatouille.util.crossover.CrossoverConstants;

import java.util.List;

@Service
public class CreateOrderTool {

    private static final Logger logger = LoggerFactory.getLogger(CreateOrderTool.class);

    private final RestClient restClient;

    public CreateOrderTool(RestClient crossoverRestClient) {
        this.restClient = crossoverRestClient;
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
            @McpToolParam(description = "API ключ партнера") String apiKey,
            @McpToolParam(description = "Уникальный идентификатор запроса (по умолчанию генерируется автоматически)", required = false) String rqUID,
            @McpToolParam(description = "ID сессии на фронтенде (опционально)", required = false) String localSessionId) {

        var headers = CrossoverHeaders.builder()
                .authorization(apiKey)
                .timestamp(java.time.Instant.now().toString())
                .rqUID(rqUID != null ? rqUID : java.util.UUID.randomUUID().toString())
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
                                .productId(java.util.UUID.fromString(p.productId()))
                                .quantity(p.quantity())
                                .amount(p.amount())
                                .build())
                        .toList() : null)
                .build();

        logger.info("Создание заказа: orderId={}, subId={}, pointId={}, totalAmount={}, itemsCount={}",
                orderId, subId, pointId, totalAmount,
                request.getItems() != null ? request.getItems().size() : 0);

        QR response = restClient.post()
                .uri(CrossoverConstants.ENDPOINT_ORDER_CREATE)
                .header(CrossoverConstants.HEADER_AUTHORIZATION, headers.getAuthorization())
                .header(CrossoverConstants.HEADER_TIMESTAMP, headers.getTimestamp())
                .header(CrossoverConstants.HEADER_RQ_UID, headers.getRqUID())
                .header(CrossoverConstants.HEADER_LOCAL_SESSION_ID, headers.getLocalSessionId())
                .body(request)
                .retrieve()
                .body(QR.class);

        logger.info("Заказ создан: verificationCode={}",
                response != null ? response.getVerificationCode() : null);

        return response;
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