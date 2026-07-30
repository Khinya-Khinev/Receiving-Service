package com.waregang.receiving_service.receiving_process.domain.dto;

import com.waregang.receiving_service.receiving_process.domain.model.ReceivingMode;

import java.util.UUID;

public record AsnDto(
        UUID id,
        String asnNumber,
        String warehouseId,
        ReceivingMode receivingMode
) {
}