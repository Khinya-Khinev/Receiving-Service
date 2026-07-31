package com.waregang.receiving_service.advanced_shipping_notice.api.dto;

import java.util.List;

public record GetAsnsResponse(
        List<AsnResponse> asns
) {
}