package com.waregang.receiving_service.receiving_process.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class ReceivedContentTest {

    @Test
    @DisplayName("Должен корректно создавать экземпляр ReceivedContent через factory метод")
    void shouldCreateReceivedContentCorrectly() {
        // Arrange
        String sku = "SKU-123";
        Long quantity = 10L;
        UUID containerUnitId = UUID.randomUUID();

        // Act
        ReceivedContent content = ReceivedContent.create(sku, quantity, containerUnitId);

        // Assert
        assertThat(content.getId()).isNotNull();
        assertThat(content.getSku()).isEqualTo(sku);
        assertThat(content.getQuantity()).isEqualTo(quantity);
        assertThat(content.getContainerUnitId()).isEqualTo(containerUnitId);
    }
}
