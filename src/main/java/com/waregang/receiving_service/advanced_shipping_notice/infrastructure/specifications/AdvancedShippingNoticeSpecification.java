package com.waregang.receiving_service.advanced_shipping_notice.infrastructure.specifications;

import com.waregang.receiving_service.advanced_shipping_notice.domain.model.AdvancedShippingNoticeJpa;
import com.waregang.receiving_service.advanced_shipping_notice.domain.model.AdvancedShippingNoticeStatus;
import org.springframework.data.jpa.domain.Specification;
import java.time.LocalDateTime;

public class AdvancedShippingNoticeSpecification {

    public static Specification<AdvancedShippingNoticeJpa> expectedArrivalAfter(LocalDateTime fromDate) {
        return (root, query, criteriaBuilder) -> {
            if (fromDate == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.greaterThanOrEqualTo(root.get("arrivalTimeline").get("expected"), fromDate);
        };
    }

    public static Specification<AdvancedShippingNoticeJpa> expectedArrivalBefore(LocalDateTime toDate) {
        return (root, query, criteriaBuilder) -> {
            if (toDate == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.lessThanOrEqualTo(root.get("arrivalTimeline").get("expected"), toDate);
        };
    }

    public static Specification<AdvancedShippingNoticeJpa> hasStatus(AdvancedShippingNoticeStatus status) {
        return (root, query, criteriaBuilder) -> {
            if (status == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.equal(root.get("status"), status);
        };
    }

    public static Specification<AdvancedShippingNoticeJpa> hasVendorName(String vendorName) {
        return (root, query, criteriaBuilder) -> {
            if (vendorName == null || vendorName.isBlank()) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.like(root.get("vendorName"), "%" + vendorName + "%");
        };
    }
}
