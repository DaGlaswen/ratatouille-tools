package ru.sber.apm.aipay.ratatouille.service.smartring;

import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.mcp.annotation.McpTool;
import org.springframework.ai.mcp.annotation.McpToolParam;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.util.UriComponentsBuilder;
import ru.sber.apm.aipay.ratatouille.config.smartring.SmartRingApiProperties;
import ru.sber.apm.aipay.ratatouille.dto.smartring.ExternalAppPageTemperatureResponseDto;
import ru.sber.apm.aipay.ratatouille.exception.smartring.SmartRingApiException;
import ru.sber.apm.aipay.ratatouille.util.Utils;
import ru.sber.apm.aipay.ratatouille.util.smartring.SmartRingConstants;
import ru.sber.apm.aipay.ratatouille.util.smartring.SmartRingUtils;

import java.net.URI;
import java.util.Collections;

@Service
public class GetSyncTemperatureTool {

    private static final Logger logger = LoggerFactory.getLogger(GetSyncTemperatureTool.class);

    private final RestClient restClient;
    private final SmartRingApiProperties properties;

    public GetSyncTemperatureTool(RestClient smartringRestClient, SmartRingApiProperties properties) {
        this.restClient = smartringRestClient;
        this.properties = properties;
    }

    @McpTool(description = "Получить данные синхронизации температуры тела (Temperature) с Smart Ring")
    public ExternalAppPageTemperatureResponseDto getTemperature(
            @McpToolParam(description = "UUID авторизации в системе Smart Ring", required = false) String uuid,
            @McpToolParam(description = "С какой даты (формат ISO 8601: yyyy-MM-dd'T'HH:mm:ss XXX) отдавать записи", required = false) String from,
            @McpToolParam(description = "По какую дату (формат ISO 8601: yyyy-MM-dd'T'HH:mm:ss XXX) отдавать записи", required = false) String to,
            @McpToolParam(description = "Текущая страница (по умолчанию 0)", required = false) Integer page,
            @McpToolParam(description = "Количество записей на странице (по умолчанию 100)", required = false) Integer pageSize) {

        Long fromUnix = Utils.convertToUnixTimestamp(from, "from");
        Long toUnix = Utils.convertToUnixTimestamp(to, "to");
        String effectiveUuid = uuid != null && !uuid.isBlank() ? uuid : properties.getUuid();

        logger.info("Запрос данных синхронизации температуры: uuid={}, from={}, to={}, page={}, pageSize={}",
                SmartRingUtils.maskUuid(effectiveUuid), from, to, page, pageSize);

        MultiValueMap<@NonNull String, @NonNull String> queryParams = new LinkedMultiValueMap<>();
        if (fromUnix != null) queryParams.put(SmartRingConstants.PARAM_FROM, Collections.singletonList(String.valueOf(fromUnix)));
        if (toUnix != null) queryParams.put(SmartRingConstants.PARAM_TO, Collections.singletonList(String.valueOf(toUnix)));
        if (page != null) queryParams.put(SmartRingConstants.PARAM_PAGE, Collections.singletonList(String.valueOf(page)));
        if (pageSize != null) queryParams.put(SmartRingConstants.PARAM_PAGE_SIZE, Collections.singletonList(String.valueOf(pageSize)));

        try {
            URI uri = UriComponentsBuilder.fromUriString(properties.getBaseUrl() + SmartRingConstants.API_PREFIX + "/temperature")
                    .queryParams(queryParams)
                    .build()
                    .toUri();

            ExternalAppPageTemperatureResponseDto response = restClient.get()
                    .uri(uri)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + effectiveUuid)
                    .retrieve()
                    .body(ExternalAppPageTemperatureResponseDto.class);

            logger.info("Ответ синхронизации температуры: записей={}, страница={}/{}, тело ответа={}",
                    response != null && response.getContent() != null ? response.getContent().size() : 0,
                    response != null ? response.getCurrentPage() : 0,
                    response != null ? response.getTotalPages() : 0,
                    Utils.toJson(response));

            return response;

        } catch (SmartRingApiException e) {
            throw e;
        } catch (RestClientResponseException e) {
            HttpStatusCode statusCode = e.getStatusCode();
            String responseBody = e.getResponseBodyAsString();
            logger.error("HTTP ошибка от Smart Ring API: status={}, body={}", statusCode, truncate(responseBody));

            switch (statusCode.value()) {
                case 400 -> throw SmartRingApiException.badRequest("Неверные параметры запроса: " + responseBody);
                case 401 -> throw SmartRingApiException.unauthorized("Неверный или отсутствующий токен авторизации");
                case 403 -> throw SmartRingApiException.forbidden("Доступ запрещён");
                case 429 -> throw SmartRingApiException.rateLimitExceeded("Превышен лимит запросов");
                case 500 -> throw SmartRingApiException.internalError("Внутренняя ошибка сервера Smart Ring", e);
                case 503 -> throw SmartRingApiException.serviceUnavailable("Сервис Smart Ring временно недоступен");
                default -> throw SmartRingApiException.internalError(
                        "Неожиданная HTTP ошибка от Smart Ring API: " + statusCode, e);
            }
        } catch (ResourceAccessException e) {
            String causeMessage = e.getCause() != null ? e.getCause().getMessage() : e.getMessage();
            logger.error("Ошибка соединения с Smart Ring API: {}", causeMessage, e);

            if (causeMessage != null && causeMessage.toLowerCase().contains("timeout")) {
                throw SmartRingApiException.timeoutError("Таймаут соединения с Smart Ring API");
            }
            throw SmartRingApiException.connectionError("Ошибка соединения с Smart Ring API: " + causeMessage, e);
        } catch (Exception e) {
            logger.error("Неожиданная ошибка при получении данных температуры: {}", e.getMessage(), e);
            throw SmartRingApiException.internalError("Неожиданная ошибка: " + e.getMessage(), e);
        }
    }

    private String truncate(String str) {
        if (str == null) return "null";
        return str.length() > 200 ? str.substring(0, 200) + "..." : str;
    }
}