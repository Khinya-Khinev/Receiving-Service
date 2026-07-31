package com.waregang.receiving_service.receiving_process.application;

import com.waregang.receiving_service.common.exception_handling.AppException;
import com.waregang.receiving_service.common.exception_handling.error_code.ReceivingErrorCode;
import com.waregang.receiving_service.receiving_process.api.dto.GetOpenedReceiptsResponse;
import com.waregang.receiving_service.receiving_process.api.dto.GoodsReceiptDetailsResponse;
import com.waregang.receiving_service.receiving_process.api.dto.StartReceivingRequest;
import com.waregang.receiving_service.receiving_process.api.dto.StartReceivingResponse;
import com.waregang.receiving_service.receiving_process.domain.dto.GoodsReceiptDto;
import com.waregang.receiving_service.receiving_process.domain.model.GoodsReceipt;
import com.waregang.receiving_service.receiving_process.domain.model.GoodsReceiptStatus;
import com.waregang.receiving_service.receiving_process.domain.model.ReceivedUnit;
import com.waregang.receiving_service.receiving_process.domain.model.WorkerReceivingSessionStatus;
import com.waregang.receiving_service.receiving_process.domain.model.asn.AsnInfo;
import com.waregang.receiving_service.receiving_process.application.ports.AsnInfoProviderPort;
import com.waregang.receiving_service.receiving_process.application.ports.GoodsReceiptRepositoryPort;
import com.waregang.receiving_service.receiving_process.application.ports.ReceivedUnitRepositoryPort;
import com.waregang.receiving_service.receiving_process.application.ports.WorkerReceivingSessionRepositoryPort;
import com.waregang.receiving_service.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@Service
public class GoodsReceiptService {

    private final ApplicationEventPublisher eventPublisher;

    private final AsnInfoProviderPort asnInfoProvider;

    private final GoodsReceiptRepositoryPort goodsReceiptRepositoryPort;

    private final WorkerReceivingSessionRepositoryPort workerSessionRepository;
    private final ReceivedUnitRepositoryPort receivedUnitRepositoryPort;

    @Transactional
    public StartReceivingResponse startReceiving(
            StartReceivingRequest request,
            UserPrincipal manager
    ) {
        AsnInfo asn = asnInfoProvider.findAndMarkAsArrived(request.asnNumber(), manager.warehouseId());

        GoodsReceipt receipt = GoodsReceipt.open(
                manager.id(),
                manager.nickname(),
                asn.id(),
                asn.warehouseId(),
                asn.receivingMode(),
                asn.asnNumber(),
                request.gateNumber()
        );

        goodsReceiptRepositoryPort.save(receipt);

        receipt.pullDomainEvents().forEach(eventPublisher::publishEvent);

        return new StartReceivingResponse(receipt.getId(), asn.receivingMode());
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

        if (workerSessionRepository.existsByReceiptIdAndStatus(
                receiptId,
                WorkerReceivingSessionStatus.IN_PROCESS
        )) {
            throw AppException.of(ReceivingErrorCode.RECEIPT_INVALID_STATE)
                    .with("receipt_id", receiptId)
                    .with("reason", "some workers joined receipt");
        }
        asnInfoProvider.closeAsn(receipt.getInboundDeliveryId());
        receipt.close();

        goodsReceiptRepositoryPort.update(receipt);

        receipt.pullDomainEvents().forEach(eventPublisher::publishEvent);
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

        // The DTOs from the other context are still used here.
        // I will leave them for now as requested.
        List<com.waregang.receiving_service.advanced_shipping_notice.api.dto.CreateUnitRequest> unitRequests = units.stream()
                .map(unit -> new com.waregang.receiving_service.advanced_shipping_notice.api.dto.CreateUnitRequest(
                        "DEFAULT",
                        unit.getLpn(),
                        unit.getParentUnitId() != null ? findLpnById(unit.getParentUnitId(), units) : null
                ))
                .toList();

        List<com.waregang.receiving_service.advanced_shipping_notice.api.dto.CreateContentRequest> contentRequests = units.stream()
                .flatMap(unit -> unit.getContents().stream())
                .map(content -> new com.waregang.receiving_service.advanced_shipping_notice.api.dto.CreateContentRequest(
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