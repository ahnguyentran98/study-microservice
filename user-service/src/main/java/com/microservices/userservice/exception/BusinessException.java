package com.microservices.userservice.exception;

/**
 * Thrown when a business rule is violated (e.g. duplicate email on registration).
 */
public class BusinessException extends RuntimeException {

    public BusinessException(String message) {
        super(message);
    }

    public BusinessException(String message, Throwable cause) {
        super(message, cause);
    }
}
