package ru.sber.apm.aipay.ratatouille.service.p2p;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.mcp.annotation.McpTool;
import org.springframework.ai.mcp.annotation.McpToolParam;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import ru.sber.apm.aipay.ratatouille.dto.p2pcrypto.LinkHeaders;
import ru.sber.apm.aipay.ratatouille.dto.p2pcrypto.Wallet;
import ru.sber.apm.aipay.ratatouille.dto.p2pcrypto.WithdrawRequest;
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
    public Wallet execute(
            @McpToolParam(description = "UUID кошелька для снятия средств (обязательный)") String walletId,
            @McpToolParam(description = "Адрес получателя в блокчейне (начинается с 0x для EVM)") String to,
            @McpToolParam(description = "Сумма вывода в минимальных единицах монеты (wei для ETH, satoshi для BTC)") String value,
            @McpToolParam(description = "Уникальный идентификатор запроса (по умолчанию генерируется автоматически)", required = false) String rquid) {

        var parsedWalletId = LinkValidationUtil.requireValidUuid(walletId, "walletId");
        
        // Валидация входных параметров
        if (!LinkValidationUtil.isValidEvmAddress(to)) {
            throw new IllegalArgumentException("Неверный формат адреса получателя: " + to);
        }
        if (!LinkValidationUtil.isValidMinimalUnitValue(value)) {
            throw new IllegalArgumentException("Неверный формат суммы: " + value);
        }

        var headers = LinkHeaders.of(rquid);
        
        var request = WithdrawRequest.builder()
                .to(to)
                .value(value)
                .build();

        logger.info("Запрос вывода средств: walletId={}, to={}, value={}, rquid={}", 
                parsedWalletId, maskAddress(to), value, headers.getRquid());

        Wallet response = restClient.post()
                .uri(uriBuilder -> uriBuilder
                        .path(LinkConstants.ENDPOINT_WITHDRAW)
                        .build(parsedWalletId.toString()))
                .header(LinkConstants.HEADER_RQUID, headers.getRquid())
                .body(request)
                .retrieve()
                .body(Wallet.class);

        logger.info("Вывод средств инициирован: walletId={}, status={}", 
                parsedWalletId,
                response != null ? response.getStatus() : null);

        return response;
    }

    /**
     * Маскировка адреса для логирования
     */
    private String maskAddress(String address) {
        if (address == null || address.length() <= 12) {
            return address;
        }
        return address.substring(0, 10) + "..." + address.substring(address.length() - 6);
    }
}