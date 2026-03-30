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
import ru.sber.apm.aipay.ratatouille.dto.p2pcrypto.GetWalletsResponseExtended;
import ru.sber.apm.aipay.ratatouille.dto.p2pcrypto.LinkHeaders;
import ru.sber.apm.aipay.ratatouille.exception.p2pcrypto.LinkApiException;
import ru.sber.apm.aipay.ratatouille.util.p2pcrypto.LinkConstants;

@Service
public class GetWalletsTool {

    private static final Logger logger = LoggerFactory.getLogger(GetWalletsTool.class);

    private final RestClient restClient;

    public GetWalletsTool(RestClient linkRestClient) {
        this.restClient = linkRestClient;
    }

    @McpTool(description = "Получить список кошельков пользователя с балансами")
    public GetWalletsResponseExtended getWallets(
            @McpToolParam(description = "UUID пользователя") String agentUserID,
            @McpToolParam(description = "Уникальный идентификатор запроса (по умолчанию генерируется автоматически)", required = false) String rquid) {

        String effectiveRquid = rquid != null ? rquid : java.util.UUID.randomUUID().toString();

        var headers = LinkHeaders.of(agentUserID, effectiveRquid);

        logger.info("Запрос списка кошельков: rquid={}", effectiveRquid);

        try {
            GetWalletsResponseExtended response = restClient.get()
                    .uri(LinkConstants.ENDPOINT_WALLETS)
                    .header(LinkConstants.HEADER_RQUID, headers.getRquid())
                    .header(LinkConstants.AGENT_USER_ID, headers.getAgentUserID())
                    .cookie("X-SP-D-M", "M")
                    .cookie("X-Geo-Sticky", "DS")
                    .retrieve()
                    .body(GetWalletsResponseExtended.class);

            logger.info("Получен список кошельков: walletCount={}, rquid={}",
                    response != null && response.getWallets() != null ? response.getWallets().size() : 0,
                    effectiveRquid);

            return response;

        } catch (LinkApiException e) {
            throw e;

        } catch (RestClientResponseException e) {
            HttpStatusCode statusCode = e.getStatusCode();
            String responseBody = e.getResponseBodyAsString();

            logger.error("HTTP ошибка от LINK API при получении кошельков: status={}, body={}, rquid={}",
                    statusCode, truncate(responseBody), effectiveRquid);

            switch (statusCode.value()) {
                case 400 -> throw LinkApiException.badRequest("Ошибка валидации: " + responseBody);
                case 401 -> throw LinkApiException.unauthorized("Требуется авторизация (Bearer токен)");
                case 403 -> throw LinkApiException.forbidden("Недостаточно прав для получения кошельков");
                case 404 -> throw LinkApiException.notFound("Пользователь", agentUserID);
                case 429 -> throw LinkApiException.rateLimitExceeded("Превышен лимит запросов");
                case 500 -> throw LinkApiException.internalError("Внутренняя ошибка сервера LINK API", e);
                case 502 -> throw LinkApiException.gatewayError("Ошибка шлюза LINK API");
                case 503 -> throw LinkApiException.serviceUnavailable("Сервис кошельков временно недоступен");
                case 504 -> throw LinkApiException.timeoutError("Таймаут ответа от LINK API");
                default -> throw LinkApiException.internalError(
                        "Неожиданная HTTP ошибка при получении кошельков: " + statusCode, e);
            }

        } catch (ResourceAccessException e) {
            String causeMessage = e.getCause() != null ? e.getCause().getMessage() : e.getMessage();
            logger.error("Ошибка соединения с LINK API: {}, rquid={}", causeMessage, effectiveRquid, e);

            if (causeMessage != null && causeMessage.toLowerCase().contains("timeout")) {
                throw LinkApiException.timeoutError("Таймаут соединения с LINK API");
            }
            if (causeMessage != null && causeMessage.toLowerCase().contains("connection refused")) {
                throw LinkApiException.connectionError("Сервис LINK недоступен (nginx down)", e);
            }

            throw LinkApiException.connectionError("Ошибка соединения с LINK API: " + causeMessage, e);

        } catch (IllegalArgumentException e) {
            logger.warn("Ошибка валидации параметров: {}, rquid={}", e.getMessage(), effectiveRquid);
            throw LinkApiException.badRequest("Ошибка валидации: " + e.getMessage());

        } catch (Exception e) {
            logger.error("Неожиданная ошибка при получении кошельков: {}, rquid={}", e.getMessage(), effectiveRquid, e);
            throw LinkApiException.internalError("Неожиданная ошибка: " + e.getMessage(), e);
        }
    }

    private String truncate(String str) {
        if (str == null) return "null";
        return str.length() > 200 ? str.substring(0, 200) + "..." : str;
    }
}
