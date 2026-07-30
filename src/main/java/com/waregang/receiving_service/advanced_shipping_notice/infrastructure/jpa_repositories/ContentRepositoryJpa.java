package com.waregang.receiving_service.advanced_shipping_notice.infrastructure.jpa_repositories;

import com.waregang.receiving_service.advanced_shipping_notice.domain.model.ContentJpa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.UUID;

public interface ContentRepositoryJpa extends JpaRepository<ContentJpa, UUID> {
    @Query("""
            SELECT 
                CASE WHEN COUNT(c) > 0 THEN true 
                ELSE false END
            FROM ContentJpa c
            JOIN c.containerUnit hu
            WHERE c.sku = :sku AND hu.asn.id = :asnId
            """)
    boolean existsBySkuAndAsn_Id(@Param("sku") String sku,
                                 @Param("asnId") UUID asnId);
}