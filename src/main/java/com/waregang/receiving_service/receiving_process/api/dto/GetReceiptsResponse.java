package com.waregang.receiving_service.receiving_process.api.dto;

import com.waregang.receiving_service.receiving_process.domain.dto.GoodsReceiptDto;

import java.util.List;

public record GetReceiptsResponse(
        List<GoodsReceiptDto> receipts
) {
}
