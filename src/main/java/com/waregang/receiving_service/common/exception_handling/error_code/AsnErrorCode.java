package com.waregang.receiving_service.common.exception_handling.error_code;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum AsnErrorCode implements ErrorCode {

    ASN_NOT_FOUND(HttpStatus.NOT_FOUND, "asn.not-found"),
    PARENT_LPN_MISSING(HttpStatus.BAD_REQUEST, "asn.parent-lpn-missing"),
    INVALID_STATE(HttpStatus.CONFLICT, "asn.illegal-state" ),
    LPN_NOT_IN_ASN(HttpStatus.BAD_REQUEST, "asn.lpn-not-in-asn"),
    SKU_NOT_IN_ASN(HttpStatus.BAD_REQUEST, "asn.sku-not-in-asn");

    private final HttpStatus httpStatus;
    private final String code;
}