package com.waregang.receiving_service.receiving_process.infrastructure.adapters;

import com.waregang.receiving_service.common.exception_handling.AppException;
import com.waregang.receiving_service.common.exception_handling.DatabaseExceptionTranslator;
import com.waregang.receiving_service.common.exception_handling.error_code.ReceivingErrorCode;
import com.waregang.receiving_service.receiving_process.domain.model.WorkerReceivingSession;
import com.waregang.receiving_service.receiving_process.domain.model.WorkerReceivingSessionStatus;
import com.waregang.receiving_service.receiving_process.application.ports.WorkerReceivingSessionRepositoryPort;
import com.waregang.receiving_service.receiving_process.infrastructure.jpa_entities.WorkerReceivingSessionJpa;
import com.waregang.receiving_service.receiving_process.infrastructure.jpa_repositories.WorkerReceivingSessionRepositoryJpa;
import com.waregang.receiving_service.receiving_process.infrastructure.mappers.WorkerReceivingSessionMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@RequiredArgsConstructor

@Repository
public class WorkerReceivingSessionJpaRepoAdapter implements WorkerReceivingSessionRepositoryPort {
    private final WorkerReceivingSessionRepositoryJpa repositoryJpa;
    private final WorkerReceivingSessionMapper mapper;
    private final ApplicationEventPublisher eventPublisher;
    private final DatabaseExceptionTranslator databaseExceptionTranslator;

    @Override
    public boolean existsByReceiptIdAndStatus(UUID receiptId, WorkerReceivingSessionStatus workerReceivingSessionStatus) {
        return repositoryJpa.existsByReceiptIdAndStatus(receiptId, workerReceivingSessionStatus);
    }

    @Override
    public boolean existsByWorkerIdAndStatus(UUID workerId, WorkerReceivingSessionStatus workerReceivingSessionStatus) {
        return repositoryJpa.existsByWorkerIdAndStatus(workerId, workerReceivingSessionStatus);
    }

    @Override
    public WorkerReceivingSession save(WorkerReceivingSession session) {
        try {
            WorkerReceivingSessionJpa saved = repositoryJpa.save(mapper.toJpa(session));
            repositoryJpa.flush();
            session.pullDomainEvents().forEach(eventPublisher::publishEvent);
            return mapper.toDomain(saved);
        } catch (DataIntegrityViolationException e) {
            throw databaseExceptionTranslator.translate(e);
        }
    }


    @Override
    public WorkerReceivingSession update(WorkerReceivingSession session) {
        try {
            WorkerReceivingSessionJpa sessionJpa = repositoryJpa.findById(session.getId())
                    .orElseThrow(() -> AppException.of(ReceivingErrorCode.WORKER_SESSION_NOT_FOUND)
                            .with("session_id", session.getId()));

            mapper.updateJpaFromDomain(sessionJpa, session);

            repositoryJpa.flush();

            session.pullDomainEvents().forEach(eventPublisher::publishEvent);

            return mapper.toDomain(sessionJpa);
        } catch (DataIntegrityViolationException e) {
            throw databaseExceptionTranslator.translate(e);
        }
    }

    @Override
    @Transactional
    public Optional<WorkerReceivingSession> findByWorkerIdAndStatus(UUID id, WorkerReceivingSessionStatus workerReceivingSessionStatus) {
        return repositoryJpa.findWithLockByWorkerIdAndStatus(id, workerReceivingSessionStatus)
                .map(mapper::toDomain);
    }

    @Override
    public Set<WorkerReceivingSession> findAll() {
        return repositoryJpa.findAll().stream()
                .map(mapper::toDomain)
                .collect(Collectors.toSet());
    }

    @Override
    public Optional<WorkerReceivingSession> findByWorkerId(UUID id) {
        return repositoryJpa.findByWorkerId(id)
                .map(mapper::toDomain);
    }

    @Override
    public Optional<WorkerReceivingSession> findByWorkerIdWithLock(UUID id) {
        return repositoryJpa.findWithLockByWorkerId(id)
                .map(mapper::toDomain);
    }

}