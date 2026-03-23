package ru.sber.apm.aipay.ratatouille.service.p2p;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.mcp.annotation.McpTool;
import org.springframework.ai.mcp.annotation.McpToolParam;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import ru.sber.apm.aipay.ratatouille.dto.p2pcrypto.GetDepositeAddressResponse;
import ru.sber.apm.aipay.ratatouille.dto.p2pcrypto.LinkHeaders;
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
            @McpToolParam(description = "Уникальный идентификатор запроса (по умолчанию генерируется автоматически)", required = false) String rquid) {

        var parsedWalletId = LinkValidationUtil.requireValidUuid(walletId, "walletId");
        var headers = LinkHeaders.of(rquid);

        logger.info("Запрос адреса депозита: walletId={}, rquid={}", parsedWalletId, headers.getRquid());

        GetDepositeAddressResponse response = restClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path(LinkConstants.ENDPOINT_DEPOSITE_ADDRESS)
                        .build(parsedWalletId.toString()))
                .header(LinkConstants.HEADER_RQUID, headers.getRquid())
                .retrieve()
                .body(GetDepositeAddressResponse.class);

        logger.info("Ответ адреса депозита: walletId={}, address={}", 
                parsedWalletId,
                response != null ? maskAddress(response.getDepositeAddress()) : null);

        return response;
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
}