package com.waregang.receiving_service.receiving_process.infrastructure.jpa_repositories;

import com.waregang.receiving_service.receiving_process.infrastructure.jpa_entities.ReceivedUnitJpa;
import org.jspecify.annotations.Nullable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ReceivedUnitRepositoryJpa extends JpaRepository<ReceivedUnitJpa, UUID> {
    Optional<ReceivedUnitJpa> findByLpn(@Nullable String currentUnitLpn);

    @Query(value = """
            SELECT ru 
            FROM ReceivedUnitJpa ru 
            WHERE ru.workerSessionId = :workerSessionId 
            AND ru.parentUnit IS NULL
            """)
    List<ReceivedUnitJpa> findAllByWorkerSessionIdAndParentUnitIsNull(@Param("workerSessionId") UUID workerSessionId);

    List<ReceivedUnitJpa> findAllByReceiptId(UUID receiptId);

    @Query("""
        select
                count(u) 
        from 
                ReceivedUnitJpa u 
        join 
                WorkerReceivingSessionJpa s 
        on
                u.workerSessionId = s.id 
        where 
                s.workerId = :workerId
        and
                s.completedAt BETWEEN :startOfDay AND :endOfDay
""")
    long countUnitsScannedByWorkerId(@Param("workerId") UUID workerId,
                                     @Param("startOfDay") java.time.LocalDateTime startOfDay,
                                     @Param("endOfDay") java.time.LocalDateTime endOfDay);
}
