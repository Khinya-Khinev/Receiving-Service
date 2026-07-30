package com.waregang.receiving_service.receiving_process.infrastructure.mappers;

import com.waregang.receiving_service.receiving_process.domain.model.ReceivedContent;
import com.waregang.receiving_service.receiving_process.domain.model.ReceivedUnit;
import com.waregang.receiving_service.receiving_process.infrastructure.jpa_entities.ReceivedUnitJpa;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ReceivedUnitMapper {

    private final EntityManager entityManager;

    public ReceivedUnit toDomain(ReceivedUnitJpa jpa) {
        ReceivedUnit unit = ReceivedUnit.reconstitute(
                jpa.getId(),
                jpa.getLpn(),
                jpa.getParentUnit() != null ? jpa.getParentUnit().getId() : null,
                jpa.getWorkerSessionId(),
                jpa.getReceiptId()
        );

        jpa.getChildUnits().stream()
                .map(this::toDomain)
                .forEach(child -> unit.getChildUnits().add(child));

        jpa.getContents().stream()
                .map(content -> ReceivedContent.reconstitute(
                        content.getId(),
                        content.getSku(),
                        content.getQuantity(),
                        content.getContainerUnit().getId()
                ))
                .forEach(content -> unit.getContents().add(content));

        return unit;
    }

    public ReceivedUnitJpa toJpa(ReceivedUnit domain) {
        ReceivedUnitJpa parentJpa = null;
        if (domain.getParentUnitId() != null) {
            parentJpa = entityManager.getReference(ReceivedUnitJpa.class, domain.getParentUnitId());
        }

        return new ReceivedUnitJpa(
                domain.getId(),
                domain.getLpn(),
                parentJpa,
                domain.getWorkerSessionId(),
                domain.getReceiptId()
        );
    }
}