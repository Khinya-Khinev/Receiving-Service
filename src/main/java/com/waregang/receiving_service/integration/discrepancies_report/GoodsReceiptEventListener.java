package com.waregang.receiving_service.integration.discrepancies_report;

import com.waregang.receiving_service.integration.discrepancies_report.application.DiscrepanciesReportService;
import com.waregang.receiving_service.receiving_process.domain.event.ClosedGoodsReceiptEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@RequiredArgsConstructor

@Component
public class GoodsReceiptEventListener {
    private final DiscrepanciesReportService reportService;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onGoodsReceiptClosedEvent (ClosedGoodsReceiptEvent event) {

        reportService.processClosedEvent(event);
    }
}
