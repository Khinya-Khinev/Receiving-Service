package com.waregang.receiving_service.receiving_process.api.dto;

import org.jspecify.annotations.Nullable;

public record ReceivedUnitDto(
        String type,
        String lpn,
        @Nullable String parentLpn
) {}
