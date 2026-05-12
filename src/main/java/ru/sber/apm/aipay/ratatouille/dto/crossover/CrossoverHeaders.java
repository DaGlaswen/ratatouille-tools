package ru.sber.apm.aipay.ratatouille.dto.crossover;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import lombok.experimental.Accessors;

/**
 * Заголовки для запросов к Crossover API.
 *
 * Полный набор по CXV_HEADER_SLUG_TO_UPSTREAM (14 canonical headers):
 * Authorization, Cookie, RqUID, localSessionId,
 * deviceName, appName, X-System-Id, x-pod-sticky,
 * sdkVersion, OS, UserTm, timestamp,
 * x-b3-traceid, x-b3-spanid.
 */
@Getter
@Setter
@Accessors(chain = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CrossoverHeaders {

    /** API KEY для авторизации */
    private String authorization;

    /** Cookie сессии (нативный клиент → bridge) */
    private String cookie;

    /** Уникальный идентификатор запроса */
    private String rqUID;

    /** ID сессии на фронтенде */
    private String localSessionId;

    /** Имя устройства (напр. iPhone, Android) */
    private String deviceName;

    /** Имя приложения (напр. ratatouille-ios) */
    private String appName;

    /** ID системы */
    private String xSystemId;

    /** Sticky pod для балансировки */
    private String xPodSticky;

    /** Версия SDK */
    private String sdkVersion;

    /** Операционная система (напр. iOS 17.0) */
    private String os;

    /** User timestamp (ISO-8601 для user-контекста) */
    private String userTm;

    /** Время отправки запроса (формат "YYYY-MM-DD HH:mm:ss", MSK) */
    private String timestamp;

    /** Zipkin trace ID */
    private String xB3Traceid;

    /** Zipkin span ID */
    private String xB3Spanid;
}
