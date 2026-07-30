package com.waregang.receiving_service.receiving_process.api.dto;

public record LpnInAsnResponse(
        String lpn,
        boolean isInAsn
) {}
