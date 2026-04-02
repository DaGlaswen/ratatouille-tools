package ru.sber.apm.aipay.ratatouille.util.smartring;

import lombok.experimental.UtilityClass;

@UtilityClass
public class SmartRingUtils {

    /**
     * Маскирует UUID для безопасного логирования
     * Показывает первые 8 и последние 4 символа, остальное заменяет на *
     *
     * @param uuid UUID для маскирования
     * @return замаскированный UUID или "null" если входное значение null
     */
    public String maskUuid(String uuid) {
        if (uuid == null || uuid.isBlank()) {
            return "null";
        }
        if (uuid.length() <= 12) {
            return "***";
        }
        return uuid.substring(0, 8) + "****" + uuid.substring(uuid.length() - 4);
    }
}
