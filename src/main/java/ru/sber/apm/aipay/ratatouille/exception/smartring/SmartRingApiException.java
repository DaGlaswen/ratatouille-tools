package ru.sber.apm.aipay.ratatouille.exception.smartring;

import lombok.Getter;
import lombok.experimental.Accessors;
import org.springframework.http.HttpStatus;
import ru.sber.apm.aipay.ratatouille.util.smartring.SmartRingConstants;

@Getter
@Accessors(chain = true)
public class SmartRingApiException extends RuntimeException {

    private final Integer errorCode;
    private final HttpStatus httpStatus;
    private final String originalMessage;

    public SmartRingApiException(Integer errorCode, String message, HttpStatus httpStatus) {
        super(message);
        this.errorCode = errorCode;
        this.httpStatus = httpStatus;
        this.originalMessage = message;
    }

    public SmartRingApiException(Integer errorCode, String message, HttpStatus httpStatus, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
        this.httpStatus = httpStatus;
        this.originalMessage = message;
    }

    // === Фабричные методы ===
    
    public static SmartRingApiException notFound(String resource, String id) {
        return new SmartRingApiException(
            SmartRingConstants.ERROR_CODE_NOT_FOUND,
            String.format("%s с ID %s не найден", resource, id),
            HttpStatus.NOT_FOUND
        );
    }

    public static SmartRingApiException badRequest(String message) {
        return new SmartRingApiException(
            SmartRingConstants.ERROR_CODE_BAD_REQUEST,
            message,
            HttpStatus.BAD_REQUEST
        );
    }

    public static SmartRingApiException unauthorized(String message) {
        return new SmartRingApiException(
            SmartRingConstants.ERROR_CODE_UNAUTHORIZED,
            message,
            HttpStatus.UNAUTHORIZED
        );
    }

    public static SmartRingApiException forbidden(String message) {
        return new SmartRingApiException(
            SmartRingConstants.ERROR_CODE_FORBIDDEN,
            message,
            HttpStatus.FORBIDDEN
        );
    }

    public static SmartRingApiException conflict(String message) {
        return new SmartRingApiException(
            SmartRingConstants.ERROR_CODE_CONFLICT,
            message,
            HttpStatus.CONFLICT
        );
    }

    public static SmartRingApiException rateLimitExceeded(String message) {
        return new SmartRingApiException(
            SmartRingConstants.ERROR_CODE_RATE_LIMIT,
            message,
            HttpStatus.TOO_MANY_REQUESTS
        );
    }

    public static SmartRingApiException internalError(String message, Throwable cause) {
        return new SmartRingApiException(
            SmartRingConstants.ERROR_CODE_INTERNAL_ERROR,
            message,
            HttpStatus.INTERNAL_SERVER_ERROR,
            cause
        );
    }

    public static SmartRingApiException serviceUnavailable(String message) {
        return new SmartRingApiException(
            SmartRingConstants.ERROR_CODE_SERVICE_UNAVAILABLE,
            message,
            HttpStatus.SERVICE_UNAVAILABLE
        );
    }

    public static SmartRingApiException timeoutError(String message) {
        return new SmartRingApiException(
            SmartRingConstants.ERROR_CODE_TIMEOUT,
            "Таймаут запроса: " + message,
            HttpStatus.GATEWAY_TIMEOUT,
            null
        );
    }

    public static SmartRingApiException connectionError(String message, Throwable cause) {
        return new SmartRingApiException(
            SmartRingConstants.ERROR_CODE_CONNECTION_ERROR,
            "Ошибка соединения: " + message,
            HttpStatus.SERVICE_UNAVAILABLE,
            cause
        );
    }
}