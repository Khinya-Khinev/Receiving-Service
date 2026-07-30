package com.waregang.receiving_service.test_utils.builders;

import com.waregang.receiving_service.advanced_shipping_notice.domain.model.*;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class HandlingUnitJpaBuilder {
    private UUID id = UUID.randomUUID();
    private String lpn = "LPN-" + UUID.randomUUID().toString().substring(0, 8);
    private HandlingUnitType type = HandlingUnitType.DEFAULT;
    private HandlingUnitJpa parentUnit = null;
    private String path = "LPN-TEST";
    private AdvancedShippingNoticeJpa asn = null;
    private boolean isNew = true;

    private final List<HandlingUnitJpa> childUnits = new ArrayList<>();
    private final List<ContentJpa> contents = new ArrayList<>();

    public static HandlingUnitJpaBuilder aHandlingUnit() {
        return new HandlingUnitJpaBuilder();
    }

    public HandlingUnitJpaBuilder withId(UUID id) {
        this.id = id;
        return this;
    }

    public HandlingUnitJpaBuilder withLpn(String lpn) {
        this.lpn = lpn;
        return this;
    }

    public HandlingUnitJpaBuilder withPath(String path) {
        this.path = path;
        return this;
    }

    public HandlingUnitJpaBuilder withType(HandlingUnitType type) {
        this.type = type;
        return this;
    }

    public HandlingUnitJpaBuilder withParentUnit(HandlingUnitJpa parentUnit) {
        this.parentUnit = parentUnit;
        return this;
    }

    public HandlingUnitJpaBuilder withAsn(AdvancedShippingNoticeJpa asn) {
        this.asn = asn;
        return this;
    }

    public HandlingUnitJpaBuilder asNew(boolean isNew) {
        this.isNew = isNew;
        return this;
    }

    public HandlingUnitJpaBuilder withContent(ContentJpa content) {
        this.contents.add(content);
        return this;
    }

    public HandlingUnitJpa build() {
        HandlingUnitJpa unit = HandlingUnitJpa.create(lpn, path, parentUnit, asn);
        setField(unit, "id", id);
        setField(unit, "type", type);
        setField(unit, "isNew", isNew);

        for (ContentJpa content : contents) {
            unit.fillWithContent(content.getSku(), content.getQuantity());
        }

        return unit;
    }

    private static void setField(Object target, String fieldName, Object value) {
        try {
            Field field = target.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(target, value);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException("Failed to set field " + fieldName + " on " + target.getClass().getSimpleName(), e);
        }
    }
}