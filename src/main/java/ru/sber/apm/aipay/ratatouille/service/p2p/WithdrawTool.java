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
import ru.sber.apm.aipay.ratatouille.dto.p2pcrypto.Wallet;
import ru.sber.apm.aipay.ratatouille.dto.p2pcrypto.WithdrawRequest;
import ru.sber.apm.aipay.ratatouille.exception.p2pcrypto.LinkApiException;
import ru.sber.apm.aipay.ratatouille.util.p2pcrypto.LinkConstants;
import ru.sber.apm.aipay.ratatouille.util.p2pcrypto.LinkValidationUtil;

@Service
public class WithdrawTool {

    private static final Logger logger = LoggerFactory.getLogger(WithdrawTool.class);

    private final RestClient restClient;

    public WithdrawTool(RestClient linkRestClient) {
        this.restClient = linkRestClient;
    }

    @McpTool(description = "Вывести криптовалюту с кошелька на внешний адрес")
    public Wallet withdraw(
            @McpToolParam(description = "UUID кошелька для снятия средств (обязательный)") String walletId,
            @McpToolParam(description = "UUID пользователя") String agentUserID,
            @McpToolParam(description = "Адрес получателя в блокчейне (начинается с 0x для EVM)") String to,
            @McpToolParam(description = "Сумма вывода в минимальных единицах монеты (wei для ETH, satoshi для BTC)") String value,
            @McpToolParam(description = "Уникальный идентификатор запроса (по умолчанию генерируется автоматически)", required = false) String rquid) {

        // Валидация входных параметров
        if (walletId == null || walletId.isBlank()) {
            throw LinkApiException.badRequest("walletId обязателен");
        }
        if (to == null || to.isBlank()) {
            throw LinkApiException.badRequest("Адрес получателя обязателен");
        }
        if (value == null || value.isBlank()) {
            throw LinkApiException.badRequest("Сумма вывода обязательна");
        }

        var parsedWalletId = LinkValidationUtil.requireValidUuid(walletId, "walletId");

        if (!LinkValidationUtil.isValidEvmAddress(to)) {
            throw LinkApiException.badRequest("Неверный формат адреса получателя: " + to);
        }
        if (!LinkValidationUtil.isValidMinimalUnitValue(value)) {
            throw LinkApiException.badRequest("Неверный формат суммы: " + value);
        }

        var headers = LinkHeaders.of(agentUserID, rquid);
        var request = WithdrawRequest.builder()
                .to(to)
                .value(value)
                .build();

        logger.info("Запрос вывода средств: walletId={}, to={}, value={}, rquid={}",
                parsedWalletId, maskAddress(to), value, headers.getRquid());

        try {
            Wallet result = restClient.post()
                    .uri(uriBuilder -> uriBuilder
                            .path(LinkConstants.ENDPOINT_WITHDRAW)
                            .build(parsedWalletId.toString()))
                    .header(LinkConstants.HEADER_RQUID, headers.getRquid())
                    .body(request)
                    .retrieve()
                    .body(Wallet.class);

            logger.info("Вывод средств инициирован: walletId={}, status={}",
                    parsedWalletId, result != null ? result.getStatus() : null);

            return result;

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

    private String maskAddress(String address) {
        if (address == null || address.length() <= 12) return address;
        return address.substring(0, 10) + "..." + address.substring(address.length() - 6);
    }

    private String truncate(String str) {
        if (str == null) return "null";
        return str.length() > 200 ? str.substring(0, 200) + "..." : str;
    }
}