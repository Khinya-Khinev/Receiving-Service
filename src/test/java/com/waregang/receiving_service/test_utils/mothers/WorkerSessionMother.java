package com.waregang.receiving_service.test_utils.mothers;

import com.waregang.receiving_service.receiving_process.domain.model.WorkerReceivingSession;
import com.waregang.receiving_service.receiving_process.domain.model.WorkerReceivingSessionStatus;
import com.waregang.receiving_service.test_utils.builders.WorkerReceivingSessionBuilder;

import java.util.UUID;

public class WorkerSessionMother {

    /** Только что открытая сессия оператора (без выбранного юнита) */
    public static WorkerReceivingSession newWorkerSession(UUID receiptId, UUID deliveryId, UUID workerId) {
        return WorkerReceivingSessionBuilder.aWorkerSession()
                .withReceiptId(receiptId)
                .withInboundDeliveryId(deliveryId)
                .withWorkerId(workerId)
                .withStatus(WorkerReceivingSessionStatus.IN_PROCESS)
                .build();
    }

    /** Сессия с активным фокусом на конкретном грузоместе (например, паллете) */
    public static WorkerReceivingSession sessionFocusedOnUnit(
            UUID receiptId, 
            UUID deliveryId, 
            UUID workerId, 
            UUID unitId, 
            String lpnPath
    ) {
        return WorkerReceivingSessionBuilder.aWorkerSession()
                .withReceiptId(receiptId)
                .withInboundDeliveryId(deliveryId)
                .withWorkerId(workerId)
                .withStatus(WorkerReceivingSessionStatus.IN_PROCESS)
                .withCurrentUnit(lpnPath, unitId)
                .build();
    }

    /** Завершенная сессия */
    public static WorkerReceivingSession closedSession(UUID receiptId, UUID deliveryId, UUID workerId) {
        return WorkerReceivingSessionBuilder.aWorkerSession()
                .withReceiptId(receiptId)
                .withInboundDeliveryId(deliveryId)
                .withWorkerId(workerId)
                .withStatus(WorkerReceivingSessionStatus.COMPLETED)
                .build();
    }
}
