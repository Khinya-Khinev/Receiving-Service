package com.waregang.receiving_service.receiving_process.domain.model;

import com.waregang.receiving_service.common.exception_handling.AppException;
import com.waregang.receiving_service.receiving_process.domain.event.WorkerSessionClosedEvent;
import com.waregang.receiving_service.test_utils.mothers.WorkerSessionMother;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class WorkerReceivingSessionTest {

    @Nested
    @DisplayName("Навигация по грузоместам (LPN Path)")
    class NavigationTests {

        @Test
        @DisplayName("Должен корректно формировать LPN path при погружении вложенности")
        void shouldBuildLpnPathWhenNavigatingDeep() {
            // Arrange
            WorkerReceivingSession session = WorkerSessionMother.newWorkerSession(
                    UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID()
            );
            UUID palletId = UUID.randomUUID();
            UUID boxId = UUID.randomUUID();

            // Act - Сканируем паллету, затем коробку внутри нее
            session.navigateToUnit(palletId, "PALLET-001");
            session.navigateToUnit(boxId, "BOX-100");

            // Assert
            assertThat(session.getCurrentUnitId()).isEqualTo(boxId);
            assertThat(session.getCurrentUnitLpnPath()).isEqualTo("/PALLET-001/BOX-100");
            assertThat(session.getCurrentUnitLpn()).isEqualTo("BOX-100");
        }

        @Test
        @DisplayName("Должен корректно возвращаться на уровень выше при navigateBack")
        void shouldNavigateBackToParentUnit() {
            // Arrange
            WorkerReceivingSession session = WorkerSessionMother.newWorkerSession(
                    UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID()
            );
            UUID palletId = UUID.randomUUID();
            UUID boxId = UUID.randomUUID();

            session.navigateToUnit(palletId, "PALLET-001");
            session.navigateToUnit(boxId, "BOX-100");

            // Act - Возвращаемся обратно на паллету
            session.navigateBack(palletId);

            // Assert
            assertThat(session.getCurrentUnitId()).isEqualTo(palletId);
            assertThat(session.getCurrentUnitLpnPath()).isEqualTo("/PALLET-001");
            assertThat(session.getCurrentUnitLpn()).isEqualTo("PALLET-001");
        }

        @Test
        @DisplayName("Должен выбрасывать исключение при попытке навигации назад, если ни один юнит не был отсканирован")
        void shouldThrowExceptionWhenNavigatingBackWithoutCurrentUnit() {
            // Arrange
            WorkerReceivingSession session = WorkerSessionMother.newWorkerSession(
                    UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID()
            );

            // Act & Assert
            assertThatThrownBy(() -> session.navigateBack(null))
                    .isInstanceOf(AppException.class);
        }
    }

    @Nested
    @DisplayName("Проверка доступности операций (Pre-conditions)")
    class AvailabilityValidationTests {

        @Test
        @DisplayName("Запрещает сканирование товара (content), если не выбран активный юнит")
        void shouldFailContentScanIfNoUnitSelected() {
            // Arrange - Сессия открыта, но текущий LPN/Unit не выставили
            WorkerReceivingSession session = WorkerSessionMother.newWorkerSession(
                    UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID()
            );

            // Act & Assert
            assertThatThrownBy(session::ensureAvailableForContentScan)
                    .isInstanceOf(AppException.class);
        }

        @Test
        @DisplayName("Разрешает сканирование товара, если юнит активен и сессия IN_PROCESS")
        void shouldAllowContentScanWhenUnitIsActive() {
            // Arrange
            WorkerReceivingSession session = WorkerSessionMother.newWorkerSession(
                    UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID()
            );
            session.navigateToUnit(UUID.randomUUID(), "PALLET-001");

            // Act & Assert (Не должно выбросить исключение)
            session.ensureAvailableForContentScan();
        }
    }

    @Nested
    @DisplayName("Завершение сессии и доменные события")
    class LifecycleTests {

        @Test
        @DisplayName("При завершении сессии сбрасывает фокус с юнита и публикует WorkerSessionClosedEvent")
        void shouldCompleteSessionAndRegisterEvent() {
            // Arrange
            WorkerReceivingSession session = WorkerSessionMother.sessionFocusedOnUnit(
                    UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(),
                    UUID.randomUUID(), "/PALLET-001"
            );

            // Act
            session.complete();

            // Assert
            assertThat(session.getStatus()).isEqualTo(WorkerReceivingSessionStatus.COMPLETED);
            assertThat(session.getCurrentUnitId()).isNull();
            assertThat(session.getCurrentUnitLpnPath()).isNull();

            // Проверка доменного события
            assertThat(session.pullDomainEvents())
                    .hasSize(1)
                    .first()
                    .isInstanceOf(WorkerSessionClosedEvent.class)
                    .extracting("workerSessionId")
                    .isEqualTo(session.getId());
        }
    }
}