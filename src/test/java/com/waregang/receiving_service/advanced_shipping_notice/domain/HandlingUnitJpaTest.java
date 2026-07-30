package com.waregang.receiving_service.advanced_shipping_notice.domain;

import com.waregang.receiving_service.advanced_shipping_notice.domain.model.AdvancedShippingNoticeJpa;
import com.waregang.receiving_service.advanced_shipping_notice.domain.model.HandlingUnitJpa;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class HandlingUnitJpaTest {

    @Test
    @DisplayName("Should correctly create a unit with a path")
    void shouldCreateUnitWithPath() {
        AdvancedShippingNoticeJpa asn = AdvancedShippingNoticeJpa.create("EXT-1", "ASN-1", "WH-1", "Vendor", LocalDateTime.now());
        HandlingUnitJpa unit = HandlingUnitJpa.create("LPN-1", "LPN-1", null, asn);

        assertThat(unit.getPath()).isEqualTo("LPN-1");
        assertThat(unit.getParentUnit()).isNull();
    }

    @Test
    @DisplayName("Should correctly create a child unit with a path extending the parent")
    void shouldCreateChildUnitWithExtendedPath() {
        AdvancedShippingNoticeJpa asn = AdvancedShippingNoticeJpa.create("EXT-1", "ASN-1", "WH-1", "Vendor", LocalDateTime.now());
        HandlingUnitJpa parent = HandlingUnitJpa.create("LPN-1", "LPN-1", null, asn);
        HandlingUnitJpa child = HandlingUnitJpa.create("LPN-2", "LPN-1/LPN-2", parent, asn);

        assertThat(child.getPath()).isEqualTo("LPN-1/LPN-2");
        assertThat(child.getParentUnit()).isEqualTo(parent);
    }
}