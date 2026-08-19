package com.waregang.receiving_service.receiving_process.api.dto;

import com.waregang.receiving_service.receiving_process.domain.model.ReceivingMode;
import com.waregang.receiving_service.receiving_process.domain.model.WorkerReceivingSessionStatus;
import org.jspecify.annotations.Nullable;

import java.util.UUID;

public record WorkerReceivingSessionResponse(
        UUID id,
        UUID workerId,
        UUID receiptId,
        UUID inboundDeliveryId,
        WorkerReceivingSessionStatus status,
        ReceivingMode receivingMode,
        @Nullable String currentUnitLpnPath,
        @Nullable UUID currentUnitId
) {}
