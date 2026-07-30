package com.waregang.receiving_service.common.exception_handling.error_code;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum DatabaseErrorCode implements ErrorCode {
    ASN_NUMBER_ALREADY_EXISTS(HttpStatus.CONFLICT, "asn.number-already-exists"),
    ASN_EXTERNAL_ID_ALREADY_EXISTS(HttpStatus.CONFLICT, "asn.external-id-already-exists"),
    LPN_ALREADY_EXISTS(HttpStatus.CONFLICT, "lpn-already-exists"),
    WORKER_ACTIVE_SESSION_ALREADY_EXISTS(HttpStatus.CONFLICT, "worker-session-already-exists"),
    RECEIPT_LPN_ALREADY_EXISTS(HttpStatus.CONFLICT, "receipt-lpn-already-exists"),
    CONSTRAINT_VIOLATION(HttpStatus.CONFLICT, "bad request???");

    private final HttpStatus httpStatus;
    private final String code;
}
