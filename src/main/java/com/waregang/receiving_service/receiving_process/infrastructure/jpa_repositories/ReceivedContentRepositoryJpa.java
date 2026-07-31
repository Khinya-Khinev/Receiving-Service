package com.waregang.receiving_service.receiving_process.infrastructure.jpa_repositories;

import com.waregang.receiving_service.SkuQuantityDto;
import com.waregang.receiving_service.receiving_process.infrastructure.jpa_entities.ReceivedContentJpa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface ReceivedContentRepositoryJpa extends JpaRepository<ReceivedContentJpa, UUID> {

    @Query("""
        SELECT 
            rc.sku, 
            SUM(rc.quantity) as quantity
        FROM 
            ReceivedContentJpa rc
        JOIN 
            rc.containerUnit rhu
        WHERE 
            rhu.receiptId = :receiptId
        GROUP BY 
            rc.sku
        """)
    List<SkuQuantityDto> findActualSkuQuantitiesByReceiptId(@Param("receiptId") UUID receiptId);
}