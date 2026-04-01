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
import ru.sber.apm.aipay.ratatouille.dto.smartring.ExternalAppPageSleepResponseDtoV3;
import ru.sber.apm.aipay.ratatouille.exception.smartring.SmartRingApiException;
import ru.sber.apm.aipay.ratatouille.util.smartring.SmartRingConstants;

import java.net.URI;
import java.util.Collections;

@Service
public class GetSyncSleepTool {

    private static final Logger logger = LoggerFactory.getLogger(GetSyncSleepTool.class);
    private final RestClient restClient;
    private final SmartRingApiProperties properties;

    public GetSyncSleepTool(RestClient smartringRestClient, SmartRingApiProperties properties) {
        this.restClient = smartringRestClient;
        this.properties = properties;
    }

    @McpTool(description = "Получить данные синхронизации сна (Sleep) с Smart Ring")
    public ExternalAppPageSleepResponseDtoV3 getSleep(
            @McpToolParam(description = "С какой даты (unix time) отдавать записи", required = false) Long from,
            @McpToolParam(description = "По какую дату (unix time) отдавать записи", required = false) Long to,
            @McpToolParam(description = "Текущая страница (по умолчанию 0)", required = false) Integer page,
            @McpToolParam(description = "Количество записей на странице (по умолчанию 100)", required = false) Integer pageSize) {

        logger.info("Запрос данных синхронизации сна: from={}, to={}, page={}, pageSize={}", from, to, page, pageSize);

        MultiValueMap<@NonNull String, @NonNull String> queryParams = new LinkedMultiValueMap<>();
        if (from != null) queryParams.put(SmartRingConstants.PARAM_FROM, Collections.singletonList(String.valueOf(from)));
        if (to != null) queryParams.put(SmartRingConstants.PARAM_TO, Collections.singletonList(String.valueOf(to)));
        if (page != null) queryParams.put(SmartRingConstants.PARAM_PAGE, Collections.singletonList(String.valueOf(page)));
        if (pageSize != null) queryParams.put(SmartRingConstants.PARAM_PAGE_SIZE, Collections.singletonList(String.valueOf(pageSize)));

        try {
            URI uri = UriComponentsBuilder.fromUriString(properties.getBaseUrl() + SmartRingConstants.API_PREFIX + "/sleep")
                    .queryParams(queryParams)
                    .build()
                    .toUri();

            ExternalAppPageSleepResponseDtoV3 response = restClient.get()
                    .uri(uri)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + properties.getUuid())
                    .retrieve()
                    .body(ExternalAppPageSleepResponseDtoV3.class);

            logger.info("Ответ синхронизации сна: записей={}, страница={}/{}", 
                    response != null && response.getContent() != null ? response.getContent().size() : 0,
                    response != null ? response.getCurrentPage() : 0,
                    response != null ? response.getTotalPages() : 0);

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
            logger.error("Неожиданная ошибка при получении данных сна: {}", e.getMessage(), e);
            throw SmartRingApiException.internalError("Неожиданная ошибка: " + e.getMessage(), e);
        }
    }

    private String truncate(String str) {
        if (str == null) return "null";
        return str.length() > 200 ? str.substring(0, 200) + "..." : str;
    }
}