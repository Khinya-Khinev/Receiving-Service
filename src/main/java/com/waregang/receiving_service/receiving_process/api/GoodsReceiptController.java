package com.waregang.receiving_service.receiving_process.api;

import com.waregang.receiving_service.receiving_process.api.dto.GetOpenedReceiptsResponse;
import com.waregang.receiving_service.receiving_process.api.dto.GoodsReceiptDetailsResponse;
import com.waregang.receiving_service.receiving_process.api.dto.StartReceivingRequest;
import com.waregang.receiving_service.receiving_process.api.dto.StartReceivingResponse;
import com.waregang.receiving_service.receiving_process.application.GoodsReceiptService;
import com.waregang.receiving_service.receiving_process.domain.model.GoodsReceiptStatus;
import com.waregang.receiving_service.security.UserPrincipal;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RequiredArgsConstructor

@RestController
@RequestMapping("/api/goods-receipts")
public class GoodsReceiptController {
    private final GoodsReceiptService service;

    @PostMapping
    @PreAuthorize("hasAuthority('MANAGER')")
    public ResponseEntity<StartReceivingResponse> startReceiving(
            @Valid @RequestBody StartReceivingRequest request,
            @AuthenticationPrincipal UserPrincipal manager
    ) {
        StartReceivingResponse response = service.startReceiving(request, manager);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/{receipt-id}/closure")
    @PreAuthorize("hasAuthority('MANAGER')")
    public ResponseEntity<Void> closeReceiving(
            @PathVariable(value = "receipt-id") UUID receiptId,
            @AuthenticationPrincipal UserPrincipal manager
    ) {
        service.closeReceiving(manager, receiptId);

        return ResponseEntity.ok().build();
    }


    @GetMapping
    public ResponseEntity<GetOpenedReceiptsResponse> getOpenedGoodsReceipts(
            @RequestParam GoodsReceiptStatus receiptStatus,
            @AuthenticationPrincipal UserPrincipal user
    ) {
        GetOpenedReceiptsResponse response = service.findAllByStatus(user, receiptStatus);

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/{receipt-id}/received-units")
    public ResponseEntity<GoodsReceiptDetailsResponse> getReceiptDetails(
            @PathVariable(value = "receipt-id") UUID receiptId
    ) {
        return ResponseEntity.ok(service.getReceiptDetails(receiptId));
    }
}