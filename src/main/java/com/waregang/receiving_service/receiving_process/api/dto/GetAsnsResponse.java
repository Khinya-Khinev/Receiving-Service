package com.waregang.receiving_service.receiving_process.api.dto;

import java.util.List;

public record GetAsnsResponse(
        List<AsnResponse> asns
) {
}