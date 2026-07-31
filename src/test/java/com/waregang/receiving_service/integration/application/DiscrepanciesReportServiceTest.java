package com.waregang.receiving_service.integration.application;

import com.waregang.receiving_service.advanced_shipping_notice.infrastructure.jpa_repositories.AdvancedShippingNoticeRepositoryJpa;
import com.waregang.receiving_service.integration.discrepancies_report.application.DiscrepanciesReportPort;
import com.waregang.receiving_service.integration.discrepancies_report.application.DiscrepanciesReportService;
import com.waregang.receiving_service.SkuQuantityDto;
import com.waregang.receiving_service.receiving_process.domain.event.ClosedGoodsReceiptEvent;
import com.waregang.receiving_service.receiving_process.domain.ports.ReceivedContentRepositoryPort;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DiscrepanciesReportServiceTest {

    @Mock private DiscrepanciesReportPort port;
    @Mock private AdvancedShippingNoticeRepositoryJpa asnRepository;
    @Mock private ReceivedContentRepositoryPort contentRepository;

    @InjectMocks private DiscrepanciesReportService discrepanciesReportService;

    @Test
    @DisplayName("Должен отправлять отчет, если есть расхождения")
    void shouldProcessClosedEventAndSendReport() {
        // Arrange
        UUID receiptId = UUID.randomUUID();
        UUID deliveryId = UUID.randomUUID();
        ClosedGoodsReceiptEvent event = new ClosedGoodsReceiptEvent(receiptId, deliveryId, "GATE-01");

        when(contentRepository.findActualSkuQuantitiesByReceiptId(receiptId))
                .thenReturn(List.of(new SkuQuantityDto("SKU-1", 5L)));
        when(asnRepository.findExpectedSkuQuantities(deliveryId))
                .thenReturn(List.of(new SkuQuantityDto("SKU-1", 10L)));

        // Act
        discrepanciesReportService.processClosedEvent(event);

        // Assert
        verify(port).sendReport(any());
    }
}
