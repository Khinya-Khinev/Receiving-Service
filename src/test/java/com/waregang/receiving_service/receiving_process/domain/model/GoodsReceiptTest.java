package com.waregang.receiving_service.receiving_process.domain.model;

import com.waregang.receiving_service.common.exception_handling.AppException;
import com.waregang.receiving_service.receiving_process.domain.event.ClosedGoodsReceiptEvent;
import com.waregang.receiving_service.receiving_process.domain.event.OpenedGoodsReceiptEvent;
import com.waregang.receiving_service.security.UserPrincipal;
import com.waregang.receiving_service.test_utils.mothers.GoodsReceiptMother;
import com.waregang.receiving_service.test_utils.mothers.UserPrincipalMother;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GoodsReceiptTest {

    private static final String WAREHOUSE_A = "WH-001";
    private static final String WAREHOUSE_B = "WH-002";

    @Test
    @DisplayName("Фабричный метод open() должен создавать открытый GoodsReceipt и генерировать OpenedGoodsReceiptEvent")
    void shouldCreateOpenReceiptAndRegisterEvent() {
        // Arrange & Act
        UUID managerId = UUID.randomUUID();
        UUID deliveryId = UUID.randomUUID();

        GoodsReceipt receipt = GoodsReceipt.open(
                managerId, deliveryId, WAREHOUSE_A,
                ReceivingMode.ASN_MATCHING, "ASN-999", "GATE-01"
        );

        // Assert
        assertThat(receipt.getStatus()).isEqualTo(GoodsReceiptStatus.OPEN);
        assertThat(receipt.getWarehouseId()).isEqualTo(WAREHOUSE_A);

        assertThat(receipt.pullDomainEvents())
                .hasSize(1)
                .first()
                .isInstanceOf(OpenedGoodsReceiptEvent.class);
    }

    @Test
    @DisplayName("Запрещает подключения кладовщика из другого склада к документу приемки")
    void shouldThrowExceptionWhenWorkerWarehouseMismatch() {
        // Arrange
        UUID deliveryId = UUID.randomUUID();
        GoodsReceipt receipt = GoodsReceiptMother.openReceipt(deliveryId, UUID.randomUUID());
        // В Mother по умолчанию зашит WAREHOUSE_A ("WH-001")

        UserPrincipal foreignWorker = UserPrincipalMother.worker(WAREHOUSE_B);

        // Act & Assert
        assertThatThrownBy(() -> receipt.ensureAvailableForJoin(foreignWorker))
                .isInstanceOf(AppException.class);
    }

    @Test
    @DisplayName("Закрытие документа приемки переводит статус в CLOSED и публикуюет ClosedGoodsReceiptEvent")
    void shouldCloseReceiptSuccessfully() {
        // Arrange
        GoodsReceipt receipt = GoodsReceiptMother.openReceipt(UUID.randomUUID(), UUID.randomUUID());

        // Act
        receipt.close();

        // Assert
        assertThat(receipt.getStatus()).isEqualTo(GoodsReceiptStatus.CLOSED);

        assertThat(receipt.pullDomainEvents())
                .hasSize(1)
                .first()
                .isInstanceOf(ClosedGoodsReceiptEvent.class);
    }

    @Test
    @DisplayName("Выбрасывает исключение при попытке повторно закрыть уже закрытый GoodsReceipt")
    void shouldFailWhenClosingAlreadyClosedReceipt() {
        // Arrange
        GoodsReceipt receipt = GoodsReceiptMother.closedReceipt(UUID.randomUUID());

        // Act & Assert
        assertThatThrownBy(receipt::close)
                .isInstanceOf(AppException.class);
    }
}