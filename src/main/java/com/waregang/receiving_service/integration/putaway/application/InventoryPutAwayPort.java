package com.waregang.receiving_service.integration.putaway.application;

import com.waregang.receiving_service.integration.putaway.infrastrusture.ForwardPutAwayRequest;

public interface InventoryPutAwayPort {
    void forwardForPutAway(ForwardPutAwayRequest dto);
}
