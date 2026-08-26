package com.waregang.receiving_service.receiving_process.application.ports;

import com.waregang.receiving_service.receiving_process.domain.model.GoodsReceipt;
import com.waregang.receiving_service.receiving_process.domain.model.GoodsReceiptStatus;
import com.waregang.receiving_service.receiving_process.domain.dto.GoodsReceiptDto;
import com.waregang.receiving_service.receiving_process.infrastructure.jpa_entities.GoodsReceiptJpa;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface GoodsReceiptRepositoryPort {
    GoodsReceipt save(GoodsReceipt receipt);

    GoodsReceipt update(GoodsReceipt receipt);

    Optional<GoodsReceipt> findWithLockById(UUID receiptId);

    Page<GoodsReceiptJpa> findAll(Specification<GoodsReceiptJpa> spec, Pageable pageable);

    Optional<GoodsReceipt> findById(UUID receiptId);
}