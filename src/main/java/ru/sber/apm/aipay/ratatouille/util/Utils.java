package ru.sber.apm.aipay.ratatouille.util;

import lombok.experimental.UtilityClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.sber.apm.aipay.ratatouille.exception.smartring.SmartRingApiException;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

@UtilityClass
public class Utils {

    private static final Logger logger = LoggerFactory.getLogger(Utils.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();

    private static final DateTimeFormatter formatter =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public static String getCurrentTimestamp() {
        return LocalDateTime.now().format(formatter);
    }

    private static final DateTimeFormatter formatterWithTimezone =
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssXXX");

    public static String getCurrentTimestampZ() {
        // Используем formatterWithTimezone для получения формата ISO с зоной
        // Заменяем 'Z' на '+00:00', т.к. паттерн 'XXX' для UTC выводит 'Z',
        // а регулярное выражение требует явный знак '+' и смещение.
        return OffsetDateTime.now(ZoneOffset.UTC)
                .format(formatterWithTimezone)
                .replace("Z", "+00:00");
    }

    /**
     * Формат ISO 8601 с timezone (без пробела): 2026-03-30T00:00:00+03:00
     */
    private static final DateTimeFormatter ISO_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssXXX");

    /**
     * Конвертирует дату из ISO 8601 формата в Unix timestamp (секунды)
     * Поддерживаемые форматы:
     * - yyyy-MM-dd'T'HH:mm:ss +03:00 (с пробелом)
     * - yyyy-MM-dd'T'HH:mm:ss+03:00 (без пробела)
     *
     * @param isoDate   дата в формате ISO 8601
     * @param paramName имя параметра для логирования ошибок
     * @return Unix timestamp в секундах или null если isoDate null/пустой
     * @throws SmartRingApiException если формат даты некорректен
     */
    public static Long convertToUnixTimestamp(String isoDate, String paramName) {
        if (isoDate == null || isoDate.isBlank()) {
            return null;
        }
        try {
            // Нормализуем дату: убираем пробел перед timezone для совместимости
            // 2026-03-30T00:00:00 +03:00 -> 2026-03-30T00:00:00+03:00
            String normalizedDate = isoDate.replace(" +", "+").replace(" -", "-");
            OffsetDateTime odt = OffsetDateTime.parse(normalizedDate, ISO_FORMATTER);
            return odt.toEpochSecond();
        } catch (Exception e) {
            logger.error("Ошибка парсинга даты {}='{}'. Ожидаемый формат: yyyy-MM-dd'T'HH:mm:ss +03:00", paramName, isoDate, e);
            throw SmartRingApiException.badRequest("Неверный формат даты для параметра " + paramName + ". Ожидаемый формат ISO 8601: yyyy-MM-dd'T'HH:mm:ss +03:00 (пробел перед timezone опционален)");
        }
    }

    /**
     * Сериализует объект в JSON строку
     *
     * @param obj объект для сериализации
     * @return JSON строка или строковое представление объекта при ошибке
     */
    public static String toJson(Object obj) {
        if (obj == null) return "null";
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (Exception e) {
            logger.warn("Ошибка сериализации объекта в JSON: {}", e.getMessage());
            return String.valueOf(obj);
        }
    }
}
