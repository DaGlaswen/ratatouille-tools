package ru.sber.apm.aipay.ratatouille.util.crossover;

import lombok.experimental.UtilityClass;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;
import java.util.regex.Pattern;

/**
 * Утилиты для валидации параметров Crossover API
 */
@UtilityClass
public class CrossoverValidationUtil {

    private static final Pattern SESSION_ID_PATTERN =
        Pattern.compile(CrossoverConstants.SESSION_ID_PATTERN);

    /**
     * Валидация pagination параметров
     * @param page номер страницы
     * @param limit количество элементов на странице
     * @throws IllegalArgumentException если параметры невалидны
     */
    public static void validatePagination(@NotNull Integer page, @NotNull Integer limit) {
        if (page == null || page < CrossoverConstants.DEFAULT_PAGE) {
            throw new IllegalArgumentException("Page must be >= " + CrossoverConstants.DEFAULT_PAGE);
        }
        if (limit == null || limit < CrossoverConstants.DEFAULT_PAGE || limit > CrossoverConstants.MAX_LIMIT) {
            throw new IllegalArgumentException("Limit must be between " + 
                CrossoverConstants.DEFAULT_PAGE + " and " + CrossoverConstants.MAX_LIMIT);
        }
    }

    /**
     * Валидация pagination для рекомендаций
     * page 1..500, limit 1..99
     */
    public static void validateRecommendationsPagination(Integer page, Integer limit) {
        if (page == null) page = CrossoverConstants.DEFAULT_PAGE;
        if (limit == null) limit = CrossoverConstants.DEFAULT_RECOMMENDATIONS_LIMIT;
        if (page < CrossoverConstants.DEFAULT_PAGE || page > CrossoverConstants.MAX_RECOMMENDATIONS_PAGE) {
            throw new IllegalArgumentException("page must be between 1 and " + CrossoverConstants.MAX_RECOMMENDATIONS_PAGE);
        }
        if (limit < CrossoverConstants.DEFAULT_PAGE || limit > CrossoverConstants.MAX_RECOMMENDATIONS_LIMIT) {
            throw new IllegalArgumentException("limit must be between 1 and " + CrossoverConstants.MAX_RECOMMENDATIONS_LIMIT);
        }
    }

    /**
     * Валидация sessionId для рекомендаций
     * Строка 4–128 символов, только [0-9A-Za-z_-]
     */
    public static void validateRecommendationsSessionId(String sessionId) {
        if (sessionId == null || sessionId.isBlank()) {
            throw new IllegalArgumentException("sessionId is required");
        }
        String trimmed = sessionId.trim();
        if (trimmed.length() < CrossoverConstants.MIN_SESSION_ID_LENGTH) {
            throw new IllegalArgumentException("sessionId must be at least " +
                CrossoverConstants.MIN_SESSION_ID_LENGTH + " characters");
        }
        if (trimmed.length() > CrossoverConstants.MAX_SESSION_ID_LENGTH) {
            throw new IllegalArgumentException("sessionId must be at most " +
                CrossoverConstants.MAX_SESSION_ID_LENGTH + " characters");
        }
        if (!SESSION_ID_PATTERN.matcher(trimmed).matches()) {
            throw new IllegalArgumentException("sessionId contains invalid characters. Only [0-9A-Za-z_-] allowed");
        }
    }

    /**
     * Парсинг UUID из строки с обработкой ошибок
     * @param uuidString строковое представление UUID
     * @return UUID или null если строка невалидна
     */
    public static UUID parseUuidSafe(String uuidString) {
        if (uuidString == null || uuidString.isBlank()) {
            return null;
        }
        try {
            return UUID.fromString(uuidString);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    /**
     * Проверка обязательного UUID
     * @param uuidString строковое представление UUID
     * @param fieldName имя поля для сообщения об ошибке
     * @return валидный UUID
     * @throws IllegalArgumentException если UUID невалиден
     */
    public static UUID requireValidUuid(String uuidString, String fieldName) {
        if (uuidString == null || uuidString.isBlank()) {
            throw new IllegalArgumentException(fieldName + " is required");
        }
        try {
            return UUID.fromString(uuidString);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(fieldName + " has invalid UUID format: " + uuidString, e);
        }
    }
}
