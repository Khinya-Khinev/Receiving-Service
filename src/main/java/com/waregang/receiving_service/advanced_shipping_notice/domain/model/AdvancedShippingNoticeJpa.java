package com.waregang.receiving_service.advanced_shipping_notice.domain.model;

import com.waregang.receiving_service.common.IdGenerator;
import com.waregang.receiving_service.common.exception_handling.AppException;
import com.waregang.receiving_service.common.exception_handling.error_code.AsnErrorCode;
import com.waregang.receiving_service.common.exception_handling.error_code.ReceivingErrorCode;
import com.waregang.receiving_service.receiving_process.domain.model.ReceivingMode;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.domain.AbstractAggregateRoot;
import org.springframework.data.domain.Persistable;

import java.time.LocalDateTime;
import java.util.*;

@Getter @Setter
@NoArgsConstructor(access = AccessLevel.PACKAGE)

@Entity
@Table(name = "asn")
public class AdvancedShippingNoticeJpa extends AbstractAggregateRoot<AdvancedShippingNoticeJpa> implements Persistable<UUID> {

    @Id
    @Column(name = "id", updatable = false, nullable = false, columnDefinition = "uuid")
    private UUID id;

    @Column(name = "external_id", updatable = false, nullable = false, unique = true)
    private String externalId;

    @Column(name = "asn_number", updatable = false, nullable = false, unique = true)
    private String asnNumber;

    @Column(name = "vendor_name", nullable = false)
    private String vendorName;

    @Column(name = "warehouse_id", updatable = false, nullable = false)
    private String warehouseId;

    @Enumerated(EnumType.STRING)
    @Column(name = "receiving_mode", nullable = false)
    private ReceivingMode receivingMode;

    @Column(name = "status", nullable = false)
    @Enumerated(value = EnumType.STRING)
    private AdvancedShippingNoticeStatus status;

    @Embedded
    private ArrivalTimeline arrivalTimeline;

    @OneToMany(
            mappedBy = "asn",
            cascade = CascadeType.ALL,
            orphanRemoval = true)
    private final Set<HandlingUnitJpa> handlingUnits = new HashSet<>();

    private AdvancedShippingNoticeJpa(
            String externalId,
            String asnNumber,
            String warehouseId,
            String vendorName,
            LocalDateTime expectedArrivalDate
    ) {
        this.id = IdGenerator.generate();
        this.externalId = externalId;
        this.status = AdvancedShippingNoticeStatus.EXPECTED;
        this.receivingMode = ReceivingMode.ASN_MATCHING;
        this.asnNumber = asnNumber;
        this.warehouseId = warehouseId;
        this.vendorName = vendorName;
        this.arrivalTimeline = ArrivalTimeline.of(expectedArrivalDate);
    }

    public static AdvancedShippingNoticeJpa create(String externalId, String asnNumber, String warehouseId, String vendorName, LocalDateTime expectedArrivalDate) {
        return new AdvancedShippingNoticeJpa(externalId, asnNumber, warehouseId, vendorName, expectedArrivalDate);
    }

    public static AdvancedShippingNoticeJpa reconstitute(
            UUID id,
            String externalId,
            String asnNumber,
            String warehouseId,
            ReceivingMode receivingMode,
            AdvancedShippingNoticeStatus status,
            Integer version,
            String vendorName,
            LocalDateTime expectedArrivalDate,
            LocalDateTime actualArrivalDate
    ) {
        AdvancedShippingNoticeJpa asn = new AdvancedShippingNoticeJpa();

        asn.id = id;
        asn.externalId = externalId;
        asn.asnNumber = asnNumber;
        asn.warehouseId = warehouseId;
        asn.receivingMode = receivingMode;
        asn.status = status;
        asn.version = version;
        asn.isNew = false;
        asn.vendorName = vendorName;
        asn.arrivalTimeline = ArrivalTimeline.of(expectedArrivalDate).withActual(actualArrivalDate);

        return asn;
    }

    public void addHandlingUnit(HandlingUnitJpa unit) {
        if (unit != null) {
            this.handlingUnits.add(unit);
        }
    }

    public void markAsArrived(String managerId) {
        ensureValidForReceiving(managerId);
        this.status = AdvancedShippingNoticeStatus.ARRIVED;
        this.arrivalTimeline = this.arrivalTimeline.withActual(LocalDateTime.now());
    }

    public void ensureValidForReceiving(String managerWarehouseId) {
        if (this.status != AdvancedShippingNoticeStatus.EXPECTED) {
            throw AppException.of(AsnErrorCode.INVALID_STATE)
                    .with("actual_status", status)
                    .with("expected_status", AdvancedShippingNoticeStatus.EXPECTED);
        }

        if (!this.getWarehouseId().equals(managerWarehouseId)) {
            throw AppException.of(ReceivingErrorCode.WAREHOUSE_MISMATCH)
                    .with("actual_wh", managerWarehouseId)
                    .with("expected_wh", this.getWarehouseId());
        }
    }

    public void close() {
        if (this.status != AdvancedShippingNoticeStatus.ARRIVED) {
            throw AppException.of(AsnErrorCode.ASN_NOT_FOUND)
                    .with("delivery_status", status);
        }

        this.status = AdvancedShippingNoticeStatus.CLOSED;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(this.id);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AdvancedShippingNoticeJpa other)) return false;
        return this.id != null && this.id.equals(other.id);
    }

    @Version
    private Integer version;

    @Transient
    private boolean isNew = true;

    @Override
    public boolean isNew() {
        return isNew;
    }

    @PostPersist
    @PostLoad
    void markNotNew() {
        this.isNew = false;
    }
}