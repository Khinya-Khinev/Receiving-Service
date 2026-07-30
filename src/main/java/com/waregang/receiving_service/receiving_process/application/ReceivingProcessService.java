package com.waregang.receiving_service.receiving_process.application;

import com.waregang.receiving_service.common.exception_handling.AppException;
import com.waregang.receiving_service.common.exception_handling.error_code.AsnErrorCode;
import com.waregang.receiving_service.common.exception_handling.error_code.ReceivingErrorCode;
import com.waregang.receiving_service.advanced_shipping_notice.application.AdvancedShippingNoticeService;
import com.waregang.receiving_service.advanced_shipping_notice.domain.model.AdvancedShippingNoticeJpa;
import com.waregang.receiving_service.receiving_process.api.dto.*;
import com.waregang.receiving_service.receiving_process.domain.model.*;
import com.waregang.receiving_service.receiving_process.domain.ports.ReceivedContentRepositoryPort;
import com.waregang.receiving_service.receiving_process.domain.ports.ReceivedUnitRepositoryPort;
import com.waregang.receiving_service.receiving_process.domain.ports.WorkerReceivingSessionRepositoryPort;
import com.waregang.receiving_service.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@RequiredArgsConstructor
@Service
public class ReceivingProcessService {

    private final WorkerReceivingSessionRepositoryPort workerSessionRepository;
    private final ReceivedUnitRepositoryPort receivedUnitRepository;
    private final ReceivedContentRepositoryPort receivedContentRepository;

    private final GoodsReceiptService goodsReceiptService;
    private final AdvancedShippingNoticeService asnService;

    @Transactional
    public JoinReceivingResponse joinReceiving(UserPrincipal worker, UUID receiptId) {
        if (workerSessionRepository.existsByWorkerIdAndStatus(worker.id(), WorkerReceivingSessionStatus.IN_PROCESS))
            throw AppException.of(ReceivingErrorCode.WORKER_ALREADY_JOINED);

        GoodsReceipt receipt = goodsReceiptService.findReceiptByIdWithLock(receiptId);
        receipt.ensureAvailableForJoin(worker);

        AdvancedShippingNoticeJpa asn = asnService.findById(receipt.getInboundDeliveryId());

        var newSession = WorkerReceivingSession.createWithBundledWorker(
                worker,
                receipt.getId(),
                asn.getReceivingMode(),
                asn.getId()
        );

        WorkerReceivingSession savedSession = workerSessionRepository.save(newSession);

        return new JoinReceivingResponse(savedSession.getId());
    }

    @Transactional
    public ScanHandlingUnitResponse scanHandlingUnit(
            ScanHandlingUnitRequest scanRequest,
            UserPrincipal worker
    ) {
        WorkerReceivingSession session = findActiveSessionByWorkerIdWithLock(worker.id());
        session.ensureAvailableForHandlingUnitScan();

        ReceivedUnit unit = ReceivedUnit.create(
                scanRequest.lpn(),
                session.getCurrentUnitId(),
                session.getId(),
                session.getReceiptId()
        );
  
        receivedUnitRepository.save(unit);

        session.navigateToUnit(unit.getId(), unit.getLpn());
        workerSessionRepository.update(session);

        return new ScanHandlingUnitResponse(session.getCurrentUnitLpnPath());
    }

    @Transactional
    public ScanContentResponse scanContent(
            ScanContentRequest scanRequest,
            UserPrincipal worker
    ) {
        WorkerReceivingSession session = findActiveSessionByWorkerIdWithLock(worker.id());
        session.ensureAvailableForContentScan();

        ReceivedContent content = ReceivedContent.create(
                scanRequest.sku(),
                scanRequest.quantity(),
                session.getCurrentUnitId()
        );

        receivedContentRepository.save(content);

        return new ScanContentResponse();
    }

    @Transactional
    public NavigationBackResponse getBackToPreviousUnit(UserPrincipal worker) {
        WorkerReceivingSession session = findActiveSessionByWorkerIdWithLock(worker.id());

        UUID parentUnitId = receivedUnitRepository.findById(session.getCurrentUnitId())
                .map(ReceivedUnit::getParentUnitId)
                .orElse(null);

        session.navigateBack(parentUnitId);

        workerSessionRepository.update(session);

        return new NavigationBackResponse(session.getCurrentUnitLpnPath());
    }

    @Transactional
    public void completeWorkerSession(UserPrincipal worker) {
        WorkerReceivingSession session = findActiveSessionByWorkerIdWithLock(worker.id());

        session.complete();

        workerSessionRepository.update(session);
    }

    @Transactional(readOnly = true)
    public LpnInAsnResponse checkIfLpnInAsn(UserPrincipal worker, String lpn) {
        try {
            WorkerReceivingSession workerSession = findActiveSessionByWorkerId(worker.id());

            asnService.validateScannedHuAgainstAsn(lpn, workerSession.getInboundDeliveryId());

            return new LpnInAsnResponse(lpn, true);

        } catch (AppException e) {
            if (e.getErrorCode() == AsnErrorCode.SKU_NOT_IN_ASN)
                return new LpnInAsnResponse(lpn, false);
            else
                throw e;
        }
    }

    @Transactional(readOnly = true)
    public SkuInAsnResponse checkIfSkuInAsn(UserPrincipal worker, String sku) {
        try {
            WorkerReceivingSession workerSession = findActiveSessionByWorkerId(worker.id());

            asnService.validateScannedContentAgainstAsn(sku, workerSession.getInboundDeliveryId());

            return new SkuInAsnResponse(sku, true);

        } catch (AppException e) {
            if (e.getErrorCode() == AsnErrorCode.SKU_NOT_IN_ASN)
                return new SkuInAsnResponse(sku, false);
            else
                throw e;
        }
    }

    private WorkerReceivingSession findActiveSessionByWorkerIdWithLock(UUID workerId) {
        WorkerReceivingSession workerSession = workerSessionRepository
                .findByWorkerIdWithLock(workerId)
                .orElseThrow(() -> AppException.of(ReceivingErrorCode.WORKER_SESSION_NOT_FOUND)
                        .with("worker_id", workerId)
                );
        if (workerSession.getStatus() != WorkerReceivingSessionStatus.IN_PROCESS)
            throw AppException.of(ReceivingErrorCode.WORKER_SESSION_INVALID_STATE)
                    .with("worker_id", workerId)
                    .with("actual_status", workerSession.getStatus())
                    .with("expected_status", WorkerReceivingSessionStatus.IN_PROCESS);

        return workerSession;
    }

    private WorkerReceivingSession findActiveSessionByWorkerId(UUID workerId) {
        WorkerReceivingSession workerSession = workerSessionRepository
                .findByWorkerId(workerId)
                .orElseThrow(() -> AppException.of(ReceivingErrorCode.WORKER_SESSION_NOT_FOUND)
                        .with("worker_id", workerId)
                );
        if (workerSession.getStatus() != WorkerReceivingSessionStatus.IN_PROCESS)
            throw AppException.of(ReceivingErrorCode.WORKER_SESSION_INVALID_STATE)
                    .with("worker_id", workerId)
                    .with("actual_status", workerSession.getStatus())
                    .with("expected_status", WorkerReceivingSessionStatus.IN_PROCESS);

        return workerSession;
    }
}