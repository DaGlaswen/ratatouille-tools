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
import ru.sber.apm.aipay.ratatouille.util.p2pcrypto.LinkValidationUtil;

import java.util.UUID;

@Service
public class GetTransactionHistoryTool {

    private static final Logger logger = LoggerFactory.getLogger(GetTransactionHistoryTool.class);

    private final RestClient restClient;

    public GetTransactionHistoryTool(RestClient linkRestClient) {
        this.restClient = linkRestClient;
    }

    @McpTool(description = "Получить историю транзакций по кошельку с пагинацией")
    public TransactionHistoryResponse getTransactionHistory(
            @McpToolParam(description = "UUID кошелька для получения истории транзакций (обязательный)") String walletId,
            @McpToolParam(description = "UUID пользователя") String agentUserID,
            @McpToolParam(description = "ID последней записи для пагинации (не заполняется при первом вызове)", required = false) String lastRecordId,
            @McpToolParam(description = "Дата последней записи для пагинации (не заполняется при первом вызове)", required = false) String lastCreated,
            @McpToolParam(description = "Размер страницы пагинации", required = false) String pageSize,
            @McpToolParam(description = "Уникальный идентификатор запроса (по умолчанию генерируется автоматически)", required = false) String rquid) {

        var parsedWalletId = LinkValidationUtil.requireValidUuid(walletId, "walletId");
        var headers = LinkHeaders.of(agentUserID, rquid);

        logger.info("Запрос истории транзакций: walletId={}, lastRecordId={}, rquid={}",
                parsedWalletId, lastRecordId, headers.getRquid());

        try {
            TransactionHistoryResponse response = restClient.get()
                    .uri(uriBuilder -> {
                        uriBuilder.path(LinkConstants.ENDPOINT_WALLET_HISTORY);

                        if (lastRecordId != null && !lastRecordId.isBlank()) {
                            uriBuilder.queryParam("last_record_id", lastRecordId);
                        }
                        if (lastCreated != null && !lastCreated.isBlank()) {
                            uriBuilder.queryParam("last_created", lastCreated);
                        }
                        if (pageSize != null && !pageSize.isBlank()) {
                            uriBuilder.queryParam("page_size", pageSize);
                        }

                        return uriBuilder.build(parsedWalletId.toString());
                    })
                    .header(LinkConstants.HEADER_RQUID, headers.getRquid())
                    .header(LinkConstants.AGENT_USER_ID, headers.getAgentUserID())
                    .cookie("X-SP-D-M", "M")
                    .cookie("X-Geo-Sticky", "DS")
                    .retrieve()
                    .body(TransactionHistoryResponse.class);

            logger.info("Получена история транзакций: walletId={}, recordCount={}, hasMore={}",
                    parsedWalletId,
                    response != null && response.getTransactionHistoryRecords() != null ? response.getTransactionHistoryRecords().size() : 0,
                    response != null && response.getIsExist() != null ? response.getIsExist() : false);

            return response;

        } catch (LinkApiException e) {
            throw e;

        } catch (RestClientResponseException e) {
            HttpStatusCode statusCode = e.getStatusCode();
            String responseBody = e.getResponseBodyAsString();

            logger.error("HTTP ошибка от LINK API при получении истории транзакций: status={}, body={}",
                    statusCode, truncate(responseBody));

            switch (statusCode.value()) {
                case 400 -> throw LinkApiException.badRequest("Ошибка валидации: " + responseBody);
                case 401 -> throw LinkApiException.unauthorized("Требуется авторизация (Bearer токен)");
                case 403 -> throw LinkApiException.forbidden("Недостаточно прав для получения истории транзакций");
                case 404 -> throw LinkApiException.notFound("Кошелек", walletId);
                case 429 -> throw LinkApiException.rateLimitExceeded("Превышен лимит запросов");
                case 500 -> throw LinkApiException.internalError("Внутренняя ошибка сервера LINK API", e);
                case 502 -> throw LinkApiException.gatewayError("Ошибка шлюза LINK API");
                case 503 -> throw LinkApiException.serviceUnavailable("Сервис истории временно недоступен");
                case 504 -> throw LinkApiException.timeoutError("Таймаут ответа от LINK API");
                default -> throw LinkApiException.internalError(
                        "Неожиданная HTTP ошибка при получении истории транзакций: " + statusCode, e);
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
            logger.error("Неожиданная ошибка при получении истории транзакций: {}", e.getMessage(), e);
            throw LinkApiException.internalError("Неожиданная ошибка: " + e.getMessage(), e);
        }
    }

    private String truncate(String str) {
        if (str == null) return "null";
        return str.length() > 200 ? str.substring(0, 200) + "..." : str;
    }
}
