package com.waregang.receiving_service.receiving_process.api.dto;

public record ReceivedContentDto(
        String parentLpn,
        String sku,
        Long quantity
) {}
