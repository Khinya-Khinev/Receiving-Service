package com.waregang.receiving_service.common.idempotency;

public class MissingIdempotencyHeaderException extends RuntimeException {
    public MissingIdempotencyHeaderException(String message) {
        super(message);
    }
}
