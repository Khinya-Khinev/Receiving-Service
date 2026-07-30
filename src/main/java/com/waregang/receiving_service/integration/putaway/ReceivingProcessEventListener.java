package com.waregang.receiving_service.integration.putaway;

import com.waregang.receiving_service.integration.putaway.application.InventoryIntegrationService;
import com.waregang.receiving_service.receiving_process.domain.event.WorkerSessionClosedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@RequiredArgsConstructor

@Component
public class ReceivingProcessEventListener {
    private final InventoryIntegrationService inventoryIntegrationService;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onWorkerSessionClosedEvent (WorkerSessionClosedEvent event) {
        inventoryIntegrationService.submitForPutAway(event);
    }
}
