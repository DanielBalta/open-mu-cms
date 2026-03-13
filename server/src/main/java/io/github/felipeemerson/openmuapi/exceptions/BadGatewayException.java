package io.github.felipeemerson.openmuapi.exceptions;

public class BadGatewayException extends RuntimeException {

    public BadGatewayException() {
        super();
    }

    public BadGatewayException(String message) {
        super(message);
    }

    public BadGatewayException(String message, Throwable cause) {
        super(message, cause);
    }

}
