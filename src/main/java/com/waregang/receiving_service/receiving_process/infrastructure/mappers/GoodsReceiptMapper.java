package com.waregang.receiving_service.receiving_process.infrastructure.mappers;

import com.waregang.receiving_service.receiving_process.domain.model.GoodsReceipt;
import com.waregang.receiving_service.receiving_process.infrastructure.jpa_entities.GoodsReceiptJpa;
import org.springframework.stereotype.Component;

@Component
public class GoodsReceiptMapper {

    public GoodsReceipt toDomain(GoodsReceiptJpa jpa) {
        return GoodsReceipt.reconstitute(
                jpa.getId(),
                jpa.getStatus(),
                jpa.getGateNumber(),
                jpa.getManagerId(),
                jpa.getAsnId(),
                jpa.getWarehouseId(),
                jpa.getReceivingMode()
        );
    }

    public GoodsReceiptJpa toJpa(GoodsReceipt domain) {
        return GoodsReceiptJpa.fromDomain(
                domain.getId(),
                domain.getStatus(),
                domain.getGateNumber(),
                domain.getManagerId(),
                domain.getInboundDeliveryId(),
                domain.getWarehouseId(),
                domain.getReceivingMode()
        );
    }

    public void updateJpaFromDomain(GoodsReceiptJpa jpa, GoodsReceipt domain) {
        jpa.setStatus(domain.getStatus());
        jpa.setGateNumber(domain.getGateNumber());
        jpa.setManagerId(domain.getManagerId());
        jpa.setReceivingMode(domain.getReceivingMode());
    }
}