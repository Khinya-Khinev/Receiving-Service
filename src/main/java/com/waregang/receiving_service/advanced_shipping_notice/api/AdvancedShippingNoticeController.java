package com.waregang.receiving_service.advanced_shipping_notice.api;

import com.waregang.receiving_service.advanced_shipping_notice.api.dto.AsnDetailsResponse;
import com.waregang.receiving_service.advanced_shipping_notice.api.dto.CreateAsnRequest;
import com.waregang.receiving_service.advanced_shipping_notice.api.dto.CreateAsnResponse;
import com.waregang.receiving_service.advanced_shipping_notice.application.AdvancedShippingNoticeMapper;
import com.waregang.receiving_service.advanced_shipping_notice.application.AdvancedShippingNoticeService;
import com.waregang.receiving_service.advanced_shipping_notice.domain.model.AdvancedShippingNoticeStatus;
import com.waregang.receiving_service.advanced_shipping_notice.api.dto.AsnFilters;
import com.waregang.receiving_service.advanced_shipping_notice.api.dto.AsnResponse;
import com.waregang.receiving_service.common.exception_handling.AppException;
import com.waregang.receiving_service.common.exception_handling.error_code.ErrorCode;
import com.waregang.receiving_service.common.exception_handling.error_code.ValidationErrorCode;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

@RequiredArgsConstructor

@RestController
@RequestMapping("/api/receiving-service/asns")
@Validated
public class AdvancedShippingNoticeController {

    private final AdvancedShippingNoticeService service;
    private final AdvancedShippingNoticeMapper mapper;

    private static final Set<String> ALLOWED_SORT_PROPERTIES =
            Set.of("arrivalTimeline.expected", "arrivalTimeline.actual", "status", "vendorName", "receivingMode");
    // mb I will try jpa metamodel lib for avoiding field names mismatch later


    @PreAuthorize("hasAuthority('MANAGER')")
    @PostMapping
    public ResponseEntity<CreateAsnResponse> createAsn(
            @Valid @RequestBody CreateAsnRequest request
    ) {
        CreateAsnResponse response = service.createAsn(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PreAuthorize("hasAuthority('MANAGER')")
    @GetMapping("/search")
    public ResponseEntity<Page<AsnResponse>> getAsnsWithFilters(
            @ParameterObject @PageableDefault(
                    size = 10,
                    page = 0,
                    direction = Sort.Direction.DESC,
                    sort = "arrivalTimeline.expected")
            Pageable pageable,

            @Past(message = "fromDate expected to be in the past")
            @RequestParam(name = "from_date", required = false)
            LocalDateTime fromDate,

            @RequestParam(name = "to_date", required = false)
            LocalDateTime toDate,

            @RequestParam(name = "status", required = false)
            AdvancedShippingNoticeStatus status,

            @Size(max = 50, message = "vendor must not exceed 50 characters")
            @RequestParam(name = "vendor", required = false)
            String vendorName
    ) {
        validateSort(pageable.getSort());
        validateDateRange(fromDate, toDate);

        AsnFilters filters = new AsnFilters(fromDate, toDate, status, vendorName);

        Page<AsnResponse> page = service.findAsnsWithFilters(pageable, filters);

        return ResponseEntity.ok(page);
    }

    @PreAuthorize("hasAuthority('MANAGER')")
    @GetMapping("/{asn_id}")
    public ResponseEntity<AsnResponse> getAsn(
            @PathVariable("asn_id") UUID asnId
    ) {
        return ResponseEntity.ok(mapper.toAsnResponse(service.findById(asnId)));
    }

    @PreAuthorize("hasAuthority('MANAGER')")
    @GetMapping("/{asn_id}/handling-units")
    public ResponseEntity<AsnDetailsResponse> getAsnDetails(
            @PathVariable("asn_id") UUID asnId
    ) {
        return ResponseEntity.ok(service.getAsnDetails(asnId));
    }

    private void validateSort(Sort sort) {
        sort.forEach(order -> {
            if (!ALLOWED_SORT_PROPERTIES.contains(order.getProperty())) {
                throw AppException.of(ValidationErrorCode.INVALID_SORT_PROPERTY)
                        .with("invalid_property", order.getProperty());
            }
        });
    }

    private void validateDateRange(LocalDateTime fromDate, LocalDateTime toDate) {
        if (fromDate != null && toDate != null && fromDate.isAfter(toDate)) {
            throw AppException.of(ValidationErrorCode.INVALID_DATE_RANGE)
                    .with("invalid_date", "toDate must be after fromDate");
        }
    }
}