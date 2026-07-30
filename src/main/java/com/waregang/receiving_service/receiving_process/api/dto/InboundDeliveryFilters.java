package com.waregang.receiving_service.receiving_process.api.dto;

import com.waregang.receiving_service.advanced_shipping_notice.domain.model.AdvancedShippingNoticeStatus;

import java.time.LocalDateTime;

public record InboundDeliveryFilters(
        LocalDateTime fromDate,
        LocalDateTime toDate,
        AdvancedShippingNoticeStatus status,
        String vendorName
) {
}
