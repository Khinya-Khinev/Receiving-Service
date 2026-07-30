package com.waregang.receiving_service.test_utils.builders;

import com.waregang.receiving_service.receiving_process.domain.model.ReceivedContent;
import com.waregang.receiving_service.receiving_process.domain.model.ReceivedUnit;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ReceivedUnitBuilder {
    private UUID id = UUID.randomUUID();
    private String lpn = "LPN-" + UUID.randomUUID().toString().substring(0, 6);
    private UUID receiptId = UUID.randomUUID();
    private UUID workerSessionId = UUID.randomUUID();
    private UUID parentUnitId = null;
    private final List<ReceivedUnit> children = new ArrayList<>();
    private final List<ReceivedContent> contents = new ArrayList<>();

    public static ReceivedUnitBuilder aReceivedUnit() {
        return new ReceivedUnitBuilder();
    }

    public ReceivedUnitBuilder withId(UUID id) {
        this.id = id;
        return this;
    }

    public ReceivedUnitBuilder withLpn(String lpn) {
        this.lpn = lpn;
        return this;
    }

    public ReceivedUnitBuilder withReceiptId(UUID receiptId) {
        this.receiptId = receiptId;
        return this;
    }

    public ReceivedUnitBuilder withWorkerSessionId(UUID workerSessionId) {
        this.workerSessionId = workerSessionId;
        return this;
    }

    public ReceivedUnitBuilder withParentUnitId(@Nullable UUID parentUnitId) {
        this.parentUnitId = parentUnitId;
        return this;
    }

    public ReceivedUnitBuilder withChild(ReceivedUnit child) {
        this.children.add(child);
        return this;
    }

    public ReceivedUnitBuilder withContent(ReceivedContent content) {
        this.contents.add(content);
        return this;
    }

    public ReceivedUnit build() {
        ReceivedUnit unit = ReceivedUnit.reconstitute(
                id, lpn, parentUnitId, workerSessionId, receiptId
        );
        // Добавляем детей и контент через сеттеры или рефлексию
        return unit;
    }

    public ReceivedUnit buildWithContent(String sku, Long quantity) {
        ReceivedUnit unit = build();
        // Через рефлексию добавляем контент
        return unit;
    }
}

// Builder для ReceivedContent
