package com.waregang.receiving_service.receiving_process.infrastructure;

import com.waregang.receiving_service.advanced_shipping_notice.application.AdvancedShippingNoticeService;
import com.waregang.receiving_service.advanced_shipping_notice.domain.model.AdvancedShippingNoticeJpa;
import com.waregang.receiving_service.receiving_process.domain.model.asn.AsnInfo;
import com.waregang.receiving_service.receiving_process.application.ports.AsnInfoProviderPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class AsnInfoProviderAdapter implements AsnInfoProviderPort {

    private final AdvancedShippingNoticeService asnService;

    @Override
    public AsnInfo findAndMarkAsArrived(String asnNumber, String managerWarehouseId) {
        AdvancedShippingNoticeJpa asn = asnService.findByAsn(asnNumber);
        asnService.markAsArrived(asn, managerWarehouseId);
        return toAsnInfo(asn);
    }

    @Override
    public AsnInfo getAsnInfoById(UUID asnId) {
        return toAsnInfo(asnService.findById(asnId));
    }

    @Override
    public void closeAsn(UUID asnId) {
        asnService.closeAsn(asnId);
    }

    @Override
    public void validateScannedHuAgainstAsn(String scannedLpn, UUID asnId) {
        asnService.validateScannedHuAgainstAsn(scannedLpn, asnId);
    }

    @Override
    public void validateScannedContentAgainstAsn(String scannedSku, UUID asnId) {
        asnService.validateScannedContentAgainstAsn(scannedSku, asnId);
    }

    private AsnInfo toAsnInfo(AdvancedShippingNoticeJpa asn) {
        return new AsnInfo(
                asn.getId(),
                asn.getWarehouseId(),
                asn.getReceivingMode(),
                asn.getAsnNumber()
        );
    }
}
