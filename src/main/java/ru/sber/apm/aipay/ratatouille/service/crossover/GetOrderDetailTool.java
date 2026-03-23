package ru.sber.apm.aipay.ratatouille.service.crossover;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.mcp.annotation.McpTool;
import org.springframework.ai.mcp.annotation.McpToolParam;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import ru.sber.apm.aipay.ratatouille.dto.crossover.CrossoverHeaders;
import ru.sber.apm.aipay.ratatouille.dto.crossover.OrderDetailResponse;
import ru.sber.apm.aipay.ratatouille.util.crossover.CrossoverConstants;
import ru.sber.apm.aipay.ratatouille.util.crossover.CrossoverValidationUtil;

import java.util.UUID;

@Service
public class GetOrderDetailTool {

    private static final Logger logger = LoggerFactory.getLogger(GetOrderDetailTool.class);

    private final RestClient restClient;

    public GetOrderDetailTool(RestClient crossoverRestClient) {
        this.restClient = crossoverRestClient;
    }

    @McpTool(description = "Получить детальную информацию о заказе по его ID")
    public OrderDetailResponse getOrderDetail(
            @McpToolParam(description = "ID заказа (обязательный)") String orderId,
            @McpToolParam(description = "UUID ID клиента (обязательный)") String subId,
            @McpToolParam(description = "UUID ID партнера (опционально)", required = false) String extBranchId,
            @McpToolParam(description = "API ключ партнера") String apiKey,
            @McpToolParam(description = "Уникальный идентификатор запроса (по умолчанию генерируется автоматически)", required = false) String rqUID,
            @McpToolParam(description = "ID сессии на фронтенде (опционально)", required = false) String localSessionId) {

        var parsedSubId = CrossoverValidationUtil.requireValidUuid(subId, "subId");
        UUID parsedExtBranchId = extBranchId != null ? CrossoverValidationUtil.parseUuidSafe(extBranchId) : null;

        var headers = CrossoverHeaders.builder()
                .apiKey(apiKey)
                .timestamp(java.time.Instant.now().toString())
                .rqUID(rqUID != null ? rqUID : java.util.UUID.randomUUID().toString())
                .localSessionId(localSessionId)
                .build();

        logger.info("Запрос детали заказа: orderId={}, subId={}, extBranchId={}",
                orderId, parsedSubId, parsedExtBranchId);

        OrderDetailResponse response = restClient.get()
                .uri(uriBuilder -> {
                    var builder = uriBuilder.path(CrossoverConstants.ENDPOINT_ORDER_DETAIL)
                            .queryParam(CrossoverConstants.PARAM_SUB_ID, parsedSubId);

                    if (parsedExtBranchId != null) {
                        builder.queryParam(CrossoverConstants.PARAM_EXT_BRANCH_ID, parsedExtBranchId);
                    }
                    return builder.build(orderId);
                })
                .header(CrossoverConstants.HEADER_API_KEY, headers.getApiKey())
                .header(CrossoverConstants.HEADER_TIMESTAMP, headers.getTimestamp())
                .header(CrossoverConstants.HEADER_RQ_UID, headers.getRqUID())
                .header(CrossoverConstants.HEADER_LOCAL_SESSION_ID, headers.getLocalSessionId())
                .retrieve()
                .body(OrderDetailResponse.class);

        logger.info("Ответ детали заказа: orderId={}, productsCount={}",
                orderId,
                response != null && response.getProducts() != null ? response.getProducts().size() : 0);

        return response;
    }
}