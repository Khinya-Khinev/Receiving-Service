package com.waregang.receiving_service.common.exception_handling.error_code;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@RequiredArgsConstructor
public enum ValidationErrorCode implements ErrorCode{
    INVALID_DATE_RANGE(HttpStatus.BAD_REQUEST, "validation.invalid_date_range"),
    INVALID_SORT_PROPERTY(HttpStatus.BAD_REQUEST, "validation.invalid_sort_field");

    @Override
    public HttpStatus getHttpStatus() {
        return null;
    }

    @Override
    public String getCode() {
        return "";
    }

    private final HttpStatus httpStatus;
    private final String code;
}
