package com.waregang.receiving_service.receiving_process.api.dto;

import com.waregang.receiving_service.receiving_process.api.dto.ReceivedContentDto;
import com.waregang.receiving_service.receiving_process.api.dto.ReceivedUnitDto;

import java.util.List;

public record GoodsReceiptDetailsResponse(
        List<ReceivedUnitDto> units,
        List<ReceivedContentDto> contents
) {}
