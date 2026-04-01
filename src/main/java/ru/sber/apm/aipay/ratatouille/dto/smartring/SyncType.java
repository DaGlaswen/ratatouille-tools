package ru.sber.apm.aipay.ratatouille.dto.smartring;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum SyncType {
    HEART_RATE("heartrate", "heart_rate"),
    SPO2("spo2", "spo2"),
    STEP("step", "step"),
    SLEEP("sleep", "sleep"),
    HRV("hrv", "hrv"),
    STRESS("stress", "stress"),
    TEMPERATURE("temperature", "temperature");

    private final String apiValue;
    private final String description;

    public static SyncType fromApiValue(String value) {
        for (SyncType type : values()) {
            if (type.apiValue.equalsIgnoreCase(value)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Неизвестный тип синхронизации: " + value);
    }
}