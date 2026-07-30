package com.waregang.receiving_service.receiving_process.api.dto;

import com.waregang.receiving_service.advanced_shipping_notice.api.dto.CreateContentRequest;
import com.waregang.receiving_service.advanced_shipping_notice.api.dto.CreateUnitRequest;

import java.util.List;

public record GoodsReceiptDetailsResponse(
        List<CreateUnitRequest> units,
        List<CreateContentRequest> contents
) {}
