package com.waregang.receiving_service.receiving_process.application;

import com.waregang.receiving_service.receiving_process.api.dto.StartReceivingRequest;
import com.waregang.receiving_service.receiving_process.api.dto.StartReceivingResponse;
import com.waregang.receiving_service.receiving_process.domain.model.GoodsReceipt;
import com.waregang.receiving_service.receiving_process.domain.model.asn.AsnInfo;
import com.waregang.receiving_service.receiving_process.application.ports.AsnInfoProviderPort;
import com.waregang.receiving_service.receiving_process.application.ports.GoodsReceiptRepositoryPort;
import com.waregang.receiving_service.receiving_process.application.ports.WorkerReceivingSessionRepositoryPort;
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

    @Mock private AsnInfoProviderPort asnInfoProvider;
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
        
        AsnInfo asnInfo = new AsnInfo(
                UUID.randomUUID(),
                "WH-001",
                com.waregang.receiving_service.receiving_process.domain.model.ReceivingMode.ASN_MATCHING,
                asnNumber
        );

        when(asnInfoProvider.findAndMarkAsArrived(asnNumber, manager.warehouseId())).thenReturn(asnInfo);

        // Act
        StartReceivingResponse response = goodsReceiptService.startReceiving(request, manager);

        // Assert
        assertThat(response).isNotNull();
        verify(goodsReceiptRepositoryPort).save(any(GoodsReceipt.class));
    }
}