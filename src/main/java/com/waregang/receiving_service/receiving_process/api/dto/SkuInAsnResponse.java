package com.waregang.receiving_service.receiving_process.api.dto;

public record SkuInAsnResponse(
        String sku,
        boolean isInAsn
) {}
