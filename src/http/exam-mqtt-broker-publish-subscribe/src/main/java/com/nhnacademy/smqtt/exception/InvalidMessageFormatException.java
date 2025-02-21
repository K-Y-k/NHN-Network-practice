package com.nhnacademy.smqtt.exception;

public class InvalidMessageFormatException extends RuntimeException {
    public InvalidMessageFormatException() {
        super();
    }

    public InvalidMessageFormatException(String message) {
        super(message);
    }

    public InvalidMessageFormatException(String message, Throwable cause) {
        super(message, cause);
    }
}
