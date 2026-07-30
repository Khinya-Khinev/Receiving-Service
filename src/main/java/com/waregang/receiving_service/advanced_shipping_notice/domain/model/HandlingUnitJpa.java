package com.waregang.receiving_service.advanced_shipping_notice.domain.model;

import com.waregang.receiving_service.common.IdGenerator;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.jspecify.annotations.Nullable;
import org.springframework.data.domain.Persistable;

import java.util.*;

@Getter
@NoArgsConstructor(access = AccessLevel.PACKAGE)
@Entity
@Table(name = "handling_units")
public class HandlingUnitJpa implements Persistable<UUID> {

    @Id
    @Column(name = "id", updatable = false, nullable = false, columnDefinition = "uuid")
    @Setter(AccessLevel.PACKAGE) // For proxy creation in reconstitute
    private UUID id;

    @Column(name = "lpn", unique = true, nullable = false)
    private String lpn;

    @Nullable
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_unit_id")
    private HandlingUnitJpa parentUnit;

    @Column(name = "parent_unit_id", insertable = false, updatable = false)
    private UUID parentUnitId;

    @Column(name = "path", nullable = false)
    private String path;

    @Enumerated(value = EnumType.STRING)
    @Column(name = "type", nullable = false)
    private HandlingUnitType type;

    @OneToMany(
            mappedBy = "containerUnit",
            cascade = CascadeType.ALL,
            orphanRemoval = true)
    private final Set<ContentJpa> contents = new HashSet<>();

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "inbound_delivery_id", nullable = false)
    private AdvancedShippingNoticeJpa asn;

    private HandlingUnitJpa(String lpn, String path, @Nullable HandlingUnitJpa parentUnit, AdvancedShippingNoticeJpa asn) {
        this.id = IdGenerator.generate();
        this.lpn = lpn;
        this.path = path;
        this.parentUnit = parentUnit;
        this.parentUnitId = parentUnit != null ? parentUnit.getId() : null;
        this.type = HandlingUnitType.DEFAULT;
        this.asn = asn;
    }

    public static HandlingUnitJpa create(String lpn, String path, @Nullable HandlingUnitJpa parentUnit, AdvancedShippingNoticeJpa inboundDelivery) {
        return new HandlingUnitJpa(lpn, path, parentUnit, inboundDelivery);
    }

    public static HandlingUnitJpa reconstitute(UUID id, String lpn, String path, @Nullable UUID parentUnitId, HandlingUnitType type, UUID inboundDeliveryId) {
        HandlingUnitJpa unit = new HandlingUnitJpa();
        unit.id = id;
        unit.lpn = lpn;
        unit.path = path;
        unit.parentUnitId = parentUnitId;
        unit.type = type;

        AdvancedShippingNoticeJpa deliveryProxy = new AdvancedShippingNoticeJpa();
        deliveryProxy.setId(inboundDeliveryId);
        unit.asn = deliveryProxy;

        if (parentUnitId != null) {
            HandlingUnitJpa parentProxy = new HandlingUnitJpa();
            parentProxy.setId(parentUnitId);
            unit.parentUnit = parentProxy;
        }

        unit.isNew = false;
        return unit;
    }

    public void fillWithContent(String sku, Long quantity) {
        this.contents.add(new ContentJpa(
                sku,
                quantity,
                this
        ));
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(this.id);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof HandlingUnitJpa other)) return false;
        return this.id != null && this.id.equals(other.id);
    }

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