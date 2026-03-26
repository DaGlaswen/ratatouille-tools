package ru.sber.apm.aipay.ratatouille.service.p2p;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.mcp.annotation.McpTool;
import org.springframework.ai.mcp.annotation.McpToolParam;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;
import ru.sber.apm.aipay.ratatouille.dto.p2pcrypto.LinkHeaders;
import ru.sber.apm.aipay.ratatouille.dto.p2pcrypto.TransactionHistoryResponse;
import ru.sber.apm.aipay.ratatouille.exception.p2pcrypto.LinkApiException;
import ru.sber.apm.aipay.ratatouille.util.p2pcrypto.LinkConstants;

@Service
public class GetUserHistoryTool {

    private static final Logger logger = LoggerFactory.getLogger(GetUserHistoryTool.class);

    private final RestClient restClient;

    public GetUserHistoryTool(RestClient linkRestClient) {
        this.restClient = linkRestClient;
    }

    @McpTool(description = "Получить общую историю транзакций пользователя с пагинацией")
    public TransactionHistoryResponse getUserHistory(
            @McpToolParam(description = "UUID пользователя") String agentUserID,
            @McpToolParam(description = "Дата последней записи для пагинации (не заполняется при первом вызове)", required = false) String lastCreated,
            @McpToolParam(description = "ID последней записи для пагинации (не заполняется при первом вызове)", required = false) String lastRecordId,
            @McpToolParam(description = "Размер страницы пагинации", required = false) String pageSize,
            @McpToolParam(description = "Уникальный идентификатор запроса (по умолчанию генерируется автоматически)", required = false) String rquid) {

        var headers = LinkHeaders.of(agentUserID, rquid);

        logger.info("Запрос общей истории пользователя: lastRecordId={}, lastCreated={}, rquid={}",
                lastRecordId, lastCreated, headers.getRquid());

        try {
            TransactionHistoryResponse response = restClient.get()
                    .uri(uriBuilder -> {
                        uriBuilder.path(LinkConstants.ENDPOINT_USER_HISTORY);
                        
                        if (lastCreated != null && !lastCreated.isBlank()) {
                            uriBuilder.queryParam("last_created", lastCreated);
                        }
                        if (lastRecordId != null && !lastRecordId.isBlank()) {
                            uriBuilder.queryParam("last_record_id", lastRecordId);
                        }
                        if (pageSize != null && !pageSize.isBlank()) {
                            uriBuilder.queryParam("page_size", pageSize);
                        }
                        return uriBuilder.build();
                    })
                    .header(LinkConstants.HEADER_RQUID, headers.getRquid())
                    .header(LinkConstants.AGENT_USER_ID, headers.getAgentUserID())
                    .retrieve()
                    .body(TransactionHistoryResponse.class);

            logger.info("Получена общая история пользователя: recordCount={}, hasMore={}",
                    response != null && response.getTransactionHistoryRecords() != null ? response.getTransactionHistoryRecords().size() : 0,
                    response != null && response.getIsExist() != null ? response.getIsExist() : false);

            return response;

        } catch (LinkApiException e) {
            throw e;

        } catch (RestClientResponseException e) {
            HttpStatusCode statusCode = e.getStatusCode();
            String responseBody = e.getResponseBodyAsString();

            logger.error("HTTP ошибка от LINK API при получении общей истории: status={}, body={}",
                    statusCode, truncate(responseBody));

            switch (statusCode.value()) {
                case 400 -> throw LinkApiException.badRequest("Ошибка валидации: " + responseBody);
                case 401 -> throw LinkApiException.unauthorized("Требуется авторизация (Bearer токен)");
                case 403 -> throw LinkApiException.forbidden("Недостаточно прав для получения истории");
                case 404 -> throw LinkApiException.notFound("Пользователь", agentUserID);
                case 429 -> throw LinkApiException.rateLimitExceeded("Превышен лимит запросов");
                case 500 -> throw LinkApiException.internalError("Внутренняя ошибка сервера LINK API", e);
                case 502 -> throw LinkApiException.gatewayError("Ошибка шлюза LINK API");
                case 503 -> throw LinkApiException.serviceUnavailable("Сервис истории временно недоступен");
                case 504 -> throw LinkApiException.timeoutError("Таймаут ответа от LINK API");
                default -> throw LinkApiException.internalError(
                        "Неожиданная HTTP ошибка при получении общей истории: " + statusCode, e);
            }

        } catch (ResourceAccessException e) {
            String causeMessage = e.getCause() != null ? e.getCause().getMessage() : e.getMessage();
            logger.error("Ошибка соединения с LINK API: {}", causeMessage, e);

            if (causeMessage != null && causeMessage.toLowerCase().contains("timeout")) {
                throw LinkApiException.timeoutError("Таймаут соединения с LINK API");
            }
            if (causeMessage != null && causeMessage.toLowerCase().contains("connection refused")) {
                throw LinkApiException.connectionError("Сервис LINK недоступен (nginx down)", e);
            }

            throw LinkApiException.connectionError("Ошибка соединения с LINK API: " + causeMessage, e);

        } catch (IllegalArgumentException e) {
            logger.warn("Ошибка валидации параметров: {}", e.getMessage());
            throw LinkApiException.badRequest("Ошибка валидации: " + e.getMessage());

        } catch (Exception e) {
            logger.error("Неожиданная ошибка при получении общей истории: {}", e.getMessage(), e);
            throw LinkApiException.internalError("Неожиданная ошибка: " + e.getMessage(), e);
        }
    }

    private String truncate(String str) {
        if (str == null) return "null";
        return str.length() > 200 ? str.substring(0, 200) + "..." : str;
    }
}
