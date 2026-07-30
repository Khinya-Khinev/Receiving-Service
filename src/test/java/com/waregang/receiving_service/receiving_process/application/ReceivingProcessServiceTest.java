package com.waregang.receiving_service.receiving_process.application;

import com.waregang.receiving_service.advanced_shipping_notice.application.AdvancedShippingNoticeService;
import com.waregang.receiving_service.advanced_shipping_notice.domain.model.AdvancedShippingNoticeJpa;
import com.waregang.receiving_service.receiving_process.api.dto.JoinReceivingResponse;
import com.waregang.receiving_service.receiving_process.api.dto.ScanHandlingUnitRequest;
import com.waregang.receiving_service.receiving_process.api.dto.ScanHandlingUnitResponse;
import com.waregang.receiving_service.receiving_process.domain.model.*;
import com.waregang.receiving_service.receiving_process.domain.ports.ReceivedContentRepositoryPort;
import com.waregang.receiving_service.receiving_process.domain.ports.ReceivedUnitRepositoryPort;
import com.waregang.receiving_service.receiving_process.domain.ports.WorkerReceivingSessionRepositoryPort;
import com.waregang.receiving_service.security.UserPrincipal;
import com.waregang.receiving_service.test_utils.mothers.UserPrincipalMother;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReceivingProcessServiceTest {

    @Mock private WorkerReceivingSessionRepositoryPort workerSessionRepository;
    @Mock private ReceivedUnitRepositoryPort receivedUnitRepository;
    @Mock private ReceivedContentRepositoryPort receivedContentRepository;
    @Mock private GoodsReceiptService goodsReceiptService;
    @Mock private AdvancedShippingNoticeService asnService;

    @InjectMocks private ReceivingProcessService receivingProcessService;

    private UserPrincipal worker;
    private UUID receiptId;

    @BeforeEach
    void setUp() {
        worker = UserPrincipalMother.worker("WH-001");
        receiptId = UUID.randomUUID();
    }

    @Test
    @DisplayName("Должен успешно присоединиться к приемке")
    void shouldJoinReceivingSuccessfully() {
        // Arrange
        GoodsReceipt receipt = mock(GoodsReceipt.class);
        when(receipt.getId())
                .thenReturn(receiptId);
        when(goodsReceiptService.findReceiptByIdWithLock(receiptId))
                .thenReturn(receipt);
        
        AdvancedShippingNoticeJpa asn = mock(AdvancedShippingNoticeJpa.class);
        when(asnService.findById(any()))
                .thenReturn(asn);
        when(asn.getReceivingMode())
                .thenReturn(ReceivingMode.ASN_MATCHING);
        when(asn.getId())
                .thenReturn(UUID.randomUUID());

        when(workerSessionRepository.save(any()))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        JoinReceivingResponse response = receivingProcessService.joinReceiving(worker, receiptId);

        // Assert
        assertThat(response).isNotNull();
        verify(workerSessionRepository)
                .save(any(WorkerReceivingSession.class));
    }
}
