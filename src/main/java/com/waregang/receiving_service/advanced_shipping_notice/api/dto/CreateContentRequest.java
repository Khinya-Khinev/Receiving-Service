package com.waregang.receiving_service.advanced_shipping_notice.api.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateContentRequest(
        @NotBlank
        String parentLpn,

        @NotBlank
        String sku,

        @NotNull
        @Min(1)
        Long quantity
) {
}
