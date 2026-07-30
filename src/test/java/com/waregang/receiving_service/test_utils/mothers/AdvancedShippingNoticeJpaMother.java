package com.waregang.receiving_service.test_utils.mothers;

import com.waregang.receiving_service.advanced_shipping_notice.domain.model.AdvancedShippingNoticeJpa;
import com.waregang.receiving_service.advanced_shipping_notice.domain.model.AdvancedShippingNoticeStatus;
import com.waregang.receiving_service.advanced_shipping_notice.domain.model.HandlingUnitJpa;
import com.waregang.receiving_service.test_utils.builders.AdvancedShippingNoticeJpaBuilder;
import com.waregang.receiving_service.test_utils.builders.ContentJpaBuilder;
import com.waregang.receiving_service.test_utils.builders.HandlingUnitJpaBuilder;

public class AdvancedShippingNoticeJpaMother {

    /** Ожидаемая поставка по умолчанию */
    public static AdvancedShippingNoticeJpa expectedAsn(String warehouseId) {
        return AdvancedShippingNoticeJpaBuilder.anAdvancedShippingNotice()
                .withWarehouseId(warehouseId)
                .withStatus(AdvancedShippingNoticeStatus.EXPECTED)
                .build();
    }

    /** Уведомление, которое уже прибыло на склад */
    public static AdvancedShippingNoticeJpa arrivedAsn(String warehouseId) {
        return AdvancedShippingNoticeJpaBuilder.anAdvancedShippingNotice()
                .withWarehouseId(warehouseId)
                .withStatus(AdvancedShippingNoticeStatus.ARRIVED)
                .asNew(false)
                .build();
    }

    /** Поставка с заполненными грузовыми местами и товарами */
    public static AdvancedShippingNoticeJpa asnWithPalletAndContent(String warehouseId, String lpn, String sku, Long quantity) {
        AdvancedShippingNoticeJpa asn = expectedAsn(warehouseId);

        HandlingUnitJpa pallet = HandlingUnitJpaBuilder.aHandlingUnit()
                .withLpn(lpn)
                .withAsn(asn)
                .withContent(
                        ContentJpaBuilder.aContent()
                                .withSku(sku)
                                .withQuantity(quantity)
                                .build()
                )
                .build();

        asn.addHandlingUnit(pallet);
        return asn;
    }
}
