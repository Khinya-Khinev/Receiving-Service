package com.waregang.receiving_service.advanced_shipping_notice.infrastructure.jpa_repositories;

import com.waregang.receiving_service.advanced_shipping_notice.domain.model.HandlingUnitJpa;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface HandlingUnitRepositoryJpa extends JpaRepository<HandlingUnitJpa, UUID> {
    boolean existsByLpnAndAsn_Id(String lpn, UUID asnId);

    List<HandlingUnitJpa> findByParentUnitId(UUID parentUnitId);
}