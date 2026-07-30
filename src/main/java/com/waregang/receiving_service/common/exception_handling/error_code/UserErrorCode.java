package com.waregang.receiving_service.common.exception_handling.error_code;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum UserErrorCode implements ErrorCode {
    USER_ALREADY_EXISTS(HttpStatus.CONFLICT, "user.already-exists");

    private final HttpStatus httpStatus;
    private final String code;
}
