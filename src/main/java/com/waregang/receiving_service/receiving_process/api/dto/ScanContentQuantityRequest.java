package com.waregang.receiving_service.receiving_process.api.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record ScanContentQuantityRequest(
        @NotNull
        @Min(1)
        Long quantity
) {}
