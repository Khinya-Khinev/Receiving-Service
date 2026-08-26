package com.waregang.receiving_service.advanced_shipping_notice.infrastructure.jpa_repositories;

import com.waregang.receiving_service.advanced_shipping_notice.domain.model.AdvancedShippingNoticeJpa;
import com.waregang.receiving_service.advanced_shipping_notice.domain.model.AdvancedShippingNoticeStatus;
import com.waregang.receiving_service.common.SkuQuantityDto;
import org.jspecify.annotations.NonNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AdvancedShippingNoticeRepositoryJpa extends
        JpaRepository<AdvancedShippingNoticeJpa, UUID>,
        JpaSpecificationExecutor<AdvancedShippingNoticeJpa>
{
    Optional<AdvancedShippingNoticeJpa> findByAsnNumber(String asn);

    @Query("""
            SELECT
                new com.waregang.receiving_service.common.SkuQuantityDto(c.sku, SUM(c.quantity))
            FROM 
                ContentJpa c
            WHERE 
                c.containerUnit.asn.id = :deliveryId
            GROUP BY 
                c.sku
            """)
    List<SkuQuantityDto> findExpectedSkuQuantities(@Param("deliveryId") UUID deliveryId);

    @Override
    Page<AdvancedShippingNoticeJpa> findAll(@NonNull Pageable pageable);

    boolean existsByIdAndStatus(UUID asnId, AdvancedShippingNoticeStatus status);

    @Query("""
        SELECT 
            d.status
        FROM 
            AdvancedShippingNoticeJpa d
        WHERE   
            d.id = :deliveryId
""")
    Optional<AdvancedShippingNoticeStatus> findDeliveryStatusById(@Param("deliveryId") UUID deliveryId);

    @EntityGraph(attributePaths = {
            "handlingUnits",
            "handlingUnits.contents"
    })
    @Query("""
        SELECT 
            d 
        FROM 
            AdvancedShippingNoticeJpa d 
        """)
    List<AdvancedShippingNoticeJpa> findAllWithHandlingUnits();

    @EntityGraph(attributePaths = {
            "handlingUnits",
            "handlingUnits.contents"
    })
    @Query("""
        SELECT 
            d 
        FROM 
            AdvancedShippingNoticeJpa d 
        WHERE 
            d.id = :deliveryId
        """)
    Optional<AdvancedShippingNoticeJpa> findByIdWithHandlingUnits(@Param("deliveryId") UUID deliveryId);
}