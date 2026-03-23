package ru.sber.apm.aipay.ratatouille.util.p2pcrypto;

import lombok.experimental.UtilityClass;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

@UtilityClass
public class LinkValidationUtil {

    /**
     * Парсинг UUID из строки с обработкой ошибок
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

    /**
     * Валидация блокчейн-адреса (EVM-совместимый)
     */
    public static boolean isValidEvmAddress(String address) {
        return address != null && address.matches("^0x[a-fA-F0-9]{40}$");
    }

    /**
     * Валидация значения в минимальных единицах (целое положительное число)
     */
    public static boolean isValidMinimalUnitValue(String value) {
        return value != null && value.matches("^\\d+$");
    }
}