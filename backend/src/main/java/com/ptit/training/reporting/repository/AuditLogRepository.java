package com.ptit.training.reporting.repository;

import com.ptit.training.reporting.entity.AuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

    @Query("""
            SELECT a FROM AuditLog a
            JOIN FETCH a.actorUser u
            WHERE (:actorUserId IS NULL OR u.id = :actorUserId)
              AND (:action IS NULL OR :action = '' OR a.action = CAST(:action AS string))
              AND (:entityType IS NULL OR :entityType = '' OR a.entityType = CAST(:entityType AS string))
            ORDER BY a.createdAt DESC
            """)
    Page<AuditLog> search(
            @Param("actorUserId") Long actorUserId,
            @Param("action") String action,
            @Param("entityType") String entityType,
            Pageable pageable);
}
