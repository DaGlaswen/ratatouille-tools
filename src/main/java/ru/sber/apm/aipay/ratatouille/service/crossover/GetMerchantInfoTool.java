package ru.sber.apm.aipay.ratatouille.service.crossover;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.mcp.annotation.McpTool;
import org.springframework.ai.mcp.annotation.McpToolParam;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import ru.sber.apm.aipay.ratatouille.dto.crossover.CrossoverHeaders;
import ru.sber.apm.aipay.ratatouille.dto.crossover.MerchantInfo;
import ru.sber.apm.aipay.ratatouille.util.crossover.CrossoverConstants;

@Service
public class GetMerchantInfoTool {

    private static final Logger logger = LoggerFactory.getLogger(GetMerchantInfoTool.class);

    private final RestClient restClient;

    public GetMerchantInfoTool(RestClient crossoverRestClient) {
        this.restClient = crossoverRestClient;
    }

    @McpTool(description = "Получить информацию о партнере по его ID в системе 2GIS")
    public MerchantInfo getMerchantInfo(
            @McpToolParam(description = "ID партнера в системе 2GIS (обязательный)") String extBranchId,
            @McpToolParam(description = "API ключ партнера") String apiKey,
            @McpToolParam(description = "Уникальный идентификатор запроса (по умолчанию генерируется автоматически)", required = false) String rqUID,
            @McpToolParam(description = "ID сессии на фронтенде (опционально)", required = false) String localSessionId) {

        var headers = CrossoverHeaders.builder()
                .authorization(apiKey)
                .timestamp(java.time.Instant.now().toString())
                .rqUID(rqUID != null ? rqUID : java.util.UUID.randomUUID().toString())
                .localSessionId(localSessionId)
                .build();

        logger.info("Запрос информации о партнере: extBranchId={}, rqUID={}", extBranchId, headers.getRqUID());

        MerchantInfo response = restClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path(CrossoverConstants.ENDPOINT_MERCHANT)
                        .queryParam(CrossoverConstants.PARAM_EXT_BRANCH_ID, extBranchId)
                        .build())
                .header(CrossoverConstants.HEADER_AUTHORIZATION, headers.getAuthorization())
                .header(CrossoverConstants.HEADER_TIMESTAMP, headers.getTimestamp())
                .header(CrossoverConstants.HEADER_RQ_UID, headers.getRqUID())
                .header(CrossoverConstants.HEADER_LOCAL_SESSION_ID, headers.getLocalSessionId())
                .retrieve()
                .body(MerchantInfo.class);

        logger.info("Ответ от партнера: pointId={}, name={}, status.active={}",
                response != null ? response.getPointId() : null,
                response != null ? response.getName() : null,
                response != null && response.getStatus() != null ? response.getStatus().getActive() : null);

        return response;
    }
}