package ru.sber.apm.aipay.ratatouille.exception.crossover;

import lombok.Getter;
import lombok.experimental.Accessors;
import org.springframework.http.HttpStatus;
import ru.sber.apm.aipay.ratatouille.util.crossover.CrossoverConstants;

@Getter
@Accessors(chain = true)
public class CrossoverApiException extends RuntimeException {

    private final Integer errorCode;
    private final HttpStatus httpStatus;
    private final String originalMessage;

    public CrossoverApiException(Integer errorCode, String message, HttpStatus httpStatus) {
        super(message);
        this.errorCode = errorCode;
        this.httpStatus = httpStatus;
        this.originalMessage = message;
    }

    public CrossoverApiException(Integer errorCode, String message, HttpStatus httpStatus, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
        this.httpStatus = httpStatus;
        this.originalMessage = message;
    }

    // === Фабричные методы ===
    
    public static CrossoverApiException notFound(String resource, String id) {
        return new CrossoverApiException(
            CrossoverConstants.ERROR_CODE_NOT_FOUND,
            String.format("%s с ID %s не найден", resource, id),
            HttpStatus.NOT_FOUND
        );
    }

    public static CrossoverApiException badRequest(String message) {
        return new CrossoverApiException(
            CrossoverConstants.ERROR_CODE_BAD_REQUEST,
            message,
            HttpStatus.BAD_REQUEST
        );
    }

    public static CrossoverApiException unauthorized(String message) {
        return new CrossoverApiException(
            CrossoverConstants.ERROR_CODE_UNAUTHORIZED,
            message,
            HttpStatus.UNAUTHORIZED
        );
    }

    public static CrossoverApiException forbidden(String message) {
        return new CrossoverApiException(
            CrossoverConstants.ERROR_CODE_FORBIDDEN,
            message,
            HttpStatus.FORBIDDEN
        );
    }

    public static CrossoverApiException conflict(String message) {
        return new CrossoverApiException(
            CrossoverConstants.ERROR_CODE_CONFLICT,
            message,
            HttpStatus.CONFLICT
        );
    }

    public static CrossoverApiException rateLimitExceeded(String message) {
        return new CrossoverApiException(
            CrossoverConstants.ERROR_CODE_RATE_LIMIT,
            message,
            HttpStatus.TOO_MANY_REQUESTS
        );
    }

    public static CrossoverApiException internalError(String message, Throwable cause) {
        return new CrossoverApiException(
            CrossoverConstants.ERROR_CODE_INTERNAL_ERROR,
            message,
            HttpStatus.INTERNAL_SERVER_ERROR,
            cause
        );
    }

    public static CrossoverApiException serviceUnavailable(String message) {
        return new CrossoverApiException(
            CrossoverConstants.ERROR_CODE_SERVICE_UNAVAILABLE,
            message,
            HttpStatus.SERVICE_UNAVAILABLE
        );
    }

    public static CrossoverApiException gatewayError(String message) {
        return new CrossoverApiException(
            CrossoverConstants.ERROR_CODE_GATEWAY_ERROR,
            message,
            HttpStatus.BAD_GATEWAY
        );
    }

    public static CrossoverApiException connectionError(String message, Throwable cause) {
        return new CrossoverApiException(
            CrossoverConstants.ERROR_CODE_CONNECTION_ERROR,
            "Ошибка соединения: " + message,
            HttpStatus.SERVICE_UNAVAILABLE,
            cause
        );
    }

    public static CrossoverApiException timeoutError(String message) {
        return new CrossoverApiException(
            CrossoverConstants.ERROR_CODE_TIMEOUT,
            "Таймаут запроса: " + message,
            HttpStatus.GATEWAY_TIMEOUT,
            null
        );
    }
}