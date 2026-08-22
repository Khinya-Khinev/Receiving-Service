package com.waregang.receiving_service.test_utils.builders;

import com.waregang.receiving_service.receiving_process.domain.model.ReceivingMode;
import com.waregang.receiving_service.receiving_process.domain.model.WorkerReceivingSession;
import com.waregang.receiving_service.receiving_process.domain.model.WorkerReceivingSessionStatus;
import com.waregang.receiving_service.security.UserPrincipal;
import org.jspecify.annotations.Nullable;

import java.time.LocalDateTime;
import java.util.UUID;

public class WorkerReceivingSessionBuilder {
    private UUID id = UUID.randomUUID();
    private UUID workerId = UUID.randomUUID();
    private UUID receiptId = UUID.randomUUID();
    private UUID inboundDeliveryId = UUID.randomUUID();
    private WorkerReceivingSessionStatus status = WorkerReceivingSessionStatus.IN_PROCESS;
    private ReceivingMode receivingMode = ReceivingMode.ASN_MATCHING;
    private String currentUnitLpnPath = null;
    private UUID currentUnitId = null;
    private LocalDateTime startedAt = LocalDateTime.now();
    @Nullable
    private LocalDateTime completedAt = null;

    public static WorkerReceivingSessionBuilder aWorkerSession() {
        return new WorkerReceivingSessionBuilder();
    }

    public WorkerReceivingSessionBuilder withId(UUID id) {
        this.id = id;
        return this;
    }

    public WorkerReceivingSessionBuilder withWorkerId(UUID workerId) {
        this.workerId = workerId;
        return this;
    }

    public WorkerReceivingSessionBuilder withStatus(WorkerReceivingSessionStatus status) {
        this.status = status;
        return this;
    }

    public WorkerReceivingSessionBuilder withReceiptId(UUID receiptId) {
        this.receiptId = receiptId;
        return this;
    }

    public WorkerReceivingSessionBuilder withInboundDeliveryId(UUID inboundDeliveryId) {
        this.inboundDeliveryId = inboundDeliveryId;
        return this;
    }

    public WorkerReceivingSessionBuilder withCurrentUnit(String lpnPath, UUID currentUnitId) {
        this.currentUnitLpnPath = lpnPath;
        this.currentUnitId = currentUnitId;
        return this;
    }

    public WorkerReceivingSessionBuilder withReceivingMode(ReceivingMode receivingMode) {
        this.receivingMode = receivingMode;
        return this;
    }
    
    public WorkerReceivingSessionBuilder withStartedAt(LocalDateTime startedAt) {
        this.startedAt = startedAt;
        return this;
    }

    public WorkerReceivingSessionBuilder withCompletedAt(@Nullable LocalDateTime completedAt) {
        this.completedAt = completedAt;
        return this;
    }

    public WorkerReceivingSession build() {
        return WorkerReceivingSession.reconstitute(
                id, workerId, receiptId, inboundDeliveryId, status,
                receivingMode, currentUnitLpnPath, currentUnitId,
                startedAt, completedAt
        );
    }
}
