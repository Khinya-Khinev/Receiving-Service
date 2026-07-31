package com.waregang.receiving_service.integration.application;

import com.waregang.receiving_service.integration.putaway.application.InventoryIntegrationService;
import com.waregang.receiving_service.integration.putaway.application.InventoryPutAwayPort;
import com.waregang.receiving_service.integration.putaway.application.PutAwayMapper;
import com.waregang.receiving_service.integration.putaway.infrastrusture.ForwardPutAwayRequest;
import com.waregang.receiving_service.receiving_process.domain.event.WorkerSessionClosedEvent;
import com.waregang.receiving_service.receiving_process.domain.model.ReceivedUnit;
import com.waregang.receiving_service.receiving_process.application.ports.ReceivedUnitRepositoryPort;
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
class InventoryIntegrationServiceTest {

    @Mock private InventoryPutAwayPort putAwayPort;
    @Mock private ReceivedUnitRepositoryPort receivedUnitRepository;
    @Mock private PutAwayMapper putAwayMapper;

    @InjectMocks private InventoryIntegrationService inventoryIntegrationService;

    @Test
    @DisplayName("Должен отправлять запрос на put-away, если есть юниты")
    void shouldForwardPutAwayRequest() {
        // Arrange
        UUID workerSessionId = UUID.randomUUID();
        WorkerSessionClosedEvent event = new WorkerSessionClosedEvent(workerSessionId);
        List<ReceivedUnit> units = List.of(mock(ReceivedUnit.class));
        
        when(receivedUnitRepository.findAllRootUnitsByWorkerSessionId(workerSessionId)).thenReturn(units);
        when(putAwayMapper.toPutAwayRequestDto(eq(units), eq(workerSessionId), anyString()))
                .thenReturn(new ForwardPutAwayRequest(workerSessionId, "2026-07-27T19:00:00Z", List.of()));

        // Act
        inventoryIntegrationService.submitForPutAway(event);

        // Assert
        verify(putAwayPort).forwardForPutAway(any());
    }
}
