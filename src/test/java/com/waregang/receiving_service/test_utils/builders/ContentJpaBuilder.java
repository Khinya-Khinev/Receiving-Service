package com.waregang.receiving_service.test_utils.builders;

import com.waregang.receiving_service.advanced_shipping_notice.domain.model.ContentJpa;
import com.waregang.receiving_service.advanced_shipping_notice.domain.model.HandlingUnitJpa;

import java.lang.reflect.Field;
import java.util.UUID;

public class ContentJpaBuilder {
    private UUID id = UUID.randomUUID();
    private String sku = "SKU-" + UUID.randomUUID().toString().substring(0, 6);
    private Long quantity = 10L;
    private HandlingUnitJpa containerUnit = null;
    private boolean isNew = true;

    public static ContentJpaBuilder aContent() {
        return new ContentJpaBuilder();
    }

    public ContentJpaBuilder withId(UUID id) {
        this.id = id;
        return this;
    }

    public ContentJpaBuilder withSku(String sku) {
        this.sku = sku;
        return this;
    }

    public ContentJpaBuilder withQuantity(Long quantity) {
        this.quantity = quantity;
        return this;
    }

    public ContentJpaBuilder withContainerUnit(HandlingUnitJpa containerUnit) {
        this.containerUnit = containerUnit;
        return this;
    }

    public ContentJpaBuilder asNew(boolean isNew) {
        this.isNew = isNew;
        return this;
    }

    public ContentJpa build() {
        ContentJpa content = new ContentJpa(sku, quantity, containerUnit);
        setField(content, "id", id);
        setField(content, "isNew", isNew);
        return content;
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