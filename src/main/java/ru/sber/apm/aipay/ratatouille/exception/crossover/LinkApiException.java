package ru.sber.apm.aipay.ratatouille.exception.crossover;

import lombok.Getter;
import lombok.experimental.Accessors;
import org.springframework.http.HttpStatus;
import ru.sber.apm.aipay.ratatouille.util.p2pcrypto.LinkConstants;

@Getter
@Accessors(chain = true)
public class LinkApiException extends RuntimeException {

    private final Integer errorCode;
    private final HttpStatus httpStatus;
    private final String originalMessage;

    public LinkApiException(Integer errorCode, String message, HttpStatus httpStatus) {
        super(message);
        this.errorCode = errorCode;
        this.httpStatus = httpStatus;
        this.originalMessage = message;
    }

    public LinkApiException(Integer errorCode, String message, HttpStatus httpStatus, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
        this.httpStatus = httpStatus;
        this.originalMessage = message;
    }

    // === Фабричные методы ===
    
    public static LinkApiException notFound(String resource, String id) {
        return new LinkApiException(
            LinkConstants.ERROR_CODE_NOT_FOUND,
            String.format("%s с ID %s не найден", resource, id),
            HttpStatus.NOT_FOUND
        );
    }

    public static LinkApiException badRequest(String message) {
        return new LinkApiException(
            LinkConstants.ERROR_CODE_BAD_REQUEST,
            message,
            HttpStatus.BAD_REQUEST
        );
    }

    public static LinkApiException unauthorized(String message) {
        return new LinkApiException(
            LinkConstants.ERROR_CODE_UNAUTHORIZED,
            message,
            HttpStatus.UNAUTHORIZED
        );
    }

    public static LinkApiException internalError(String message, Throwable cause) {
        return new LinkApiException(
            LinkConstants.ERROR_CODE_INTERNAL_ERROR,
            message,
            HttpStatus.INTERNAL_SERVER_ERROR,
            cause
        );
    }

    public static LinkApiException walletError(String message, Throwable cause) {
        return new LinkApiException(
            LinkConstants.ERROR_CODE_WALLET_ERROR,
            "Ошибка кошелька: " + message,
            HttpStatus.BAD_REQUEST,
            cause
        );
    }
}