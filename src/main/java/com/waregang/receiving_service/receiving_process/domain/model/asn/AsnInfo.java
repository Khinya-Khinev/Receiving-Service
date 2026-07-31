package com.waregang.receiving_service.receiving_process.domain.model.asn;

import com.waregang.receiving_service.receiving_process.domain.model.ReceivingMode;

import java.util.UUID;

public record AsnInfo(
        UUID id,
        String warehouseId,
        ReceivingMode receivingMode,
        String asnNumber
) {}
