package ru.sber.apm.aipay.ratatouille.service.crossover;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.mcp.annotation.McpTool;
import org.springframework.ai.mcp.annotation.McpToolParam;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import ru.sber.apm.aipay.ratatouille.dto.crossover.CrossoverHeaders;
import ru.sber.apm.aipay.ratatouille.dto.crossover.ProductDetail;
import ru.sber.apm.aipay.ratatouille.util.crossover.CrossoverConstants;
import ru.sber.apm.aipay.ratatouille.util.crossover.CrossoverValidationUtil;

@Service
public class GetProductDetailTool {

    private static final Logger logger = LoggerFactory.getLogger(GetProductDetailTool.class);

    private final RestClient restClient;

    public GetProductDetailTool(RestClient crossoverRestClient) {
        this.restClient = crossoverRestClient;
    }

    @McpTool(description = "Получить детальную информацию о товаре по его UUID")
    public ProductDetail getProductDetail(
            @McpToolParam(description = "UUID товара (обязательный)") String productId,
            @McpToolParam(description = "API ключ партнера") String apiKey,
            @McpToolParam(description = "Уникальный идентификатор запроса (по умолчанию генерируется автоматически)", required = false) String rqUID,
            @McpToolParam(description = "ID сессии на фронтенде (опционально)", required = false) String localSessionId) {

        var parsedProductId = CrossoverValidationUtil.requireValidUuid(productId, "productId");

        var headers = CrossoverHeaders.builder()
                .apiKey(apiKey)
                .timestamp(java.time.Instant.now().toString())
                .rqUID(rqUID != null ? rqUID : java.util.UUID.randomUUID().toString())
                .localSessionId(localSessionId)
                .build();

        logger.info("Запрос детали товара: productId={}", parsedProductId);

        ProductDetail response = restClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path(CrossoverConstants.ENDPOINT_PRODUCT_DETAIL)
                        .build(parsedProductId.toString()))
                .header(CrossoverConstants.HEADER_API_KEY, headers.getApiKey())
                .header(CrossoverConstants.HEADER_TIMESTAMP, headers.getTimestamp())
                .header(CrossoverConstants.HEADER_RQ_UID, headers.getRqUID())
                .header(CrossoverConstants.HEADER_LOCAL_SESSION_ID, headers.getLocalSessionId())
                .retrieve()
                .body(ProductDetail.class);

        logger.info("Ответ детали товара: productId={}, name={}, price={} коп.",
                response != null ? response.getId() : null,
                response != null ? response.getName() : null,
                response != null ? response.getPrice() : null);

        return response;
    }
}