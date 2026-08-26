package com.waregang.receiving_service.receiving_process.infrastructure.specifications;

import com.waregang.receiving_service.receiving_process.domain.model.GoodsReceiptStatus;
import com.waregang.receiving_service.receiving_process.infrastructure.jpa_entities.GoodsReceiptJpa;
import org.springframework.data.jpa.domain.Specification;

public class GoodsReceiptSpecification {

    public static Specification<GoodsReceiptJpa> hasStatus(GoodsReceiptStatus status) {
        return (root, query, criteriaBuilder) -> {
            if (status == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.equal(root.get("status"), status);
        };
    }

    public static Specification<GoodsReceiptJpa> hasWarehouseId(String warehouseId) {
        return (root, query, criteriaBuilder) -> {
            if (warehouseId == null || warehouseId.isBlank()) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.equal(root.get("warehouseId"), warehouseId);
        };
    }
}
