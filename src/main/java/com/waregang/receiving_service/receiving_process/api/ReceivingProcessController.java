package com.waregang.receiving_service.receiving_process.api;

import com.waregang.receiving_service.receiving_process.api.dto.*;
import com.waregang.receiving_service.receiving_process.application.ReceivingProcessService;
import com.waregang.receiving_service.receiving_process.domain.model.WorkerReceivingSession;
import com.waregang.receiving_service.security.UserPrincipal;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.UUID;

@RequiredArgsConstructor


@RestController
@RequestMapping(ReceivingProcessController.BASE_URL)
@Validated
public class ReceivingProcessController {
    public static final String BASE_URL = "/api/receiving-service/receiving-sessions";
    private final ReceivingProcessService service;

    @PreAuthorize("hasAuthority('WORKER')")
    @PostMapping("/{receiptId}/joins")
    public ResponseEntity<JoinReceivingResponse> joinReceiving(
            @PathVariable("receiptId")
            UUID receiptId,

            @AuthenticationPrincipal
            UserPrincipal worker
    ) {
        JoinReceivingResponse response = service.joinReceiving(worker, receiptId);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PreAuthorize("hasAuthority('WORKER')")
    @PostMapping("/scans/{lpn}")
    public ResponseEntity<ScanHandlingUnitResponse> scanHandlingUnit(
            @NotBlank
            @Size(max = 50, message = "too long LPN")
            @PathVariable("lpn")
            String lpn,

            @AuthenticationPrincipal
            UserPrincipal worker
    ) {
        ScanHandlingUnitRequest request = new ScanHandlingUnitRequest(lpn);
        ScanHandlingUnitResponse response = service.scanHandlingUnit(request, worker);

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PreAuthorize("hasAuthority('WORKER')")
    @PostMapping("/scans/contents/{sku}")
    public ResponseEntity<ScanContentResponse> receiveContent(
            @NotBlank
            @Size(max = 50, message = "too long SKU")
            @PathVariable("sku")
            String sku,

            @Valid @RequestBody
            ScanContentQuantityRequest request,

            @AuthenticationPrincipal
            UserPrincipal worker
    ) {
        ScanContentRequest fullRequest = new ScanContentRequest(sku, request.quantity());
        ScanContentResponse response = service.scanContent(fullRequest, worker);

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PreAuthorize("hasAuthority('WORKER')")
    @PostMapping("/navigation/back")
    public ResponseEntity<NavigationBackResponse> getBackToPreviousUnit(
            @AuthenticationPrincipal UserPrincipal worker
    ) {
        NavigationBackResponse response = service.getBackToPreviousUnit(worker);

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PreAuthorize("hasAuthority('WORKER')")
    @PostMapping("/completion")
    public ResponseEntity<Void> completeWorkerSession(
            @AuthenticationPrincipal UserPrincipal worker
    ) {
        service.completeWorkerSession(worker);

        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @PreAuthorize("hasAuthority('WORKER')")
    @GetMapping("/validations/lpn/{lpn}")
    public ResponseEntity<LpnInAsnResponse> checkIfLpnInAsn(
            @NotBlank
            @Size(max = 50, message = "too long LPN")
            @PathVariable("lpn")
            String lpn,

            @AuthenticationPrincipal
            UserPrincipal worker
    ) {
        LpnInAsnResponse response = service.checkIfLpnInAsn(worker, lpn);
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasAuthority('WORKER')")
    @GetMapping("/validations/sku/{sku}")
    public ResponseEntity<SkuInAsnResponse> checkIfSkuInAsn(
            @NotBlank
            @Size(max = 50, message = "too long SKU")
            @PathVariable("sku")
            String sku,

            @AuthenticationPrincipal
            UserPrincipal worker
    ) {
        SkuInAsnResponse response = service.checkIfSkuInAsn(worker, sku);
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasAuthority('WORKER')")
    @GetMapping("/worker-sessions")
    public ResponseEntity<WorkerReceivingSessionResponse> getCurrentSessionInfo(
            @AuthenticationPrincipal
            UserPrincipal worker
    ){
        WorkerReceivingSessionResponse response = service.getCurrentSession(worker.id());
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasAuthority('WORKER')")
    @GetMapping("/daily-statistics")
    public ResponseEntity<WorkerStatisticsResponse> getWorkerStatistics(
            @AuthenticationPrincipal UserPrincipal worker
    ){
        LocalDate queryDate = LocalDate.now();
        WorkerStatisticsResponse response = service.getWorkerStatistics(worker.id(), queryDate);
        return ResponseEntity.ok(response);
    }
}