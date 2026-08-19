package com.waregang.receiving_service.common.exception_handling;

import com.waregang.receiving_service.common.exception_handling.error_code.DatabaseErrorCode;
import com.waregang.receiving_service.common.exception_handling.error_code.ErrorCode;
import com.waregang.receiving_service.common.exception_handling.error_code.ReceivingErrorCode;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class DatabaseExceptionTranslator {

    private static final Map<String, ErrorCode> CONSTRAINT_MAPPING = Map.of(
            "uq_asn_asn_number", DatabaseErrorCode.ASN_NUMBER_ALREADY_EXISTS,
            "uq_asn_external_id", DatabaseErrorCode.ASN_EXTERNAL_ID_ALREADY_EXISTS,
            "uq_handling_units_lpn", DatabaseErrorCode.LPN_ALREADY_EXISTS,
            "uq_worker_active_session", DatabaseErrorCode.WORKER_ACTIVE_SESSION_ALREADY_EXISTS,
            "uq_receipt_lpn", DatabaseErrorCode.RECEIPT_LPN_ALREADY_EXISTS,
            "uk_unit_sku", ReceivingErrorCode.DUPLICATE_SKU_SCAN
    );

    public AppException translate(DataIntegrityViolationException e) {
        if (e.getRootCause() instanceof ConstraintViolationException cve && cve.getConstraintName() != null) {
            ErrorCode errorCode = CONSTRAINT_MAPPING.get(cve.getConstraintName());
            if (errorCode != null) {
                return AppException.of(errorCode);
            }

            return AppException.of(DatabaseErrorCode.CONSTRAINT_VIOLATION)
                    .with("constraintName", cve.getConstraintName());
        }
        return AppException.of(DatabaseErrorCode.CONSTRAINT_VIOLATION)
                .with("detail", "Database operation failed");
    }
}
