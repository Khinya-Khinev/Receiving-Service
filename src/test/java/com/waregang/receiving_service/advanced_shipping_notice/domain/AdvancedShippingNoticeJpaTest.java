package com.waregang.receiving_service.advanced_shipping_notice.domain;

import com.waregang.receiving_service.common.exception_handling.AppException;
import com.waregang.receiving_service.advanced_shipping_notice.domain.model.AdvancedShippingNoticeJpa;
import com.waregang.receiving_service.advanced_shipping_notice.domain.model.AdvancedShippingNoticeStatus;
import com.waregang.receiving_service.test_utils.mothers.AdvancedShippingNoticeJpaMother;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AdvancedShippingNoticeJpaTest {

    private static final String VALID_WH = "WH-001";
    private static final String WRONG_WH = "WH-777";

    @Test
    @DisplayName("Should transition to ARRIVED status when marked as arrived at the correct warehouse")
    void shouldMarkAsArrivedWhenWarehouseMatches() {
        // Arrange
        AdvancedShippingNoticeJpa asn = AdvancedShippingNoticeJpaMother.expectedAsn(VALID_WH);

        // Act
        asn.markAsArrived(VALID_WH);

        // Assert
        assertThat(asn.getStatus()).isEqualTo(AdvancedShippingNoticeStatus.ARRIVED);
    }

    @Test
    @DisplayName("Should throw exception when trying to mark as arrived at a wrong warehouse")
    void shouldFailArrivedWhenWarehouseMismatch() {
        // Arrange
        AdvancedShippingNoticeJpa asn = AdvancedShippingNoticeJpaMother.expectedAsn(VALID_WH);

        // Act & Assert
        assertThatThrownBy(() -> asn.markAsArrived(WRONG_WH))
                .isInstanceOf(AppException.class);
    }

    @Test
    @DisplayName("Should successfully close a delivery in ARRIVED status")
    void shouldCloseDeliveryInArrivedStatus() {
        // Arrange
        AdvancedShippingNoticeJpa asn = AdvancedShippingNoticeJpaMother.arrivedAsn(VALID_WH);

        // Act
        asn.close();

        // Assert
        assertThat(asn.getStatus()).isEqualTo(AdvancedShippingNoticeStatus.CLOSED);
    }

    @Test
    @DisplayName("Should fail to close a delivery in EXPECTED status directly")
    void shouldFailClosingExpectedDelivery() {
        // Arrange
        AdvancedShippingNoticeJpa asn = AdvancedShippingNoticeJpaMother.expectedAsn(VALID_WH);

        // Act & Assert
        assertThatThrownBy(asn::close)
                .isInstanceOf(AppException.class);
    }
}