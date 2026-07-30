package com.waregang.receiving_service.receiving_process.api.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ScanContentRequest(
        @NotBlank
        String sku,

        @NotNull
        @Min(1)
        Long quantity
) {}
