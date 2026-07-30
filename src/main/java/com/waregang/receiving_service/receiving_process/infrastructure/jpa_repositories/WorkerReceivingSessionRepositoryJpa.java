package com.waregang.receiving_service.receiving_process.infrastructure.jpa_repositories;

import com.waregang.receiving_service.receiving_process.domain.model.WorkerReceivingSessionStatus;
import com.waregang.receiving_service.receiving_process.infrastructure.jpa_entities.WorkerReceivingSessionJpa;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;

import java.util.Optional;
import java.util.UUID;

public interface WorkerReceivingSessionRepositoryJpa extends JpaRepository<WorkerReceivingSessionJpa, UUID> {
    boolean existsByWorkerIdAndStatus(
            UUID workerId,
            WorkerReceivingSessionStatus status);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<WorkerReceivingSessionJpa> findWithLockByWorkerIdAndStatus(
            UUID workerId,
            WorkerReceivingSessionStatus status);


    Optional<WorkerReceivingSessionJpa> findByWorkerId(UUID id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<WorkerReceivingSessionJpa> findWithLockByWorkerId(UUID id);

    boolean existsByReceiptIdAndStatus(
            UUID receiptId,
            WorkerReceivingSessionStatus workerReceivingSessionStatus);

}
