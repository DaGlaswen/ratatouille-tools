package ru.sber.apm.aipay.ratatouille.service.smartring;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.mcp.annotation.McpTool;
import org.springframework.ai.mcp.annotation.McpToolParam;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;
import ru.sber.apm.aipay.ratatouille.config.smartring.SmartRingApiProperties;
import ru.sber.apm.aipay.ratatouille.dto.smartring.AllHealthStatisticsResponse;
import ru.sber.apm.aipay.ratatouille.dto.smartring.ExternalAppPageHeartRateResponseDto;
import ru.sber.apm.aipay.ratatouille.dto.smartring.ExternalAppPageHrvResponseDto;
import ru.sber.apm.aipay.ratatouille.dto.smartring.ExternalAppPageSleepResponseDtoV3;
import ru.sber.apm.aipay.ratatouille.dto.smartring.ExternalAppPageSpo2ResponseDto;
import ru.sber.apm.aipay.ratatouille.dto.smartring.ExternalAppPageStepResponseDto;
import ru.sber.apm.aipay.ratatouille.dto.smartring.ExternalAppPageStressResponseDto;
import ru.sber.apm.aipay.ratatouille.dto.smartring.ExternalAppPageTemperatureResponseDto;
import ru.sber.apm.aipay.ratatouille.exception.smartring.SmartRingApiException;
import ru.sber.apm.aipay.ratatouille.util.smartring.SmartRingConstants;
import ru.sber.apm.aipay.ratatouille.util.smartring.SmartRingUtils;

import java.net.URI;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
public class GetAllHealthStatisticsTool {

    private static final Logger logger = LoggerFactory.getLogger(GetAllHealthStatisticsTool.class);
    private static final ExecutorService executor = Executors.newFixedThreadPool(7);

    private final RestClient restClient;
    private final SmartRingApiProperties properties;

    public GetAllHealthStatisticsTool(RestClient smartringRestClient, SmartRingApiProperties properties) {
        this.restClient = smartringRestClient;
        this.properties = properties;
    }

    // TODO: заставлять отправлять timestamp без пробела
    @McpTool(description = """
        Получить сводку здоровья Smart Ring за период.
        Возвращает первые N записей по каждому типу данных (по умолчанию 100).
        Если в ответе hasMoreData.XXX = true — данные обрезаны, используйте индивидуальные endpoints (getHeartRate, getSleep, etc.) для получения полной выборки.
        """)
    public AllHealthStatisticsResponse getAllHealthStatistics(
            @McpToolParam(description = "UUID авторизации в системе Smart Ring", required = false) String uuid,
            @McpToolParam(description = "С какой даты (формат ISO 8601: yyyy-MM-dd'T'HH:mm:ss XXX) отдавать записи", required = false) String from,
            @McpToolParam(description = "По какую дату (формат ISO 8601: yyyy-MM-dd'T'HH:mm:ss XXX) отдавать записи", required = false) String to,
            @McpToolParam(description = "Максимум записей на каждый тип данных (по умолчанию 100)", required = false) Integer limit) {

        int effectiveLimit = limit != null ? limit : 100;
        String effectiveUuid = uuid != null && !uuid.isBlank() ? uuid : properties.getUuid();
        String maskedUuid = SmartRingUtils.maskUuid(effectiveUuid);

        logger.info("Запрос всей статистики здоровья: uuid={}, from={}, to={}, limit={}",
                maskedUuid, from, to, effectiveLimit);

        // Map для сбора ошибок
        Map<String, String> errors = new HashMap<>();

        // Запускаем все запросы параллельно, ошибки собираем в map
        CompletableFuture<ExternalAppPageHeartRateResponseDto> heartRateFuture =
                exceptionHandlingFuture(
                        CompletableFuture.supplyAsync(() -> fetchHeartRate(effectiveUuid, from, to, effectiveLimit), executor),
                        "heartRate", errors);
        CompletableFuture<ExternalAppPageSleepResponseDtoV3> sleepFuture =
                exceptionHandlingFuture(
                        CompletableFuture.supplyAsync(() -> fetchSleep(effectiveUuid, from, to, effectiveLimit), executor),
                        "sleep", errors);
        CompletableFuture<ExternalAppPageStepResponseDto> stepsFuture =
                exceptionHandlingFuture(
                        CompletableFuture.supplyAsync(() -> fetchSteps(effectiveUuid, from, to, effectiveLimit), executor),
                        "steps", errors);
        CompletableFuture<ExternalAppPageStressResponseDto> stressFuture =
                exceptionHandlingFuture(
                        CompletableFuture.supplyAsync(() -> fetchStress(effectiveUuid, from, to, effectiveLimit), executor),
                        "stress", errors);
        CompletableFuture<ExternalAppPageSpo2ResponseDto> spo2Future =
                exceptionHandlingFuture(
                        CompletableFuture.supplyAsync(() -> fetchSpo2(effectiveUuid, from, to, effectiveLimit), executor),
                        "spo2", errors);
        CompletableFuture<ExternalAppPageHrvResponseDto> hrvFuture =
                exceptionHandlingFuture(
                        CompletableFuture.supplyAsync(() -> fetchHrv(effectiveUuid, from, to, effectiveLimit), executor),
                        "hrv", errors);
        CompletableFuture<ExternalAppPageTemperatureResponseDto> temperatureFuture =
                exceptionHandlingFuture(
                        CompletableFuture.supplyAsync(() -> fetchTemperature(effectiveUuid, from, to, effectiveLimit), executor),
                        "temperature", errors);

        // Ждем завершения всех запросов
        CompletableFuture.allOf(
                heartRateFuture, sleepFuture, stepsFuture, stressFuture,
                spo2Future, hrvFuture, temperatureFuture
        ).join();

        ExternalAppPageHeartRateResponseDto heartRate = heartRateFuture.join();
        ExternalAppPageSleepResponseDtoV3 sleep = sleepFuture.join();
        ExternalAppPageStepResponseDto steps = stepsFuture.join();
        ExternalAppPageStressResponseDto stress = stressFuture.join();
        ExternalAppPageSpo2ResponseDto spo2 = spo2Future.join();
        ExternalAppPageHrvResponseDto hrv = hrvFuture.join();
        ExternalAppPageTemperatureResponseDto temperature = temperatureFuture.join();

        // Определяем, есть ли ещё данные для каждого типа
        boolean hasMoreHeartRate = hasMoreData(heartRate);
        boolean hasMoreSleep = hasMoreData(sleep);
        boolean hasMoreSteps = hasMoreData(steps);
        boolean hasMoreStress = hasMoreData(stress);
        boolean hasMoreSpo2 = hasMoreData(spo2);
        boolean hasMoreHrv = hasMoreData(hrv);
        boolean hasMoreTemperature = hasMoreData(temperature);
        boolean hasMoreAny = hasMoreHeartRate || hasMoreSleep || hasMoreSteps || hasMoreStress || 
                             hasMoreSpo2 || hasMoreHrv || hasMoreTemperature;

        AllHealthStatisticsResponse.HasMoreDataInfo hasMoreData = 
                AllHealthStatisticsResponse.HasMoreDataInfo.builder()
                        .heartRate(hasMoreHeartRate)
                        .sleep(hasMoreSleep)
                        .steps(hasMoreSteps)
                        .stress(hasMoreStress)
                        .spo2(hasMoreSpo2)
                        .hrv(hasMoreHrv)
                        .temperature(hasMoreTemperature)
                        .any(hasMoreAny)
                        .build();

        AllHealthStatisticsResponse response = AllHealthStatisticsResponse.builder()
                .heartRate(heartRate)
                .sleep(sleep)
                .steps(steps)
                .stress(stress)
                .spo2(spo2)
                .hrv(hrv)
                .temperature(temperature)
                .requestedUuid(maskedUuid)
                .timestamp(Instant.now().getEpochSecond())
                .hasMoreData(hasMoreData)
                .errors(errors.isEmpty() ? null : errors)
                .build();

        logger.info("Получена вся статистика здоровья: heartRate={} (more={}), sleep={} (more={}), steps={} (more={}), stress={} (more={}), spo2={} (more={}), hrv={} (more={}), temperature={} (more={})",
                heartRate != null && heartRate.getContent() != null ? heartRate.getContent().size() : 0, hasMoreHeartRate,
                sleep != null && sleep.getContent() != null ? sleep.getContent().size() : 0, hasMoreSleep,
                steps != null && steps.getContent() != null ? steps.getContent().size() : 0, hasMoreSteps,
                stress != null && stress.getContent() != null ? stress.getContent().size() : 0, hasMoreStress,
                spo2 != null && spo2.getContent() != null ? spo2.getContent().size() : 0, hasMoreSpo2,
                hrv != null && hrv.getContent() != null ? hrv.getContent().size() : 0, hasMoreHrv,
                temperature != null && temperature.getContent() != null ? temperature.getContent().size() : 0, hasMoreTemperature);

        return response;
    }

    /**
     * Оборачивает CompletableFuture в обработчик исключений.
     * При ошибке сохраняет её в map и возвращает null вместо проброса исключения.
     */
    private <T> CompletableFuture<T> exceptionHandlingFuture(CompletableFuture<T> future, String metricName, Map<String, String> errors) {
        return future.exceptionally(ex -> {
            String errorMsg = ex.getCause() != null ? ex.getCause().getMessage() : ex.getMessage();
            logger.error("Ошибка получения данных метрики {}: {}", metricName, errorMsg);
            errors.put(metricName, errorMsg);
            return null;
        });
    }

    /**
     * Проверяет, есть ли ещё данные для загрузки
     * @return true если isLast = false (есть следующие страницы)
     */
    private boolean hasMoreData(Object response) {
        if (response == null) return false;
        if (response instanceof ExternalAppPageHeartRateResponseDto r) {
            return r.getIsLast() != null && !r.getIsLast();
        }
        if (response instanceof ExternalAppPageSleepResponseDtoV3 r) {
            return r.getIsLast() != null && !r.getIsLast();
        }
        if (response instanceof ExternalAppPageStepResponseDto r) {
            return r.getIsLast() != null && !r.getIsLast();
        }
        if (response instanceof ExternalAppPageStressResponseDto r) {
            return r.getIsLast() != null && !r.getIsLast();
        }
        if (response instanceof ExternalAppPageSpo2ResponseDto r) {
            return r.getIsLast() != null && !r.getIsLast();
        }
        if (response instanceof ExternalAppPageHrvResponseDto r) {
            return r.getIsLast() != null && !r.getIsLast();
        }
        if (response instanceof ExternalAppPageTemperatureResponseDto r) {
            return r.getIsLast() != null && !r.getIsLast();
        }
        return false;
    }

    private ExternalAppPageHeartRateResponseDto fetchHeartRate(String uuid, String from, String to, Integer limit) {
        URI uri = buildUri("/heartrate", from, to, limit);
        return restClient.get()
                .uri(uri)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + uuid)
                .retrieve()
                .body(ExternalAppPageHeartRateResponseDto.class);
    }

    private ExternalAppPageSleepResponseDtoV3 fetchSleep(String uuid, String from, String to, Integer limit) {
        URI uri = buildUri("/sleep", from, to, limit);
        return restClient.get()
                .uri(uri)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + uuid)
                .retrieve()
                .body(ExternalAppPageSleepResponseDtoV3.class);
    }

    private ExternalAppPageStepResponseDto fetchSteps(String uuid, String from, String to, Integer limit) {
        URI uri = buildUri("/step", from, to, limit);
        return restClient.get()
                .uri(uri)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + uuid)
                .retrieve()
                .body(ExternalAppPageStepResponseDto.class);
    }

    private ExternalAppPageStressResponseDto fetchStress(String uuid, String from, String to, Integer limit) {
        URI uri = buildUri("/stress", from, to, limit);
        return restClient.get()
                .uri(uri)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + uuid)
                .retrieve()
                .body(ExternalAppPageStressResponseDto.class);
    }

    private ExternalAppPageSpo2ResponseDto fetchSpo2(String uuid, String from, String to, Integer limit) {
        URI uri = buildUri("/spo2", from, to, limit);
        return restClient.get()
                .uri(uri)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + uuid)
                .retrieve()
                .body(ExternalAppPageSpo2ResponseDto.class);
    }

    private ExternalAppPageHrvResponseDto fetchHrv(String uuid, String from, String to, Integer limit) {
        URI uri = buildUri("/hrv", from, to, limit);
        return restClient.get()
                .uri(uri)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + uuid)
                .retrieve()
                .body(ExternalAppPageHrvResponseDto.class);
    }

    private ExternalAppPageTemperatureResponseDto fetchTemperature(String uuid, String from, String to, Integer limit) {
        URI uri = buildUri("/temperature", from, to, limit);
        return restClient.get()
                .uri(uri)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + uuid)
                .retrieve()
                .body(ExternalAppPageTemperatureResponseDto.class);
    }

    private URI buildUri(String endpoint, String from, String to, Integer limit) {
        Long fromUnix = ru.sber.apm.aipay.ratatouille.util.Utils.convertToUnixTimestamp(from, "from");
        Long toUnix = ru.sber.apm.aipay.ratatouille.util.Utils.convertToUnixTimestamp(to, "to");

        var queryParams = new org.springframework.util.LinkedMultiValueMap<String, String>();
        if (fromUnix != null) queryParams.put(SmartRingConstants.PARAM_FROM, java.util.Collections.singletonList(String.valueOf(fromUnix)));
        if (toUnix != null) queryParams.put(SmartRingConstants.PARAM_TO, java.util.Collections.singletonList(String.valueOf(toUnix)));
        if (limit != null) queryParams.put(SmartRingConstants.PARAM_PAGE_SIZE, java.util.Collections.singletonList(String.valueOf(limit)));

        return UriComponentsBuilder.fromUriString(properties.getBaseUrl() + SmartRingConstants.API_PREFIX + endpoint)
                .queryParams(queryParams)
                .build()
                .toUri();
    }
}
