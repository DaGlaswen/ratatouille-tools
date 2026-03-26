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
import ru.sber.apm.aipay.ratatouille.dto.p2pcrypto.GetDepositeAddressResponse;
import ru.sber.apm.aipay.ratatouille.dto.p2pcrypto.LinkHeaders;
import ru.sber.apm.aipay.ratatouille.exception.p2pcrypto.LinkApiException;
import ru.sber.apm.aipay.ratatouille.util.p2pcrypto.LinkConstants;
import ru.sber.apm.aipay.ratatouille.util.p2pcrypto.LinkValidationUtil;

@Service
public class GetDepositeAddressTool {

    private static final Logger logger = LoggerFactory.getLogger(GetDepositeAddressTool.class);

    private final RestClient restClient;

    public GetDepositeAddressTool(RestClient linkRestClient) {
        this.restClient = linkRestClient;
    }

    @McpTool(description = "Получить адрес кошелька для пополнения (депозита) криптовалюты")
    public GetDepositeAddressResponse getDepositeAddress(
            @McpToolParam(description = "UUID кошелька для получения адреса депозита (обязательный)") String walletId,
            @McpToolParam(description = "UUID пользователя") String agentUserID,
            @McpToolParam(description = "Уникальный идентификатор запроса (по умолчанию генерируется автоматически)", required = false) String rquid) {

        try {
            var parsedWalletId = LinkValidationUtil.requireValidUuid(walletId, "walletId");
            var headers = LinkHeaders.of(agentUserID, rquid);

            logger.info("Запрос адреса депозита: walletId={}, rquid={}", parsedWalletId, headers.getRquid());

            GetDepositeAddressResponse response = restClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path(LinkConstants.ENDPOINT_DEPOSITE_ADDRESS)
                            .build(parsedWalletId.toString()))
                    .header(LinkConstants.HEADER_RQUID, headers.getRquid())
                    .header(LinkConstants.AGENT_USER_ID, headers.getAgentUserID())
                    .cookie("X-SP-D-M", "M")
                    .cookie("X-Geo-Sticky", "DS")
                    .retrieve()
                    .body(GetDepositeAddressResponse.class);

            logger.info("Ответ адреса депозита: walletId={}, address={}",
                    parsedWalletId,
                    response != null ? maskAddress(response.getDepositeAddress()) : null);

            return response;
        } catch (LinkApiException e) {
            throw e;

        } catch (RestClientResponseException e) {
            HttpStatusCode statusCode = e.getStatusCode();
            String responseBody = e.getResponseBodyAsString();

            logger.error("HTTP ошибка от LINK API при выводе средств: status={}, body={}",
                    statusCode, truncate(responseBody));

            switch (statusCode.value()) {
                case 400 -> throw LinkApiException.badRequest("Ошибка валидации: " + responseBody);
                case 401 -> throw LinkApiException.unauthorized("Требуется авторизация (Bearer токен)");
                case 403 -> throw LinkApiException.forbidden("Недостаточно прав для вывода средств");
                case 404 -> throw LinkApiException.notFound("Кошелек", walletId);
                case 409 -> throw LinkApiException.conflict("Конфликт: возможно уже естьpending вывод");
                case 422 -> throw LinkApiException.badRequest("Некорректные данные вывода: " + responseBody);
                case 429 -> throw LinkApiException.rateLimitExceeded("Превышен лимит запросов на вывод");
                case 500 -> throw LinkApiException.internalError("Внутренняя ошибка сервера LINK API", e);
                case 502 -> throw LinkApiException.gatewayError("Ошибка шлюза LINK API");
                case 503 -> throw LinkApiException.serviceUnavailable("Сервис вывода временно недоступен");
                case 504 -> throw LinkApiException.timeoutError("Таймаут ответа от LINK API");
                default -> throw LinkApiException.internalError(
                        "Неожиданная HTTP ошибка при выводе средств: " + statusCode, e);
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
            logger.warn("Ошибка валидации параметров вывода: {}", e.getMessage());
            throw LinkApiException.badRequest("Ошибка валидации: " + e.getMessage());

        } catch (Exception e) {
            logger.error("Неожиданная ошибка при выводе средств: {}", e.getMessage(), e);
            throw LinkApiException.internalError("Неожиданная ошибка: " + e.getMessage(), e);
        }
    }

    /**
     * Маскировка адреса для логирования (показывает первые и последние 6 символов)
     */
    private String maskAddress(String address) {
        if (address == null || address.length() <= 12) {
            return address;
        }
        return address.substring(0, 10) + "..." + address.substring(address.length() - 6);
    }
    private String truncate(String str) {
        if (str == null) return "null";
        return str.length() > 200 ? str.substring(0, 200) + "..." : str;
    }

}