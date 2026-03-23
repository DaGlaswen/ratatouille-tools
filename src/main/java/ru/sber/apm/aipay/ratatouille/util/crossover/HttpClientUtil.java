package ru.sber.apm.aipay.ratatouille.util.crossover;

import lombok.experimental.UtilityClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@UtilityClass
public class HttpClientUtil {

    private static final Logger logger = LoggerFactory.getLogger(HttpClientUtil.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Выполняет GET запрос и десериализует ответ
     */
    public <T> T get(java.net.http.HttpClient httpClient, String baseUrl, String path, 
                     Map<String, String> queryParams, Map<String, String> headers, Class<T> responseType) {
        try {
            var requestBuilder = HttpRequest.newBuilder()
                    .uri(buildUri(baseUrl, path, queryParams))
                    .timeout(Duration.ofSeconds(30))
                    .header("Accept", "application/json");

            // Добавляем заголовки только если они не null
            if (headers != null) {
                headers.forEach((key, value) -> {
                    if (value != null && !value.isBlank()) {
                        requestBuilder.header(key, value);
                    }
                });
            }

            var request = requestBuilder.GET().build();
            var response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            logger.debug("GET {} -> Status: {}, Body: {}", request.uri(), response.statusCode(), 
                    truncateBody(response.body()));

            if (response.statusCode() >= 400) {
                throw new RuntimeException("HTTP error " + response.statusCode() + ": " + response.body());
            }

            return objectMapper.readValue(response.body(), responseType);
            
        } catch (JacksonException e) {
            logger.error("Ошибка сериализации: {}", e.getMessage(), e);
            throw new RuntimeException("Ошибка обработки JSON", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Запрос прерван", e);
        } catch (Exception e) {
            logger.error("Ошибка выполнения запроса: {}", e.getMessage(), e);
            throw new RuntimeException("Ошибка выполнения HTTP запроса", e);
        }
    }

    /**
     * Выполняет POST запрос с телом и десериализует ответ
     */
    public <T, R> R post(java.net.http.HttpClient httpClient, String baseUrl, String path, 
                         Map<String, String> queryParams, Map<String, String> headers, 
                         T requestBody, Class<R> responseType) {
        try {
            String jsonBody = objectMapper.writeValueAsString(requestBody);
            logger.debug("POST {} -> Body: {}", baseUrl + path, truncateBody(jsonBody));

            var requestBuilder = HttpRequest.newBuilder()
                    .uri(buildUri(baseUrl, path, queryParams))
                    .timeout(Duration.ofSeconds(30))
                    .header("Content-Type", "application/json")
                    .header("Accept", "application/json");

            // Добавляем заголовки только если они не null
            if (headers != null) {
                headers.forEach((key, value) -> {
                    if (value != null && !value.isBlank()) {
                        requestBuilder.header(key, value);
                    }
                });
            }

            var request = requestBuilder.POST(HttpRequest.BodyPublishers.ofString(jsonBody)).build();
            var response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            logger.debug("POST {} -> Status: {}, Body: {}", request.uri(), response.statusCode(), 
                    truncateBody(response.body()));

            if (response.statusCode() >= 400) {
                throw new RuntimeException("HTTP error " + response.statusCode() + ": " + response.body());
            }

            return objectMapper.readValue(response.body(), responseType);
            
        } catch (JacksonException e) {
            logger.error("Ошибка сериализации: {}", e.getMessage(), e);
            throw new RuntimeException("Ошибка обработки JSON", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Запрос прерван", e);
        } catch (Exception e) {
            logger.error("Ошибка выполнения запроса: {}", e.getMessage(), e);
            throw new RuntimeException("Ошибка выполнения HTTP запроса", e);
        }
    }

    /**
     * Вспомогательный метод для создания карты заголовков с фильтрацией null-значений
     */
    public static Map<String, String> buildHeaders(Map<String, String> source) {
        if (source == null) {
            return Map.of();
        }
        var result = new HashMap<String, String>();
        source.forEach((key, value) -> {
            if (value != null && !value.isBlank()) {
                result.put(key, value);
            }
        });
        return result;
    }

    /**
     * Перегрузка для построения заголовков из пар ключ-значение
     */
    public static Map<String, String> buildHeaders(Object... keyValuePairs) {
        var result = new HashMap<String, String>();
        for (int i = 0; i < keyValuePairs.length; i += 2) {
            String key = (String) keyValuePairs[i];
            String value = (String) keyValuePairs[i + 1];
            if (value != null && !value.isBlank()) {
                result.put(key, value);
            }
        }
        return result;
    }

    private URI buildUri(String baseUrl, String path, Map<String, String> queryParams) throws URISyntaxException {
        if (queryParams == null || queryParams.isEmpty()) {
            return new URI(baseUrl + path);
        }
        
        var builder = new StringBuilder(baseUrl).append(path).append("?");
        queryParams.forEach((k, v) -> {
            if (v != null) {
                builder.append(java.net.URLEncoder.encode(k, java.nio.charset.StandardCharsets.UTF_8))
                       .append("=")
                       .append(java.net.URLEncoder.encode(v, java.nio.charset.StandardCharsets.UTF_8))
                       .append("&");
            }
        });
        
        // Убираем последний &
        String uriString = builder.toString();
        if (uriString.endsWith("&")) {
            uriString = uriString.substring(0, uriString.length() - 1);
        }
        
        return new URI(uriString);
    }

    private String truncateBody(String body) {
        if (body == null) return "null";
        return body.length() > 500 ? body.substring(0, 500) + "..." : body;
    }
}