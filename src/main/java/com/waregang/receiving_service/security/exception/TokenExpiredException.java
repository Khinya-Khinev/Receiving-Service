package com.waregang.receiving_service.security.exception;

import org.springframework.security.core.AuthenticationException;

public class TokenExpiredException extends AuthenticationException {
    private final String reason;

    public TokenExpiredException(String msg, String reason) {
        super(msg);
        this.reason = reason;
    }

    public String getReason() {
        return reason;
    }
}
