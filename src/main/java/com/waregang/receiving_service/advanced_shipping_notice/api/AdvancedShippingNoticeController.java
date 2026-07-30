package com.waregang.receiving_service.advanced_shipping_notice.api;

import com.waregang.receiving_service.advanced_shipping_notice.api.dto.AsnDetailsResponse;
import com.waregang.receiving_service.advanced_shipping_notice.api.dto.CreateAsnRequest;
import com.waregang.receiving_service.advanced_shipping_notice.api.dto.CreateAsnResponse;
import com.waregang.receiving_service.advanced_shipping_notice.application.AdvancedShippingNoticeMapper;
import com.waregang.receiving_service.advanced_shipping_notice.application.AdvancedShippingNoticeService;
import com.waregang.receiving_service.advanced_shipping_notice.domain.model.AdvancedShippingNoticeStatus;
import com.waregang.receiving_service.receiving_process.api.dto.AsnFilters;
import com.waregang.receiving_service.receiving_process.api.dto.AsnResponse;
import com.waregang.receiving_service.receiving_process.api.dto.GetAsnsResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor

@RestController
@RequestMapping("/api/asns")
public class AdvancedShippingNoticeController {
    private final AdvancedShippingNoticeService service;
    private final AdvancedShippingNoticeMapper mapper;

    @PostMapping
    public ResponseEntity<CreateAsnResponse> createAsn(
            @Valid @RequestBody CreateAsnRequest request
    ) {
        CreateAsnResponse response = service.createAsn(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/search")
    public ResponseEntity<Page<AsnResponse>> getAsnsWithFilters(
            @PageableDefault(
                    size = 10,
                    page = 0,
                    direction = Sort.Direction.DESC,
                    sort = "arrivalTimeline.expected"
            ) Pageable pageable,
            @RequestParam(name = "from_date", required = false) LocalDateTime fromDate,
            @RequestParam(name = "to_date", required = false) LocalDateTime toDate,
            @RequestParam(name = "status", required = false) AdvancedShippingNoticeStatus status,
            @RequestParam(name = "vendor", required = false) String vendorName
    ) {
        AsnFilters filters = new AsnFilters(fromDate, toDate, status, vendorName);

        Page<AsnResponse> page = service.findAsnsWithFilters(pageable, filters);

        return ResponseEntity.ok(page);
    }

    @GetMapping("/{asn_id}")
    public ResponseEntity<AsnResponse> getAsn(
            @PathVariable("asn_id") UUID asnId
    ) {
        return ResponseEntity.ok(mapper.toAsnResponse(service.findById(asnId)));
    }

    @GetMapping("/{asn_id}/handling-units")
    public ResponseEntity<AsnDetailsResponse> getAsnDetails(
            @PathVariable("asn_id") UUID asnId
    ) {
        return ResponseEntity.ok(service.getAsnDetails(asnId));
    }
}