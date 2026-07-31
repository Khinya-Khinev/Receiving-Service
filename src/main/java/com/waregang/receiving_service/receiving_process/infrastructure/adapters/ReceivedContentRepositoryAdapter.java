package com.waregang.receiving_service.receiving_process.infrastructure.adapters;

import com.waregang.receiving_service.common.exception_handling.DatabaseExceptionTranslator;
import com.waregang.receiving_service.SkuQuantityDto;
import com.waregang.receiving_service.receiving_process.domain.model.ReceivedContent;
import com.waregang.receiving_service.receiving_process.application.ports.ReceivedContentRepositoryPort;
import com.waregang.receiving_service.receiving_process.infrastructure.jpa_entities.ReceivedContentJpa;
import com.waregang.receiving_service.receiving_process.infrastructure.jpa_repositories.ReceivedContentRepositoryJpa;
import com.waregang.receiving_service.receiving_process.infrastructure.mappers.ReceivedContentMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class ReceivedContentRepositoryAdapter implements ReceivedContentRepositoryPort {

    private final ReceivedContentRepositoryJpa jpaRepository;
    private final ReceivedContentMapper mapper;
    private final DatabaseExceptionTranslator databaseExceptionTranslator;

    @Override
    public ReceivedContent save(ReceivedContent content) {
        try {
            ReceivedContentJpa jpaEntity = mapper.toJpa(content);
            ReceivedContentJpa savedEntity = jpaRepository.saveAndFlush(jpaEntity);
            return mapper.toDomain(savedEntity);
        } catch (DataIntegrityViolationException e) {
            throw databaseExceptionTranslator.translate(e);
        }
    }

    @Override
    public List<ReceivedContent> findAll() {
        return jpaRepository.findAll().stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<SkuQuantityDto> findActualSkuQuantitiesByReceiptId(UUID receiptId) {
        return jpaRepository.findActualSkuQuantitiesByReceiptId(receiptId);
    }
}