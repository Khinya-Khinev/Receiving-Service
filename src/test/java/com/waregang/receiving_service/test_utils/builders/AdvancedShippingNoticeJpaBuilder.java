package com.waregang.receiving_service.test_utils.builders;

import com.waregang.receiving_service.advanced_shipping_notice.domain.model.AdvancedShippingNoticeJpa;
import com.waregang.receiving_service.advanced_shipping_notice.domain.model.AdvancedShippingNoticeStatus;
import com.waregang.receiving_service.advanced_shipping_notice.domain.model.HandlingUnitJpa;
import com.waregang.receiving_service.receiving_process.domain.model.ReceivingMode;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class AdvancedShippingNoticeJpaBuilder {
    private UUID id = UUID.randomUUID();
    private String externalId = "EXT-" + UUID.randomUUID().toString().substring(0, 8);
    private String asnNumber = "ASN-" + UUID.randomUUID().toString().substring(0, 8);
    private String warehouseId = "WH-001";
    private ReceivingMode receivingMode = ReceivingMode.ASN_MATCHING;
    private AdvancedShippingNoticeStatus status = AdvancedShippingNoticeStatus.EXPECTED;
    private Integer version = 0;
    private boolean isNew = true;
    private String vendorName = "Test Vendor";
    private LocalDateTime expectedArrivalDate = LocalDateTime.now().plusDays(1);
    private LocalDateTime actualArrivalDate = null;

    private final List<HandlingUnitJpa> handlingUnits = new ArrayList<>();

    public static AdvancedShippingNoticeJpaBuilder anAdvancedShippingNotice() {
        return new AdvancedShippingNoticeJpaBuilder();
    }

    public AdvancedShippingNoticeJpaBuilder withId(UUID id) {
        this.id = id;
        return this;
    }

    public AdvancedShippingNoticeJpaBuilder withExternalId(String externalId) {
        this.externalId = externalId;
        return this;
    }

    public AdvancedShippingNoticeJpaBuilder withAsnNumber(String asnNumber) {
        this.asnNumber = asnNumber;
        return this;
    }

    public AdvancedShippingNoticeJpaBuilder withWarehouseId(String warehouseId) {
        this.warehouseId = warehouseId;
        return this;
    }

    public AdvancedShippingNoticeJpaBuilder withReceivingMode(ReceivingMode receivingMode) {
        this.receivingMode = receivingMode;
        return this;
    }

    public AdvancedShippingNoticeJpaBuilder withStatus(AdvancedShippingNoticeStatus status) {
        this.status = status;
        return this;
    }

    public AdvancedShippingNoticeJpaBuilder withVersion(Integer version) {
        this.version = version;
        return this;
    }

    public AdvancedShippingNoticeJpaBuilder asNew(boolean isNew) {
        this.isNew = isNew;
        return this;
    }

    public AdvancedShippingNoticeJpaBuilder withHandlingUnit(HandlingUnitJpa handlingUnit) {
        this.handlingUnits.add(handlingUnit);
        return this;
    }
    
    public AdvancedShippingNoticeJpaBuilder withVendorName(String vendorName) {
        this.vendorName = vendorName;
        return this;
    }

    public AdvancedShippingNoticeJpaBuilder withExpectedArrivalDate(LocalDateTime expectedArrivalDate) {
        this.expectedArrivalDate = expectedArrivalDate;
        return this;
    }

    public AdvancedShippingNoticeJpaBuilder withActualArrivalDate(LocalDateTime actualArrivalDate) {
        this.actualArrivalDate = actualArrivalDate;
        return this;
    }

    public AdvancedShippingNoticeJpa build() {
        AdvancedShippingNoticeJpa asn = AdvancedShippingNoticeJpa.reconstitute(
                id,
                externalId,
                asnNumber,
                warehouseId,
                receivingMode,
                status,
                version,
                vendorName,
                expectedArrivalDate,
                actualArrivalDate
        );
        asn.setNew(isNew);
        handlingUnits.forEach(asn::addHandlingUnit);
        return asn;
    }
}
