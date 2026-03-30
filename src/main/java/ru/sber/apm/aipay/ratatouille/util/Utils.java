package ru.sber.apm.aipay.ratatouille.util;

import lombok.experimental.UtilityClass;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

@UtilityClass
public class Utils {

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
}
