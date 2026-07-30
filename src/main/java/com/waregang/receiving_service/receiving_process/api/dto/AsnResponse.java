package com.waregang.receiving_service.receiving_process.api.dto;

import com.waregang.receiving_service.advanced_shipping_notice.domain.model.AdvancedShippingNoticeStatus;

import java.time.LocalDateTime;
import java.util.UUID;

public record AsnResponse(
        UUID id,
        String asnNumber,
        String vendorName,
        AdvancedShippingNoticeStatus status,
        LocalDateTime expectedArrivalDate,
        LocalDateTime actualArrivalDate
) {
}