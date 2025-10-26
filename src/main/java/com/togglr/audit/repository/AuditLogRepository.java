package com.togglr.audit.repository;

import com.togglr.audit.entity.AuditLog;
import com.togglr.audit.enums.AuditAction;
import com.togglr.audit.enums.DataSource;
import com.togglr.audit.enums.EntityType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long>, JpaSpecificationExecutor<AuditLog> {
    Page<AuditLog> findByEntityTypeAndEntityIdOrderByCreatedAtDesc(
            EntityType entityType, Long entityId, Pageable pageable);
}