package com.waregang.receiving_service.receiving_process.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class ReceivedUnitTest {

    @Test
    @DisplayName("Должен корректно создавать экземпляр ReceivedUnit через factory метод")
    void shouldCreateReceivedUnitCorrectly() {
        // Arrange
        String lpn = "LPN-001";
        UUID parentUnitId = UUID.randomUUID();
        UUID workerSessionId = UUID.randomUUID();
        UUID receiptId = UUID.randomUUID();

        // Act
        ReceivedUnit unit = ReceivedUnit.create(lpn, parentUnitId, workerSessionId, receiptId);

        // Assert
        assertThat(unit.getId()).isNotNull();
        assertThat(unit.getLpn()).isEqualTo(lpn);
        assertThat(unit.getParentUnitId()).isEqualTo(parentUnitId);
        assertThat(unit.getWorkerSessionId()).isEqualTo(workerSessionId);
        assertThat(unit.getReceiptId()).isEqualTo(receiptId);
    }
}
