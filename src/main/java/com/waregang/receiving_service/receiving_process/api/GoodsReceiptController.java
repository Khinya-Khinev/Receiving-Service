package com.waregang.receiving_service.receiving_process.api;

import com.waregang.receiving_service.receiving_process.api.dto.GoodsReceiptDetailsResponse;
import com.waregang.receiving_service.receiving_process.api.dto.StartReceivingRequest;
import com.waregang.receiving_service.receiving_process.api.dto.StartReceivingResponse;
import com.waregang.receiving_service.receiving_process.application.GoodsReceiptService;
import com.waregang.receiving_service.receiving_process.domain.dto.GoodsReceiptDto;
import com.waregang.receiving_service.receiving_process.domain.model.GoodsReceiptStatus;
import com.waregang.receiving_service.security.UserPrincipal;
import com.waregang.receiving_service.common.exception_handling.AppException;
import com.waregang.receiving_service.common.exception_handling.error_code.ValidationErrorCode;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Set;
import java.util.UUID;

@RequiredArgsConstructor

@RestController
@RequestMapping("/api/receiving-service/goods-receipts")
public class GoodsReceiptController {

    private final GoodsReceiptService service;

    private static final Set<String> ALLOWED_SORT_PROPERTIES =
            Set.of("status", "warehouseId", "gateNumber", "receivingMode", "arrivalTimeline.expecred", "arrivalTimeline.actual");

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


    @PreAuthorize("hasAuthority('MANAGER') or hasAuthority('WORKER')")
    @GetMapping
    public ResponseEntity<Page<GoodsReceiptDto>> getGoodsReceipts(
            @ParameterObject @PageableDefault(
                    size = 10,
                    page = 0,
                    direction = Sort.Direction.DESC
            ) Pageable pageable,

            @RequestParam(name = "status", required = false)
            GoodsReceiptStatus receiptStatus,

            @AuthenticationPrincipal
            UserPrincipal user
    ) {
        validateSort(pageable.getSort());

        return ResponseEntity.ok(service.findGoodsReceipts(user, receiptStatus, pageable));
    }

    private void validateSort(Sort sort) {
        sort.forEach(order -> {
            if (!ALLOWED_SORT_PROPERTIES.contains(order.getProperty())) {
                throw AppException.of(ValidationErrorCode.INVALID_SORT_PROPERTY)
                        .with("invalid_property", order.getProperty());
            }
        });
    }

    @GetMapping("/{receipt-id}/received-units")
    public ResponseEntity<GoodsReceiptDetailsResponse> getReceiptDetails(
            @PathVariable(value = "receipt-id") UUID receiptId
    ) {
        return ResponseEntity.ok(service.getReceiptDetails(receiptId));
    }
}