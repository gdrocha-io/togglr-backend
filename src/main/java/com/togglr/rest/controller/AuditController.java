package com.togglr.rest.controller;

import com.togglr.audit.entity.AuditLog;
import com.togglr.audit.enums.AuditAction;
import com.togglr.audit.enums.DataSource;
import com.togglr.audit.enums.EntityType;
import com.togglr.audit.repository.AuditLogRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/v1/audit")
@Tag(name = "Audit", description = "Audit log management and tracking")
@RequiredArgsConstructor
@Slf4j
public class AuditController {
    private final AuditLogRepository auditLogRepository;

    @GetMapping
    @Operation(summary = "Get audit logs", description = "Retrieve paginated audit logs for all operations")
    @PreAuthorize("hasRole('ADMIN') or hasRole('ROOT')")
    public Page<AuditLog> getAuditLogs(
            @Parameter(description = "Pagination parameters") Pageable pageable) {
        return auditLogRepository.findAll(pageable);
    }

    @GetMapping("/feature/{featureId}")
    @Operation(summary = "Get feature audit logs", description = "Retrieve audit logs for specific feature with optional filters")
    @PreAuthorize("hasRole('ADMIN') or hasRole('ROOT')")
    public Page<AuditLog> getFeatureAuditLogs(
            @Parameter(description = "Feature ID", example = "1") @PathVariable Long featureId,
            @Parameter(description = "Action types") @RequestParam(required = false) List<String> action,
            @Parameter(description = "User type") @RequestParam(name = "user_type", required = false) String userType,
            @Parameter(description = "Username") @RequestParam(required = false) String username,
            @Parameter(description = "Data source") @RequestParam(name = "data_source", required = false) String dataSource,
            @Parameter(description = "Pagination parameters") Pageable pageable) {

        List<AuditAction> actions = (action != null && !action.isEmpty()) ?
                action.stream().map(AuditAction::valueOf).toList() : null;

        DataSource dataSourceEnum = (dataSource != null) ? DataSource.valueOf(dataSource.toUpperCase()) : null;

        Specification<AuditLog> spec = Specification.where(null);
        
        spec = spec.and((root, query, cb) -> cb.equal(root.get("entityType"), EntityType.FEATURE));
        spec = spec.and((root, query, cb) -> cb.equal(root.get("entityId"), featureId));
        
        if (actions != null && !actions.isEmpty()) {
            spec = spec.and((root, query, cb) -> root.get("action").in(actions));
        }
        if (userType != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("userType"), userType));
        }
        if (username != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("username"), username));
        }
        if (dataSourceEnum != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("dataSource"), dataSourceEnum));
        }
        
        return auditLogRepository.findAll(spec, pageable);
    }

    @GetMapping("/entity")
    @Operation(summary = "Get audit logs by entity type and ID", description = "Generic endpoint for any entity audit logs")
    @PreAuthorize("hasRole('ADMIN') or hasRole('ROOT')")
    public Page<AuditLog> getEntityAuditLogs(
            @Parameter(description = "Entity type", example = "FEATURE") @RequestParam EntityType entityType,
            @Parameter(description = "Entity ID", example = "1") @RequestParam Long entityId,
            @Parameter(description = "Pagination parameters") Pageable pageable) {
        return auditLogRepository.findByEntityTypeAndEntityIdOrderByCreatedAtDesc(
                entityType, entityId, pageable);
    }
}