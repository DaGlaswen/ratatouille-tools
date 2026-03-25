package ru.sber.apm.aipay.ratatouille.service.crossover;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.mcp.annotation.McpTool;
import org.springframework.ai.mcp.annotation.McpToolParam;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import ru.sber.apm.aipay.ratatouille.dto.crossover.CrossoverHeaders;
import ru.sber.apm.aipay.ratatouille.dto.crossover.OrderListResponse;
import ru.sber.apm.aipay.ratatouille.util.crossover.CrossoverConstants;
import ru.sber.apm.aipay.ratatouille.util.crossover.CrossoverValidationUtil;

import java.util.UUID;

@Service
public class GetOrderListTool {

    private static final Logger logger = LoggerFactory.getLogger(GetOrderListTool.class);

    private final RestClient restClient;

    public GetOrderListTool(RestClient crossoverRestClient) {
        this.restClient = crossoverRestClient;
    }

    @McpTool(description = "Получить историю покупок клиента с поддержкой пагинации")
    public OrderListResponse getOrderList(
            @McpToolParam(description = "UUID ID клиента (обязательный)") String subId,
            @McpToolParam(description = "UUID ID партнера (опционально)", required = false) String extBranchId,
            @McpToolParam(description = "Номер страницы (по умолчанию 1)", required = false) Integer page,
            @McpToolParam(description = "Количество записей на странице (1-100, по умолчанию 20)", required = false) Integer limit,
            @McpToolParam(description = "API ключ партнера") String apiKey,
            @McpToolParam(description = "Уникальный идентификатор запроса (по умолчанию генерируется автоматически)", required = false) String rqUID,
            @McpToolParam(description = "ID сессии на фронтенде (опционально)", required = false) String localSessionId) {

        var parsedSubId = CrossoverValidationUtil.requireValidUuid(subId, "subId");
        UUID parsedExtBranchId = extBranchId != null ? CrossoverValidationUtil.parseUuidSafe(extBranchId) : null;

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

        logger.info("Ответ истории заказов: найдено {} заказов, страница {}/{}, всего {} записей",
                response != null && response.getOrders() != null ? response.getOrders().size() : 0,
                response != null && response.getPagination() != null ? response.getPagination().getCurrentPage() : 1,
                response != null && response.getPagination() != null ? response.getPagination().getTotalPages() : 1,
                response != null && response.getPagination() != null ? response.getPagination().getTotalItems() : 0);

        return response;
    }
}