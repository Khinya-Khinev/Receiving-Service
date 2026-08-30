package com.waregang.receiving_service.receiving_process.application;

import com.waregang.receiving_service.common.exception_handling.AppException;
import com.waregang.receiving_service.common.exception_handling.error_code.ReceivingErrorCode;
import com.waregang.receiving_service.receiving_process.api.dto.GoodsReceiptDetailsResponse;
import com.waregang.receiving_service.receiving_process.api.dto.ReceivedContentDto;
import com.waregang.receiving_service.receiving_process.api.dto.ReceivedUnitDto;
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
import com.waregang.receiving_service.receiving_process.infrastructure.jpa_entities.GoodsReceiptJpa;
import com.waregang.receiving_service.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

import com.waregang.receiving_service.receiving_process.infrastructure.specifications.GoodsReceiptSpecification;

import static com.waregang.receiving_service.receiving_process.infrastructure.specifications.GoodsReceiptSpecification.hasStatus;
import static com.waregang.receiving_service.receiving_process.infrastructure.specifications.GoodsReceiptSpecification.hasWarehouseId;

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
                asn.id(),
                asn.warehouseId(),
                request.receivingMode(), // it can be different from ASN
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
    public Page<GoodsReceiptDto> findGoodsReceipts(UserPrincipal user, GoodsReceiptStatus status, Pageable pageable) {
        var spec = Specification
                .where(hasStatus(status))
                .and(hasWarehouseId(user.warehouseId()));

        Page<GoodsReceiptJpa> receipts = goodsReceiptRepositoryPort.findAll(spec, pageable);

        return receipts.map(jpa -> new GoodsReceiptDto(
                jpa.getId(),
                jpa.getStatus(),
                jpa.getWarehouseId(),
                jpa.getGateNumber(),
                jpa.getManagerId(),
                jpa.getReceivingMode(),
                jpa.getAsnId()
        ));
    }

    @Transactional(readOnly = true)
    public GoodsReceiptDetailsResponse getReceiptDetails(UUID receiptId) {
        goodsReceiptRepositoryPort.findById(receiptId)
                .orElseThrow(() -> AppException.of(ReceivingErrorCode.RECEIPT_NOT_FOUND)
                        .with("receipt_id", receiptId));

        List<ReceivedUnit> units = receivedUnitRepositoryPort.findAllByReceiptId(receiptId);

        List<ReceivedUnitDto> unitDtos = units.stream()
                .map(unit -> new ReceivedUnitDto(
                        "DEFAULT",
                        unit.getLpn(),
                        unit.getParentUnitId() != null ? findLpnById(unit.getParentUnitId(), units) : null
                ))
                .toList();

        List<ReceivedContentDto> contentDtos = units.stream()
                .flatMap(unit -> unit.getContents().stream())
                .map(content -> new ReceivedContentDto(
                        findLpnById(content.getContainerUnitId(), units),
                        content.getSku(),
                        content.getQuantity()
                ))
                .toList();

        return new GoodsReceiptDetailsResponse(unitDtos, contentDtos);
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