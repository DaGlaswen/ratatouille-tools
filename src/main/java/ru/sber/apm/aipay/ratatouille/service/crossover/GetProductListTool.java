package ru.sber.apm.aipay.ratatouille.service.crossover;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.mcp.annotation.McpTool;
import org.springframework.ai.mcp.annotation.McpToolParam;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import ru.sber.apm.aipay.ratatouille.dto.crossover.CrossoverHeaders;
import ru.sber.apm.aipay.ratatouille.dto.crossover.ProductListResponse;
import ru.sber.apm.aipay.ratatouille.util.crossover.CrossoverConstants;
import ru.sber.apm.aipay.ratatouille.util.crossover.CrossoverValidationUtil;

import java.util.UUID;

@Service
public class GetProductListTool {

    private static final Logger logger = LoggerFactory.getLogger(GetProductListTool.class);

    private final RestClient restClient;

    public GetProductListTool(RestClient crossoverRestClient) {
        this.restClient = crossoverRestClient;
    }

    // TODO try catch или exception handler
    @McpTool(description = "Получить каталог товаров партнера с поддержкой пагинации и фильтрации по категории")
    public ProductListResponse getProductList(
            @McpToolParam(description = "ID партнера в кроссовере (обязательный)") String pointId,
            @McpToolParam(description = "Номер страницы (по умолчанию 1)", required = false) Integer page,
            @McpToolParam(description = "Количество товаров на странице (1-100, по умолчанию 20)", required = false) Integer limit,
            @McpToolParam(description = "ID категории для фильтрации товаров (опционально)", required = false) String categoryId,
            @McpToolParam(description = "API ключ партнера") String apiKey,
            @McpToolParam(description = "Уникальный идентификатор запроса (по умолчанию генерируется автоматически)", required = false) String rqUID,
            @McpToolParam(description = "ID сессии на фронтенде (опционально)", required = false) String localSessionId) {

        // Валидация пагинации
        CrossoverValidationUtil.validatePagination(
                page != null ? page : CrossoverConstants.DEFAULT_PAGE,
                limit != null ? limit : CrossoverConstants.DEFAULT_LIMIT);

        var headers = CrossoverHeaders.builder()
                .authorization(apiKey)
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

        logger.info("Ответ каталога товаров: найдено {} товаров, страница {}/{}, всего {} записей",
                response != null && response.getProducts() != null ? response.getProducts().size() : 0,
                response != null && response.getPagination() != null ? response.getPagination().getCurrentPage() : 1,
                response != null && response.getPagination() != null ? response.getPagination().getTotalPages() : 1,
                response != null && response.getPagination() != null ? response.getPagination().getTotalItems() : 0);

        return response;
    }
}