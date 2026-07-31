package com.waregang.receiving_service.receiving_process.application.ports;

import com.waregang.receiving_service.receiving_process.domain.model.asn.AsnInfo;

import java.util.UUID;

public interface AsnInfoProviderPort {
    AsnInfo findAndMarkAsArrived(String asnNumber, String managerWarehouseId);
    AsnInfo getAsnInfoById(UUID asnId);
    void closeAsn(UUID asnId);
    void validateScannedHuAgainstAsn(String scannedLpn, UUID asnId);
    void validateScannedContentAgainstAsn(String scannedSku, UUID asnId);
}
