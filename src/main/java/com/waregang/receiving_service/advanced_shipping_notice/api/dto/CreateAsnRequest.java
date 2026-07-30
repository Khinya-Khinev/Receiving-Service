package com.waregang.receiving_service.advanced_shipping_notice.api.dto;

import com.waregang.receiving_service.common.validation.ValidAsnRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.util.List;

@ValidAsnRequest
public record CreateAsnRequest(
        @NotBlank
        String externalId,

        @NotBlank
        String asnNumber,

        @NotBlank
        String warehouseId,

        @NotBlank
        String vendorName,

        @NotNull
        @Future
        LocalDateTime expectedArrivalDate,

        @NotEmpty
        List<@Valid CreateUnitRequest> unitRequests,

        @NotEmpty
        List<@Valid CreateContentRequest> contents
) {}