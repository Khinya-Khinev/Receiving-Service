package com.waregang.receiving_service.advanced_shipping_notice.application;

import com.waregang.receiving_service.advanced_shipping_notice.api.dto.CreateAsnRequest;
import com.waregang.receiving_service.advanced_shipping_notice.api.dto.CreateContentRequest;
import com.waregang.receiving_service.advanced_shipping_notice.api.dto.CreateUnitRequest;
import com.waregang.receiving_service.advanced_shipping_notice.domain.model.HandlingUnitJpa;
import com.waregang.receiving_service.advanced_shipping_notice.domain.model.AdvancedShippingNoticeJpa;
import com.waregang.receiving_service.advanced_shipping_notice.api.dto.AsnResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Component
public class AdvancedShippingNoticeMapper {

    public AdvancedShippingNoticeJpa toEntity(CreateAsnRequest request) {
        AdvancedShippingNoticeJpa delivery = AdvancedShippingNoticeJpa.create(
                request.externalId(),
                request.asnNumber(),
                request.warehouseId(),
                request.vendorName(),
                request.expectedArrivalDate()
        );

        Map<String, List<CreateUnitRequest>> unitsByParentLpn = request.unitRequests().stream()
                .filter(r -> r.parentLpn() != null)
                .collect(Collectors.groupingBy(CreateUnitRequest::parentLpn));

        Map<String, List<CreateContentRequest>> contentsByParentLpn = request.contents().stream()
                .collect(Collectors.groupingBy(CreateContentRequest::parentLpn));

        // Create a map to look up parent entities by LPN
        Map<String, HandlingUnitJpa> createdUnits = new java.util.HashMap<>();

        // Create units in a way that respects hierarchy
        List<CreateUnitRequest> sortedUnits = sortUnitsByHierarchy(request.unitRequests());

        sortedUnits.forEach(unitReq -> {
            HandlingUnitJpa parent = unitReq.parentLpn() != null ? createdUnits.get(unitReq.parentLpn()) : null;
            String path = (parent != null ? parent.getPath() + "/" : "") + unitReq.lpn();

            HandlingUnitJpa unit = HandlingUnitJpa.create(
                    unitReq.lpn(),
                    path,
                    parent,
                    delivery
            );

            // Add content
            contentsByParentLpn.getOrDefault(unitReq.lpn(), Collections.emptyList())
                    .forEach(contentReq -> unit.fillWithContent(contentReq.sku(), contentReq.quantity()));

            delivery.addHandlingUnit(unit);
            createdUnits.put(unitReq.lpn(), unit);
        });

        return delivery;
    }

    public AsnResponse toAsnResponse(AdvancedShippingNoticeJpa asn) {
        return new AsnResponse(
                asn.getId(),
                asn.getAsnNumber(),
                asn.getVendorName(),
                asn.getStatus(),
                asn.getArrivalTimeline().getExpected(),
                asn.getArrivalTimeline().getActual()
        );
    }

    private List<CreateUnitRequest> sortUnitsByHierarchy(List<CreateUnitRequest> units) {
        // Simple topological sort or level-based ordering would work
        // For now, ensure parent comes before child
        List<CreateUnitRequest> result = new ArrayList<>();
        List<CreateUnitRequest> remaining = new ArrayList<>(units);

        while (!remaining.isEmpty()) {
            List<CreateUnitRequest> ready = remaining.stream()
                    .filter(u -> u.parentLpn() == null || result.stream()
                                    .anyMatch(r -> r.lpn().equals(u.parentLpn())))
                    .toList();
            result.addAll(ready);
            remaining.removeAll(ready);
        }
        return result;
    }
}