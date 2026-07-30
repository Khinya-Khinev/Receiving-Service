package com.waregang.receiving_service.test_utils.mothers;

import com.waregang.receiving_service.receiving_process.domain.model.ReceivedContent;
import com.waregang.receiving_service.receiving_process.domain.model.ReceivedUnit;
import com.waregang.receiving_service.test_utils.builders.ReceivedContentBuilder;
import com.waregang.receiving_service.test_utils.builders.ReceivedUnitBuilder;

import java.util.UUID;

public class ReceivedUnitMother {

    /** Пустая паллета (верхний уровень) */
    public static ReceivedUnit emptyPallet(String lpn, UUID receiptId, UUID sessionId) {
        return ReceivedUnitBuilder.aReceivedUnit()
                .withLpn(lpn)
                .withReceiptId(receiptId)
                .withWorkerSessionId(sessionId)
                .withParentUnitId(null)
                .build();
    }

    /** Коробка внутри паллеты */
    public static ReceivedUnit boxInsidePallet(String boxLpn, UUID parentPalletId, UUID receiptId, UUID sessionId) {
        return ReceivedUnitBuilder.aReceivedUnit()
                .withLpn(boxLpn)
                .withParentUnitId(parentPalletId)
                .withReceiptId(receiptId)
                .withWorkerSessionId(sessionId)
                .build();
    }

    /** Юнит с наполненным товаром */
    public static ReceivedUnit unitWithSingleSku(
            String lpn, 
            String sku, 
            Long qty, 
            UUID receiptId, 
            UUID sessionId
    ) {
        UUID unitId = UUID.randomUUID();
        ReceivedContent content = ReceivedContentBuilder.aReceivedContent()
                .withSku(sku)
                .withQuantity(qty)
                .withContainerUnitId(unitId)
                .build();

        return ReceivedUnitBuilder.aReceivedUnit()
                .withId(unitId)
                .withLpn(lpn)
                .withReceiptId(receiptId)
                .withWorkerSessionId(sessionId)
                .withContent(content)
                .build();
    }
}
