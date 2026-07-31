package com.waregang.receiving_service.advanced_shipping_notice.application;

import com.waregang.receiving_service.common.exception_handling.AppException;
import com.waregang.receiving_service.common.exception_handling.error_code.AsnErrorCode;
import com.waregang.receiving_service.advanced_shipping_notice.api.dto.AsnDetailsResponse;
import com.waregang.receiving_service.advanced_shipping_notice.api.dto.CreateAsnRequest;
import com.waregang.receiving_service.advanced_shipping_notice.api.dto.CreateAsnResponse;
import com.waregang.receiving_service.advanced_shipping_notice.api.dto.CreateContentRequest;
import com.waregang.receiving_service.advanced_shipping_notice.api.dto.CreateUnitRequest;
import com.waregang.receiving_service.advanced_shipping_notice.domain.model.AdvancedShippingNoticeJpa;
import com.waregang.receiving_service.advanced_shipping_notice.infrastructure.jpa_repositories.AdvancedShippingNoticeRepositoryJpa;
import com.waregang.receiving_service.advanced_shipping_notice.infrastructure.jpa_repositories.ContentRepositoryJpa;
import com.waregang.receiving_service.advanced_shipping_notice.infrastructure.jpa_repositories.HandlingUnitRepositoryJpa;
import com.waregang.receiving_service.advanced_shipping_notice.api.dto.AsnFilters;
import com.waregang.receiving_service.advanced_shipping_notice.api.dto.AsnResponse;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@Service
public class AdvancedShippingNoticeService {

    private final AdvancedShippingNoticeRepositoryJpa asnRepository;
    private final HandlingUnitRepositoryJpa handlingUnitRepositoryJpa;
    private final ContentRepositoryJpa contentRepositoryJpa;

    private final AdvancedShippingNoticeMapper mapper;

    @Transactional
    public CreateAsnResponse createAsn(CreateAsnRequest request) {
        AdvancedShippingNoticeJpa asn = asnRepository.save(mapper.toEntity(request));
        return new CreateAsnResponse(asn.getId());
    }

    @Transactional(readOnly = true)
    public AsnDetailsResponse getAsnDetails(UUID asnId) {
        AdvancedShippingNoticeJpa asn = asnRepository.findByIdWithHandlingUnits(asnId)
                .orElseThrow(() -> AppException.of(AsnErrorCode.ASN_NOT_FOUND)
                        .with("asn_id", asnId));

        List<CreateUnitRequest> unitRequests = asn.getHandlingUnits().stream()
                .map(unit -> new CreateUnitRequest(
                        unit.getType().name(),
                        unit.getLpn(),
                        unit.getParentUnit() != null ? unit.getParentUnit().getLpn() : null
                ))
                .toList();

        List<CreateContentRequest> contentRequests = asn.getHandlingUnits().stream()
                .flatMap(unit -> unit.getContents().stream())
                .map(content -> new CreateContentRequest(
                        content.getContainerUnit().getLpn(),
                        content.getSku(),
                        content.getQuantity()
                ))
                .toList();

        return new AsnDetailsResponse(unitRequests, contentRequests);
    }

    @Transactional
    public AdvancedShippingNoticeJpa findByAsn(String asnNumber) {
        return asnRepository.findByAsnNumber(asnNumber)
                .orElseThrow(() -> AppException.of(AsnErrorCode.ASN_NOT_FOUND)
                        .with("asn_number", asnNumber));
    }

    @Transactional
    public AdvancedShippingNoticeJpa findById(UUID id) {
        return asnRepository.findById(id)
                .orElseThrow(() -> AppException.of(AsnErrorCode.ASN_NOT_FOUND)
                        .with("inbound_delivery_id", id));
    }

    @Transactional(readOnly = true)
    public void validateScannedHuAgainstAsn(String scannedLpn, UUID asnId) {
        if (!handlingUnitRepositoryJpa.existsByLpnAndAsn_Id(scannedLpn, asnId)) {
            throw AppException.of(AsnErrorCode.LPN_NOT_IN_ASN)
                    .with("scanned_lpn", scannedLpn)
                    .with("asn_id", asnId);
        }
    }

    @Transactional(readOnly = true)
    public void validateScannedContentAgainstAsn(String scannedSku, UUID asnId) {
        if (!contentRepositoryJpa.existsBySkuAndAsn_Id(scannedSku, asnId)) {
            throw AppException.of(AsnErrorCode.SKU_NOT_IN_ASN)
                    .with("scanned_sku", scannedSku)
                    .with("asn_id", asnId);
        }
    }

    @Transactional
    public void closeAsn(UUID asnId) {
         AdvancedShippingNoticeJpa asn = asnRepository.findById(asnId)
                 .orElseThrow(() -> AppException.of(AsnErrorCode.ASN_NOT_FOUND)
                         .with("asn_id", asnId));

         asn.close();

         asnRepository.save(asn);
    }
    @Transactional(propagation = Propagation.MANDATORY)
    public void markAsArrived(AdvancedShippingNoticeJpa asn, String managerId) {
        asn.markAsArrived(managerId);
    }

    @Transactional(readOnly = true)
    public Page<AsnResponse> findAsnsWithFilters(
            Pageable pageable,
            AsnFilters filters
    ) {
        Specification<AdvancedShippingNoticeJpa> spec = buildSpecification(filters);

        return asnRepository.findAll(spec, pageable)
                .map(mapper::toAsnResponse);
    }

    private Specification<AdvancedShippingNoticeJpa> buildSpecification(AsnFilters filters) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (filters.fromDate() != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("arrivalDate").get("expected"), filters.fromDate()));
            }
            if (filters.toDate() != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("arrivalDate").get("expected"), filters.toDate()));
            }
            if (filters.status() != null) {
                predicates.add(criteriaBuilder.equal(root.get("status"), filters.status()));
            }
            if (filters.vendorName() != null && !filters.vendorName().isEmpty()) {
                predicates.add(criteriaBuilder.like(root.get("vendorName"), "%" + filters.vendorName() + "%"));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}