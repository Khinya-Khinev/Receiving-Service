package com.waregang.receiving_service.advanced_shipping_notice.application;

import com.waregang.receiving_service.advanced_shipping_notice.api.dto.CreateAsnRequest;
import com.waregang.receiving_service.advanced_shipping_notice.api.dto.CreateContentRequest;
import com.waregang.receiving_service.advanced_shipping_notice.api.dto.CreateUnitRequest;
import com.waregang.receiving_service.advanced_shipping_notice.domain.model.AdvancedShippingNoticeJpa;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class AdvancedShippingNoticeMapperTest {

    private final AdvancedShippingNoticeMapper mapper = new AdvancedShippingNoticeMapper();

    @Test
    void toEntity() {
        // Given
        CreateAsnRequest request = new CreateAsnRequest(
                "externalId",
                "asnNumber",
                "warehouseId",
                "vendorName",
                LocalDateTime.now(),
                List.of(
                        new CreateUnitRequest("pallet", "lpn1", null),
                        new CreateUnitRequest("box", "lpn2", "lpn1")
                ),
                List.of(
                        new CreateContentRequest("lpn2", "sku1", 10L)
                )
        );

        // When
        AdvancedShippingNoticeJpa result = mapper.toEntity(request);

        // Then
        assertThat(result.getExternalId()).isEqualTo("externalId");
        assertThat(result.getAsnNumber()).isEqualTo("asnNumber");
        assertThat(result.getWarehouseId()).isEqualTo("warehouseId");
        assertThat(result.getVendorName()).isEqualTo("vendorName");
        assertThat(result.getHandlingUnits()).hasSize(2);
    }
}
