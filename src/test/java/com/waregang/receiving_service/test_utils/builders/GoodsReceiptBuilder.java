package com.waregang.receiving_service.test_utils.builders;

import com.waregang.receiving_service.receiving_process.domain.model.GoodsReceipt;
import com.waregang.receiving_service.receiving_process.domain.model.GoodsReceiptStatus;
import com.waregang.receiving_service.receiving_process.domain.model.ReceivingMode;
import java.util.UUID;

public class GoodsReceiptBuilder {
    private UUID id = UUID.randomUUID();
    private GoodsReceiptStatus status = GoodsReceiptStatus.OPEN;
    private String gateNumber = "GATE-01";
    private UUID managerId = UUID.randomUUID();
    private UUID inboundDeliveryId = UUID.randomUUID();
    private String warehouseId = "WH-001";
    private ReceivingMode receivingMode = ReceivingMode.ASN_MATCHING;

    public static GoodsReceiptBuilder aGoodsReceipt() {
        return new GoodsReceiptBuilder();
    }

    public GoodsReceiptBuilder withId(UUID id) {
        this.id = id;
        return this;
    }

    public GoodsReceiptBuilder withStatus(GoodsReceiptStatus status) {
        this.status = status;
        return this;
    }

    public GoodsReceiptBuilder withGateNumber(String gateNumber) {
        this.gateNumber = gateNumber;
        return this;
    }

    public GoodsReceiptBuilder withManagerId(UUID managerId) {
        this.managerId = managerId;
        return this;
    }

    public GoodsReceiptBuilder withInboundDeliveryId(UUID inboundDeliveryId) {
        this.inboundDeliveryId = inboundDeliveryId;
        return this;
    }

    public GoodsReceiptBuilder withWarehouseId(String warehouseId) {
        this.warehouseId = warehouseId;
        return this;
    }

    public GoodsReceipt build() {
        return GoodsReceipt.reconstitute(
                id, status, gateNumber, managerId, inboundDeliveryId, warehouseId, receivingMode
        );
    }

    public GoodsReceipt buildOpened() {
        return GoodsReceipt.open(
                managerId,
                inboundDeliveryId,
                warehouseId,
                ReceivingMode.ASN_MATCHING,
                "ASN",
                gateNumber
        );
    }
}