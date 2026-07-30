package com.waregang.receiving_service.test_utils.mothers;

import com.waregang.receiving_service.receiving_process.domain.model.GoodsReceipt;
import com.waregang.receiving_service.receiving_process.domain.model.GoodsReceiptStatus;
import com.waregang.receiving_service.test_utils.builders.GoodsReceiptBuilder;

import java.util.UUID;

public class GoodsReceiptMother {

    /** Открытый документ приемки для заданной поставки */
    public static GoodsReceipt openReceipt(UUID inboundDeliveryId, UUID managerId) {
        return GoodsReceiptBuilder.aGoodsReceipt()
                .withInboundDeliveryId(inboundDeliveryId)
                .withManagerId(managerId)
                .withStatus(GoodsReceiptStatus.OPEN)
                .build();
    }

    /** Закрытый документ приемки */
    public static GoodsReceipt closedReceipt(UUID inboundDeliveryId) {
        return GoodsReceiptBuilder.aGoodsReceipt()
                .withInboundDeliveryId(inboundDeliveryId)
                .withStatus(GoodsReceiptStatus.CLOSED)
                .build();
    }
}
