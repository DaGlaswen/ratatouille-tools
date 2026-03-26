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
import ru.sber.apm.aipay.ratatouille.dto.p2pcrypto.CreateWallet;
import ru.sber.apm.aipay.ratatouille.dto.p2pcrypto.LinkHeaders;
import ru.sber.apm.aipay.ratatouille.dto.p2pcrypto.Wallet;
import ru.sber.apm.aipay.ratatouille.exception.p2pcrypto.LinkApiException;
import ru.sber.apm.aipay.ratatouille.util.p2pcrypto.LinkConstants;

@Service
public class CreateWalletTool {

    private static final Logger logger = LoggerFactory.getLogger(CreateWalletTool.class);

    private final RestClient restClient;

    public CreateWalletTool(RestClient linkRestClient) {
        this.restClient = linkRestClient;
    }

    @McpTool(description = "Создать новый кошелек для криптовалюты")
    public Wallet createWallet(
            @McpToolParam(description = "UUID пользователя") String agentUserID,
            @McpToolParam(description = "Валюта кошелька (ETH, BTC, USDT, TON)") String coin,
            @McpToolParam(description = "Уникальный идентификатор запроса (по умолчанию генерируется автоматически)", required = false) String rquid) {

        if (coin == null || coin.isBlank()) {
            throw LinkApiException.badRequest("Валюта кошелька обязательна");
        }

        var headers = LinkHeaders.of(agentUserID, rquid);
        var request = CreateWallet.builder()
                .coin(coin)
                .build();

        logger.info("Запрос создания кошелька: coin={}, rquid={}", coin, headers.getRquid());

        try {
            Wallet result = restClient.post()
                    .uri(LinkConstants.ENDPOINT_WALLETS)
                    .header(LinkConstants.HEADER_RQUID, headers.getRquid())
                    .header(LinkConstants.AGENT_USER_ID, headers.getAgentUserID())
                    .cookie("X-SP-D-M", "M")
                    .cookie("X-Geo-Sticky", "DS")
                    .body(request)
                    .retrieve()
                    .body(Wallet.class);

            logger.info("Кошелек создан: walletId={}, coin={}, status={}",
                    result != null ? result.getId() : null,
                    result != null ? result.getCoin() : null,
                    result != null ? result.getStatus() : null);

            return result;

        } catch (LinkApiException e) {
            throw e;

        } catch (RestClientResponseException e) {
            HttpStatusCode statusCode = e.getStatusCode();
            String responseBody = e.getResponseBodyAsString();

            logger.error("HTTP ошибка от LINK API при создании кошелька: status={}, body={}",
                    statusCode, truncate(responseBody));

            switch (statusCode.value()) {
                case 400 -> throw LinkApiException.badRequest("Ошибка валидации: " + responseBody);
                case 401 -> throw LinkApiException.unauthorized("Требуется авторизация (Bearer токен)");
                case 403 -> throw LinkApiException.forbidden("Недостаточно прав для создания кошелька");
                case 409 -> throw LinkApiException.conflict("Кошелек с такой валютой уже существует");
                case 422 -> throw LinkApiException.badRequest("Некорректные данные кошелька: " + responseBody);
                case 429 -> throw LinkApiException.rateLimitExceeded("Превышен лимит запросов");
                case 500 -> throw LinkApiException.internalError("Внутренняя ошибка сервера LINK API", e);
                case 502 -> throw LinkApiException.gatewayError("Ошибка шлюза LINK API");
                case 503 -> throw LinkApiException.serviceUnavailable("Сервис кошельков временно недоступен");
                case 504 -> throw LinkApiException.timeoutError("Таймаут ответа от LINK API");
                default -> throw LinkApiException.internalError(
                        "Неожиданная HTTP ошибка при создании кошелька: " + statusCode, e);
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
        } catch (org.springframework.web.client.UnknownContentTypeException e) {
            HttpStatusCode statusCode = e.getStatusCode();
            String responseBody = e.getResponseBodyAsString();

            logger.error("HTTP ошибка от LINK API при создании кошелька. Неизвестный content-type: status={}, body={}",
                    statusCode, truncate(responseBody), e);

            throw LinkApiException.internalError(
                    "Неожиданная HTTP ошибка при создании кошелька: " + statusCode, e);
        } catch (Exception e) {
            logger.error("Неожиданная ошибка при создании кошелька: {}", e.getMessage(), e);
            throw LinkApiException.internalError("Неожиданная ошибка: " + e.getMessage(), e);
        }
    }

    private String truncate(String str) {
        if (str == null) return "null";
        return str.length() > 200 ? str.substring(0, 200) + "..." : str;
    }
}
