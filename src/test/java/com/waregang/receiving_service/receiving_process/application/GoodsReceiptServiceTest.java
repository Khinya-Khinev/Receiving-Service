package com.waregang.receiving_service.receiving_process.application;

import com.waregang.receiving_service.advanced_shipping_notice.application.AdvancedShippingNoticeService;
import com.waregang.receiving_service.advanced_shipping_notice.domain.model.AdvancedShippingNoticeJpa;
import com.waregang.receiving_service.receiving_process.api.dto.StartReceivingRequest;
import com.waregang.receiving_service.receiving_process.api.dto.StartReceivingResponse;
import com.waregang.receiving_service.receiving_process.domain.model.GoodsReceipt;
import com.waregang.receiving_service.receiving_process.domain.model.GoodsReceiptStatus;
import com.waregang.receiving_service.receiving_process.domain.ports.GoodsReceiptRepositoryPort;
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
class GoodsReceiptServiceTest {

    @Mock private AdvancedShippingNoticeService asnService;
    @Mock private GoodsReceiptRepositoryPort goodsReceiptRepositoryPort;
    @Mock private WorkerReceivingSessionRepositoryPort workerSessionRepository;

    @InjectMocks private GoodsReceiptService goodsReceiptService;

    private UserPrincipal manager;

    @BeforeEach
    void setUp() {
        manager = UserPrincipalMother.manager("WH-001");
    }

    @Test
    @DisplayName("Должен успешно открывать приемку")
    void shouldStartReceivingSuccessfully() {
        // Arrange
        String asnNumber = "ASN-123";
        StartReceivingRequest request = new StartReceivingRequest(asnNumber, "GATE-01");
        
        AdvancedShippingNoticeJpa asn = mock(AdvancedShippingNoticeJpa.class);

        when(asn.getId()).thenReturn(UUID.randomUUID());
        when(asn.getWarehouseId()).thenReturn("WH-001");
        when(asn.getReceivingMode()).thenReturn(com.waregang.receiving_service.receiving_process.domain.model.ReceivingMode.ASN_MATCHING);
        when(asn.getAsnNumber()).thenReturn(asnNumber);

        when(asnService.findByAsn(asnNumber)).thenReturn(asn);

        // Act
        StartReceivingResponse response = goodsReceiptService.startReceiving(request, manager);

        // Assert
        assertThat(response).isNotNull();
        verify(goodsReceiptRepositoryPort).save(any(GoodsReceipt.class));
    }
}
