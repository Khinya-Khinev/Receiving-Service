package com.waregang.receiving_service.advanced_shipping_notice.api.dto;

import java.util.List;

public record AsnDetailsResponse(
        List<CreateUnitRequest> units,
        List<CreateContentRequest> contents
) {}
