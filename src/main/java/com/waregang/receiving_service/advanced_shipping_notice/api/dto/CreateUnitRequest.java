package com.waregang.receiving_service.advanced_shipping_notice.api.dto;

import jakarta.validation.constraints.NotBlank;
import org.jspecify.annotations.Nullable;

public record CreateUnitRequest(
        @NotBlank
        String type,

        @NotBlank
        String lpn,

        @Nullable
        String parentLpn
) {}
