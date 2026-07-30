package com.waregang.receiving_service.advanced_shipping_notice.application;

import com.waregang.receiving_service.advanced_shipping_notice.domain.model.AdvancedShippingNoticeJpa;
import com.waregang.receiving_service.advanced_shipping_notice.infrastructure.jpa_repositories.AdvancedShippingNoticeRepositoryJpa;
import com.waregang.receiving_service.advanced_shipping_notice.infrastructure.jpa_repositories.ContentRepositoryJpa;
import com.waregang.receiving_service.advanced_shipping_notice.infrastructure.jpa_repositories.HandlingUnitRepositoryJpa;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdvancedShippingNoticeServiceTest {

    @Mock private AdvancedShippingNoticeRepositoryJpa asnRepository;
    @Mock private HandlingUnitRepositoryJpa handlingUnitRepositoryJpa;
    @Mock private ContentRepositoryJpa contentRepositoryJpa;
    @Mock private AdvancedShippingNoticeMapper mapper;

    @InjectMocks private AdvancedShippingNoticeService asnService;

    @Test
    @DisplayName("Должен закрывать ASN")
    void shouldCloseAsnSuccessfully() {
        // Arrange
        UUID asnId = UUID.randomUUID();
        AdvancedShippingNoticeJpa asn = mock(AdvancedShippingNoticeJpa.class);
        when(asnRepository.findById(asnId)).thenReturn(Optional.of(asn));

        // Act
        asnService.closeAsn(asnId);

        // Assert
        verify(asn).close();
        verify(asnRepository).save(asn);
    }
}
