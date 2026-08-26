package com.waregang.receiving_service.receiving_process.api.dto;

import com.waregang.receiving_service.receiving_process.domain.model.ReceivingMode;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record StartReceivingRequest(
        @NotBlank
        String asnNumber,

        @NotBlank
        String gateNumber,

        @NotNull
        ReceivingMode receivingMode
) {
}
