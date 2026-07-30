package com.waregang.receiving_service.receiving_process.application;

import com.waregang.receiving_service.advanced_shipping_notice.api.dto.CreateContentRequest;
import com.waregang.receiving_service.advanced_shipping_notice.api.dto.CreateUnitRequest;
import com.waregang.receiving_service.common.exception_handling.AppException;
import com.waregang.receiving_service.common.exception_handling.error_code.ReceivingErrorCode;
import com.waregang.receiving_service.advanced_shipping_notice.application.AdvancedShippingNoticeService;
import com.waregang.receiving_service.advanced_shipping_notice.domain.model.AdvancedShippingNoticeJpa;
import com.waregang.receiving_service.receiving_process.api.dto.GetOpenedReceiptsResponse;
import com.waregang.receiving_service.receiving_process.api.dto.GoodsReceiptDetailsResponse;
import com.waregang.receiving_service.receiving_process.api.dto.StartReceivingRequest;
import com.waregang.receiving_service.receiving_process.api.dto.StartReceivingResponse;
import com.waregang.receiving_service.receiving_process.domain.model.GoodsReceipt;
import com.waregang.receiving_service.receiving_process.domain.model.GoodsReceiptStatus;
import com.waregang.receiving_service.receiving_process.domain.model.ReceivedUnit;
import com.waregang.receiving_service.receiving_process.domain.model.WorkerReceivingSessionStatus;
import com.waregang.receiving_service.receiving_process.domain.ports.GoodsReceiptRepositoryPort;
import com.waregang.receiving_service.receiving_process.domain.ports.ReceivedUnitRepositoryPort;
import com.waregang.receiving_service.receiving_process.domain.ports.WorkerReceivingSessionRepositoryPort;
import com.waregang.receiving_service.receiving_process.domain.dto.GoodsReceiptDto;
import com.waregang.receiving_service.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@Service
public class GoodsReceiptService {
    private final AdvancedShippingNoticeService asnService;

    private final GoodsReceiptRepositoryPort goodsReceiptRepositoryPort;

    private final WorkerReceivingSessionRepositoryPort workerSessionRepository;
    private final ReceivedUnitRepositoryPort receivedUnitRepositoryPort;

    // TODO:  mb add retryable later
    @Transactional
    public StartReceivingResponse startReceiving(
            StartReceivingRequest request,
            UserPrincipal manager
    ) {
        AdvancedShippingNoticeJpa asn = asnService.findByAsn(request.asnNumber());
        asnService.markAsArrived(asn, manager.warehouseId());

        GoodsReceipt receipt = GoodsReceipt.open(
                manager.id(),
                manager.nickname(),
                asn.getId(),
                asn.getWarehouseId(),
                asn.getReceivingMode(),
                asn.getAsnNumber(),
                request.gateNumber()
        );

        goodsReceiptRepositoryPort.save(receipt);

        return new StartReceivingResponse(receipt.getId(), asn.getReceivingMode());
    }

    @Transactional
    public void closeReceiving(UserPrincipal manager, UUID receiptId) {
        GoodsReceipt receipt = goodsReceiptRepositoryPort.findWithLockById(receiptId)
                .orElseThrow(() -> AppException.of(ReceivingErrorCode.RECEIPT_NOT_FOUND)
                        .with("receipt_id", receiptId));

        if (receipt.getStatus() != GoodsReceiptStatus.OPEN) {
            throw AppException.of(ReceivingErrorCode.RECEIPT_INVALID_STATE)
                    .with("expected_status", GoodsReceiptStatus.OPEN)
                    .with("actual_status", receipt.getStatus())
                    .with("receipt_id", receiptId);
        }

        // GoodsReceipt is a point of synchronization for itself and WorkerReceivingSession:
        // checking worker receiving session without locks because its creation is locked on GoodsReceipt as well
        if (workerSessionRepository.existsByReceiptIdAndStatus(
                receiptId,
                WorkerReceivingSessionStatus.IN_PROCESS
        )) {
            throw AppException.of(ReceivingErrorCode.RECEIPT_INVALID_STATE)
                    .with("receipt_id", receiptId)
                    .with("reason", "some workers joined receipt");
        }
        asnService.closeAsn(receipt.getInboundDeliveryId());
        receipt.close();

        goodsReceiptRepositoryPort.update(receipt);
    }

    @Transactional(readOnly = true)
    public GetOpenedReceiptsResponse findAllByStatus(UserPrincipal user, GoodsReceiptStatus receiptStatus) {
        List<GoodsReceiptDto> receipts = goodsReceiptRepositoryPort
                .findAllDtosByStatusAndWarehouseId(receiptStatus, user.warehouseId());

        return new GetOpenedReceiptsResponse(receipts);
    }

    @Transactional(readOnly = true)
    public GoodsReceiptDetailsResponse getReceiptDetails(UUID receiptId) {
        goodsReceiptRepositoryPort.findById(receiptId)
                .orElseThrow(() -> AppException.of(ReceivingErrorCode.RECEIPT_NOT_FOUND)
                        .with("receipt_id", receiptId));

        List<ReceivedUnit> units = receivedUnitRepositoryPort.findAllByReceiptId(receiptId);

        List<CreateUnitRequest> unitRequests = units.stream()
                .map(unit -> new CreateUnitRequest(
                        "DEFAULT",
                        unit.getLpn(),
                        unit.getParentUnitId() != null ? findLpnById(unit.getParentUnitId(), units) : null
                ))
                .toList();

        List<CreateContentRequest> contentRequests = units.stream()
                .flatMap(unit -> unit.getContents().stream())
                .map(content -> new CreateContentRequest(
                        findLpnById(content.getContainerUnitId(), units),
                        content.getSku(),
                        content.getQuantity()
                ))
                .toList();

        return new GoodsReceiptDetailsResponse(unitRequests, contentRequests);
    }

    private String findLpnById(UUID id, List<ReceivedUnit> units) {
        return units.stream()
                .filter(unit -> unit.getId().equals(id))
                .map(ReceivedUnit::getLpn)
                .findFirst()
                .orElse(null);
    }

    @Transactional(propagation = Propagation.MANDATORY)
    public GoodsReceipt findReceiptByIdWithLock(UUID receiptId) {
        return goodsReceiptRepositoryPort.findWithLockById(receiptId)
                .orElseThrow(() -> AppException.of(ReceivingErrorCode.RECEIPT_NOT_FOUND)
                        .with("receipt_id", receiptId));
    }
}