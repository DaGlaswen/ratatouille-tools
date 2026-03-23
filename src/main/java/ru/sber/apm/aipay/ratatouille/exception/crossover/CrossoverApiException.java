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

    public static CrossoverApiException internalError(String message, Throwable cause) {
        return new CrossoverApiException(
            CrossoverConstants.ERROR_CODE_INTERNAL_ERROR,
            message,
            HttpStatus.INTERNAL_SERVER_ERROR,
            cause
        );
    }

    public static CrossoverApiException sslError(String message, Throwable cause) {
        return new CrossoverApiException(
            CrossoverConstants.ERROR_CODE_SSL_ERROR,
            "SSL ошибка: " + message,
            HttpStatus.SERVICE_UNAVAILABLE,
            cause
        );
    }
}