package ru.sber.apm.aipay.ratatouille.util.crossover;

import lombok.experimental.UtilityClass;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

/**
 * Утилиты для валидации параметров Crossover API
 */
@UtilityClass
public class CrossoverValidationUtil {

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