package com.waregang.receiving_service.test_utils.builders;

import com.waregang.receiving_service.receiving_process.domain.model.ReceivedContent;

import java.util.UUID;

public class ReceivedContentBuilder {
    private UUID id = UUID.randomUUID();
    private String sku = "SKU-" + UUID.randomUUID().toString().substring(0, 6);
    private Long quantity = 10L;
    private UUID containerUnitId = UUID.randomUUID();

    public static ReceivedContentBuilder aReceivedContent() {
        return new ReceivedContentBuilder();
    }

    public ReceivedContentBuilder withSku(String sku) {
        this.sku = sku;
        return this;
    }

    public ReceivedContentBuilder withQuantity(Long quantity) {
        this.quantity = quantity;
        return this;
    }

    public ReceivedContentBuilder withContainerUnitId(UUID containerUnitId) {
        this.containerUnitId = containerUnitId;
        return this;
    }

    public ReceivedContent build() {
        return ReceivedContent.reconstitute(id, sku, quantity, containerUnitId);
    }
}