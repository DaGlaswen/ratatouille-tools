package ru.sber.apm.aipay.ratatouille.service.crossover;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.mcp.annotation.McpTool;
import org.springframework.ai.mcp.annotation.McpToolParam;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;
import ru.sber.apm.aipay.ratatouille.config.crossover.CrossoverApiProperties;
import ru.sber.apm.aipay.ratatouille.dto.crossover.CrossoverHeaders;
import ru.sber.apm.aipay.ratatouille.dto.crossover.MerchantInfo;
import ru.sber.apm.aipay.ratatouille.exception.crossover.CrossoverApiException;
import ru.sber.apm.aipay.ratatouille.util.Utils;
import ru.sber.apm.aipay.ratatouille.util.crossover.CrossoverConstants;

import static org.springframework.util.StringUtils.truncate;

@Service
public class GetMerchantInfoTool {

    private static final Logger logger = LoggerFactory.getLogger(GetMerchantInfoTool.class);

    private final RestClient restClient;
    private final CrossoverApiProperties crossoverApiProperties;

    public GetMerchantInfoTool(RestClient crossoverRestClient, CrossoverApiProperties crossoverApiProperties) {
        this.restClient = crossoverRestClient;
        this.crossoverApiProperties = crossoverApiProperties;
    }

    @McpTool(description = "Получить информацию о партнере по его ID в системе 2GIS")
    public MerchantInfo getMerchantInfo(
            @McpToolParam(description = "ID партнера в системе 2GIS (обязательный)") String extBranchId,
            @McpToolParam(description = "Уникальный идентификатор запроса (по умолчанию генерируется автоматически)", required = false) String rqUID,
            @McpToolParam(description = "ID сессии на фронтенде (опционально)", required = false) String localSessionId) {

        rqUID = rqUID != null ? rqUID : java.util.UUID.randomUUID().toString();

        try {
            var headers = CrossoverHeaders.builder()
                    .authorization(crossoverApiProperties.getApiKey())
                    .timestamp(Utils.getCurrentTimestamp())
                    .rqUID(rqUID)
                    .localSessionId(localSessionId)
                    .build();

            logger.info("Запрос информации о партнере: extBranchId={}, rqUID={}", extBranchId, rqUID);

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

            logger.info("ЗАГОЛОВОК Authorization: {}, rqUID={}", headers.getAuthorization(), rqUID);
            if (response == null) {
                throw CrossoverApiException.notFound("Партнер", extBranchId);
            }
            logger.info("Ответ от партнера: pointId={}, name={}, status.active={}, qrData={}, rqUID={}",
                    response.getPointId(),
                    response.getName(),
                    response.getStatus() != null ? response.getStatus().getActive() : null,
                    response.getQrData(),
                    rqUID);

            return response;
        } catch (CrossoverApiException e) {
            // Пробрасываем наши кастомные исключения дальше
            throw e;

        } catch (RestClientResponseException e) {
            // Обработка HTTP ошибок от API (4xx, 5xx)
            HttpStatusCode statusCode = e.getStatusCode();
            String responseBody = e.getResponseBodyAsString();

            logger.error("HTTP ошибка от Crossover API: status={}, body={}, rqUID={}", statusCode, truncate(responseBody), rqUID);

            switch (statusCode.value()) {
                case 400 -> throw CrossoverApiException.badRequest("Неверные параметры запроса: " + responseBody);
                case 401 -> throw CrossoverApiException.unauthorized("Неверный apiKey");
                case 403 -> throw CrossoverApiException.forbidden("Доступ запрещён: " + responseBody);
                case 404 -> throw CrossoverApiException.notFound("Партнер", extBranchId);
                case 409 -> throw CrossoverApiException.conflict("Конфликт запроса: " + responseBody);
                case 429 -> throw CrossoverApiException.rateLimitExceeded("Превышен лимит запросов");
                case 500 -> throw CrossoverApiException.internalError("Внутренняя ошибка сервера Crossover", e);
                case 502 -> throw CrossoverApiException.gatewayError("Ошибка шлюза Crossover API");
                case 503 -> throw CrossoverApiException.serviceUnavailable("Сервис Crossover временно недоступен");
                case 504 -> throw CrossoverApiException.timeoutError("Таймаут ответа от Crossover API");
                default -> throw CrossoverApiException.internalError(
                        "Неожиданная HTTP ошибка от Crossover API: " + statusCode, e);
            }

        } catch (ResourceAccessException e) {
            // Ошибки соединения (nginx down, timeout, DNS)
            String causeMessage = e.getCause() != null ? e.getCause().getMessage() : e.getMessage();
            logger.error("Ошибка соединения с Crossover API: {}, rqUID={}", causeMessage, rqUID, e);

            if (causeMessage != null && causeMessage.toLowerCase().contains("timeout")) {
                throw CrossoverApiException.timeoutError("Таймаут соединения с Crossover API");
            }
            if (causeMessage != null && causeMessage.toLowerCase().contains("connection refused")) {
                throw CrossoverApiException.connectionError("Сервис Crossover недоступен (nginx down)", e);
            }
            if (causeMessage != null && causeMessage.toLowerCase().contains("ssl")) {
                throw CrossoverApiException.connectionError("SSL ошибка при соединении с Crossover API", e);
            }

            throw CrossoverApiException.connectionError("Ошибка соединения с Crossover API: " + causeMessage, e);

        } catch (IllegalArgumentException e) {
            // Ошибки валидации URI, параметров
            logger.warn("Ошибка валидации параметров: {}, rqUID={}", e.getMessage(), rqUID);
            throw CrossoverApiException.badRequest("Ошибка валидации: " + e.getMessage());

        } catch (Exception e) {
            // Все остальные неожиданные ошибки
            logger.error("Неожиданная ошибка при получении информации о партнере: {}, rqUID={}", e.getMessage(), rqUID, e);
            throw CrossoverApiException.internalError("Неожиданная ошибка: " + e.getMessage(), e);
        }
    }
}
