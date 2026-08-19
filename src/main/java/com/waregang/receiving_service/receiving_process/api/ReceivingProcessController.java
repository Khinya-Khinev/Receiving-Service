package com.waregang.receiving_service.receiving_process.api;

import com.waregang.receiving_service.receiving_process.api.dto.*;
import com.waregang.receiving_service.receiving_process.application.ReceivingProcessService;
import com.waregang.receiving_service.receiving_process.domain.model.WorkerReceivingSession;
import com.waregang.receiving_service.security.UserPrincipal;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RequiredArgsConstructor

@RestController
@RequestMapping("/api/receiving-sessions")
public class ReceivingProcessController {
    private final ReceivingProcessService service;

    @PostMapping("/{receiptId}/joins")
    public ResponseEntity<JoinReceivingResponse> joinReceiving(
            @PathVariable("receiptId") UUID receiptId,
            @AuthenticationPrincipal UserPrincipal worker
    ) {
        JoinReceivingResponse response = service.joinReceiving(worker, receiptId);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/scans/{lpn}")
    public ResponseEntity<ScanHandlingUnitResponse> scanHandlingUnit(
            @PathVariable("lpn") String lpn,
            @AuthenticationPrincipal UserPrincipal worker
    ) {
        ScanHandlingUnitRequest request = new ScanHandlingUnitRequest(lpn);
        ScanHandlingUnitResponse response = service.scanHandlingUnit(request, worker);

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PostMapping("/scans/contents/{sku}")
    public ResponseEntity<ScanContentResponse> receiveContent(
            @PathVariable("sku") String sku,
            @Valid @RequestBody ScanContentQuantityRequest request,
            @AuthenticationPrincipal UserPrincipal worker
    ) {
        ScanContentRequest fullRequest = new ScanContentRequest(sku, request.quantity());
        ScanContentResponse response = service.scanContent(fullRequest, worker);

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PostMapping("/navigation/back")
    public ResponseEntity<NavigationBackResponse> getBackToPreviousUnit(
            @AuthenticationPrincipal UserPrincipal worker
    ) {
        NavigationBackResponse response = service.getBackToPreviousUnit(worker);

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PostMapping("/completion")
    public ResponseEntity<Void> completeWorkerSession(
            @AuthenticationPrincipal UserPrincipal worker
    ) {
        service.completeWorkerSession(worker);

        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @GetMapping("/validations/lpn/{lpn}")
    public ResponseEntity<LpnInAsnResponse> checkIfLpnInAsn(
            @PathVariable("lpn") String lpn,
            @AuthenticationPrincipal UserPrincipal worker
    ) {
        LpnInAsnResponse response = service.checkIfLpnInAsn(worker, lpn);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/validations/sku/{sku}")
    public ResponseEntity<SkuInAsnResponse> checkIfSkuInAsn(
            @PathVariable("sku") String sku,
            @AuthenticationPrincipal UserPrincipal worker
    ) {
        SkuInAsnResponse response = service.checkIfSkuInAsn(worker, sku);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/worker-sessions/{user-id}")
    public ResponseEntity<WorkerReceivingSessionResponse> getCurrentSessionInfo(
            @PathVariable("user-id") String userId
    ){
        WorkerReceivingSessionResponse response = service.getCurrentSession(UUID.fromString(userId));
        return ResponseEntity.ok(response);
    }

    @GetMapping("/statistics/{user-id}")
    public ResponseEntity<WorkerStatisticsResponse> getWorkerStatistics(
            @PathVariable("user-id") String userId
    ){
        WorkerStatisticsResponse response = service.getWorkerStatistics(UUID.fromString(userId));
        return ResponseEntity.ok(response);
    }
}