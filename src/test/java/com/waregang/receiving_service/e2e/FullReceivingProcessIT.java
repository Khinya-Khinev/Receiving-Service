package com.waregang.receiving_service.e2e;

import com.waregang.receiving_service.advanced_shipping_notice.api.dto.*;
import com.waregang.receiving_service.advanced_shipping_notice.application.AdvancedShippingNoticeService;
import com.waregang.receiving_service.advanced_shipping_notice.domain.model.*;
import com.waregang.receiving_service.advanced_shipping_notice.infrastructure.jpa_repositories.AdvancedShippingNoticeRepositoryJpa;
import com.waregang.receiving_service.integration.discrepancies_report.application.DiscrepanciesReportPort;
import com.waregang.receiving_service.integration.putaway.application.InventoryPutAwayPort;
import com.waregang.receiving_service.integration.discrepancies_report.application.DiscrepanciesReport;
import com.waregang.receiving_service.integration.discrepancies_report.application.DiscrepancyLine;
import com.waregang.receiving_service.integration.discrepancies_report.application.DiscrepancyType;
import com.waregang.receiving_service.integration.putaway.infrastrusture.ForwardPutAwayRequest;
import com.waregang.receiving_service.receiving_process.api.dto.JoinReceivingResponse;
import com.waregang.receiving_service.receiving_process.api.dto.ScanContentRequest;
import com.waregang.receiving_service.receiving_process.api.dto.ScanHandlingUnitRequest;
import com.waregang.receiving_service.receiving_process.api.dto.ScanHandlingUnitResponse;
import com.waregang.receiving_service.receiving_process.api.dto.StartReceivingRequest;
import com.waregang.receiving_service.receiving_process.api.dto.StartReceivingResponse;
import com.waregang.receiving_service.receiving_process.application.GoodsReceiptService;
import com.waregang.receiving_service.receiving_process.application.ReceivingProcessService;
import com.waregang.receiving_service.receiving_process.domain.model.GoodsReceipt;
import com.waregang.receiving_service.receiving_process.domain.model.GoodsReceiptStatus;
import com.waregang.receiving_service.receiving_process.domain.model.WorkerReceivingSession;
import com.waregang.receiving_service.receiving_process.domain.model.WorkerReceivingSessionStatus;
import com.waregang.receiving_service.receiving_process.domain.ports.GoodsReceiptRepositoryPort;
import com.waregang.receiving_service.receiving_process.domain.ports.WorkerReceivingSessionRepositoryPort;
import com.waregang.receiving_service.security.UserPrincipal;
import com.waregang.receiving_service.test_utils.BaseIT;
import com.waregang.receiving_service.test_utils.mothers.UserPrincipalMother;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class FullReceivingProcessIT extends BaseIT {

    @Autowired private GoodsReceiptService goodsReceiptService;
    @Autowired private ReceivingProcessService receivingProcessService;
    @Autowired private AdvancedShippingNoticeService asnService;
    @Autowired private AdvancedShippingNoticeRepositoryJpa asnRepositoryJpa;
    @Autowired private GoodsReceiptRepositoryPort goodsReceiptRepositoryPort;
    @Autowired private WorkerReceivingSessionRepositoryPort workerSessionRepositoryPort;

    @MockitoBean private DiscrepanciesReportPort discrepanciesReportPort;
    @MockitoBean private InventoryPutAwayPort inventoryPutAwayPort;

    @Test
    void shouldCompleteFullReceivingWithNoDiscrepancies() {
        // 1. Подготовка пользователей
        UserPrincipal manager = UserPrincipalMother.manager("WH-001");
        UserPrincipal worker = UserPrincipalMother.worker("WH-001");

        // 2. Создание ASN
        String asnNumber = "ASN-123";
        String lpn = "LPN-PALLET-001";
        String sku = "SKU-123";
        Long expectedQty = 10L;

        CreateAsnRequest createRequest = new CreateAsnRequest(
                "EXT-123",
                asnNumber,
                "WH-001",
                "vendor name",
                LocalDateTime.now().plusDays(1),
                List.of(new CreateUnitRequest("PALLET", lpn, null)),
                List.of(new CreateContentRequest(lpn, sku, expectedQty))
        );

        CreateAsnResponse createResponse = asnService.createAsn(createRequest);
        UUID asnId = createResponse.asnId();

        // 3. Открытие документа приемки
        StartReceivingRequest startRequest = new StartReceivingRequest(asnNumber, "GATE-01");
        StartReceivingResponse startResponse = goodsReceiptService.startReceiving(startRequest, manager);
        UUID receiptId = startResponse.receiptId();

        // 4. Работник присоединяется
        JoinReceivingResponse joinResponse = receivingProcessService.joinReceiving(worker, receiptId);
        UUID sessionId = joinResponse.workerSessionId();

        // 5. Сканирование LPN
        ScanHandlingUnitRequest scanUnitRequest = new ScanHandlingUnitRequest(lpn);
        ScanHandlingUnitResponse scanUnitResponse = receivingProcessService.scanHandlingUnit(scanUnitRequest, worker);

        // 6. Сканирование содержимого
        ScanContentRequest scanContentRequest = new ScanContentRequest(sku, expectedQty);
        receivingProcessService.scanContent(scanContentRequest, worker);

        // 7. Завершение сессии
        receivingProcessService.completeWorkerSession(worker);

        // 8. Закрытие документа
        goodsReceiptService.closeReceiving(manager, receiptId);

        // 9. Проверки статусов
        AdvancedShippingNoticeJpa updatedAsn = asnRepositoryJpa.findByIdWithHandlingUnits(asnId).orElseThrow();
        assertThat(updatedAsn.getStatus()).isEqualTo(AdvancedShippingNoticeStatus.CLOSED);

        GoodsReceipt receipt = goodsReceiptRepositoryPort.findById(receiptId).orElseThrow();
        assertThat(receipt.getStatus()).isEqualTo(GoodsReceiptStatus.CLOSED);

        Optional<WorkerReceivingSession> session = workerSessionRepositoryPort.findByWorkerIdAndStatus(worker.id(), WorkerReceivingSessionStatus.COMPLETED);
        assertThat(session).isPresent();


        // Проверка сохранённых единиц и содержимого
        assertThat(updatedAsn.getHandlingUnits()).hasSize(1);
        HandlingUnitJpa unit = updatedAsn.getHandlingUnits().iterator().next();
        assertThat(unit.getLpn()).isEqualTo(lpn);

        assertThat(unit.getContents()).hasSize(1);
        ContentJpa contentJpa = unit.getContents().iterator().next();
        assertThat(contentJpa.getSku()).isEqualTo(sku);
        assertThat(contentJpa.getQuantity()).isEqualTo(expectedQty);

        // 10. Проверка интеграционных вызовов
        // Отчёт о расхождениях не отправлен (всё совпало)
        verify(discrepanciesReportPort, never()).sendReport(any());

        // Запрос на размещение отправлен
        ArgumentCaptor<ForwardPutAwayRequest> putAwayCaptor = ArgumentCaptor.forClass(ForwardPutAwayRequest.class);
        verify(inventoryPutAwayPort, times(1)).forwardForPutAway(putAwayCaptor.capture());
        ForwardPutAwayRequest putAwayRequest = putAwayCaptor.getValue();
        assertThat(putAwayRequest.workerSessionId()).isEqualTo(sessionId);
        assertThat(putAwayRequest.receivedUnits()).hasSize(1);
        var rootDto = putAwayRequest.receivedUnits().get(0);
        assertThat(rootDto.lpn()).isEqualTo(lpn);
        assertThat(rootDto.contents()).hasSize(1);
        assertThat(rootDto.contents().get(0).sku()).isEqualTo(sku);
        assertThat(rootDto.contents().get(0).quantity()).isEqualTo(expectedQty);
    }

    @Test
    void shouldProcessFullReceivingWithDiscrepancies() {
        // 1. Подготовка пользователей
        UserPrincipal manager = UserPrincipalMother.manager("WH-001");
        UserPrincipal worker = UserPrincipalMother.worker("WH-001");

        // 2. Создание ASN
        String asnNumber = "ASN-456";
        String lpn = "LPN-PALLET-002";
        String sku = "SKU-456";
        Long expectedQty = 10L;   // ожидаем 10
        Long actualQty = 7L;      // сканируем 7 → shortage

        CreateAsnRequest createRequest = new CreateAsnRequest(
                "EXT-456",
                asnNumber,
                "WH-001",
                "vendor name",
                LocalDateTime.now().plusDays(1),
                List.of(new CreateUnitRequest("PALLET", lpn, null)),
                List.of(new CreateContentRequest(lpn, sku, expectedQty))
        );

        CreateAsnResponse createResponse = asnService.createAsn(createRequest);
        UUID asnId = createResponse.asnId();

        // 3. Открытие документа приемки
        StartReceivingRequest startRequest = new StartReceivingRequest(asnNumber, "GATE-02");
        StartReceivingResponse startResponse = goodsReceiptService.startReceiving(startRequest, manager);
        UUID receiptId = startResponse.receiptId();

        // 4. Работник присоединяется
        JoinReceivingResponse joinResponse = receivingProcessService.joinReceiving(worker, receiptId);
        UUID sessionId = joinResponse.workerSessionId();

        // 5. Сканирование LPN
        receivingProcessService.scanHandlingUnit(new ScanHandlingUnitRequest(lpn), worker);

        // 6. Сканирование содержимого с меньшим количеством
        receivingProcessService.scanContent(new ScanContentRequest(sku, actualQty), worker);

        // 7. Завершение сессии
        receivingProcessService.completeWorkerSession(worker);

        // 8. Закрытие документа
        goodsReceiptService.closeReceiving(manager, receiptId);

        // 9. Проверки статусов
        AdvancedShippingNoticeJpa updatedAsn = asnRepositoryJpa.findByIdWithHandlingUnits(asnId).orElseThrow();
        assertThat(updatedAsn.getStatus()).isEqualTo(AdvancedShippingNoticeStatus.CLOSED);

        GoodsReceipt receipt = goodsReceiptRepositoryPort.findById(receiptId).orElseThrow();
        assertThat(receipt.getStatus()).isEqualTo(GoodsReceiptStatus.CLOSED);

        // 10. Проверка интеграционных вызовов
        // Отчёт о расхождениях должен быть отправлен
        ArgumentCaptor<DiscrepanciesReport> reportCaptor = ArgumentCaptor.forClass(DiscrepanciesReport.class);
        verify(discrepanciesReportPort, times(1)).sendReport(reportCaptor.capture());
        DiscrepanciesReport report = reportCaptor.getValue();
        assertThat(report.goodsReceiptId()).isEqualTo(receiptId);
        assertThat(report.discrepancyLines()).hasSize(1);
        DiscrepancyLine discrepancy = report.discrepancyLines().get(0);
        assertThat(discrepancy.sku()).isEqualTo(sku);
        assertThat(discrepancy.expected()).isEqualTo(expectedQty);
        assertThat(discrepancy.actual()).isEqualTo(actualQty);
        assertThat(discrepancy.type()).isEqualTo(DiscrepancyType.SHORTAGE);

        // Запрос на размещение также должен быть отправлен (даже при расхождениях)
        verify(inventoryPutAwayPort, times(1)).forwardForPutAway(any(ForwardPutAwayRequest.class));
    }
}
