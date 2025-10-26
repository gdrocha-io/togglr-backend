package com.togglr.audit.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.togglr.audit.entity.AuditLog;
import com.togglr.audit.enums.AuditAction;
import com.togglr.audit.enums.DataSource;
import com.togglr.audit.enums.EntityType;
import com.togglr.audit.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Iterator;
import java.util.Map;

import static java.util.Arrays.asList;

@Log4j2
@Service
@RequiredArgsConstructor
public class AuditService {
    private final AuditLogRepository auditLogRepository;
    private final ObjectMapper objectMapper;

    public void logAccess(EntityType entityType, Long entityId, String entityName, DataSource dataSource) {
        logAudit(AuditAction.ACCESS, entityType, entityId, entityName, null, null, dataSource);
    }

    public void logCreate(EntityType entityType, Long entityId, String entityName, Object newEntity) {
        logAudit(AuditAction.CREATE, entityType, entityId, entityName, null, newEntity, DataSource.DATABASE);
    }

    public void logUpdate(EntityType entityType, Long entityId, String entityName, Object oldEntity, Object newEntity) {
        try {
            JsonNode oldJson = objectMapper.valueToTree(oldEntity);
            JsonNode newJson = objectMapper.valueToTree(newEntity);

            ObjectNode oldValues = objectMapper.createObjectNode();
            ObjectNode newValues = objectMapper.createObjectNode();

            findChanges(oldJson, newJson, oldValues, newValues);

            if (!oldValues.isEmpty() || !newValues.isEmpty()) {
                logAudit(AuditAction.UPDATE, entityType, entityId, entityName, oldValues, newValues, DataSource.DATABASE);
            }
        } catch (Exception e) {
            logAudit(AuditAction.UPDATE, entityType, entityId, entityName, oldEntity, newEntity, DataSource.DATABASE);
        }
    }

    public void logDelete(EntityType entityType, Long entityId, String entityName, Object oldEntity) {
        logAudit(AuditAction.DELETE, entityType, entityId, entityName, oldEntity, null, DataSource.DATABASE);
    }

    private void logAudit(AuditAction action, EntityType entityType, Long entityId,
                          String entityName, Object oldValues, Object newValues, DataSource dataSource) {
        try {
            AuditLog auditLog = new AuditLog();
            auditLog.setUsername(getCurrentUsername());
            auditLog.setAction(action);
            auditLog.setEntityType(entityType);
            auditLog.setEntityId(entityId);
            auditLog.setEntityName(entityName);
            auditLog.setIpAddress(getCurrentIpAddress());
            auditLog.setTraceId(getCurrentTraceId());
            auditLog.setUserType(getCurrentUserType());
            auditLog.setDataSource(dataSource);

            if (oldValues != null) {
                auditLog.setOldValues(objectMapper.writeValueAsString(oldValues));
            }

            if (newValues != null) {
                auditLog.setNewValues(objectMapper.writeValueAsString(newValues));
            }

            auditLogRepository.save(auditLog);
        } catch (Exception e) {
            log.error("Error logging audit", e);
        }
    }

    private String getCurrentUsername() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null ? auth.getName() : "system";
    }

    private String getCurrentIpAddress() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();

        if (attributes != null) {
            return attributes.getRequest().getRemoteAddr();
        }

        return null;
    }

    private void findChanges(JsonNode oldJson, JsonNode newJson, ObjectNode oldValues, ObjectNode newValues) {
        String[] skipFields = {"updatedAt", "createdAt", "id", "namespace", "environment"};
        Iterator<Map.Entry<String, JsonNode>> fields = newJson.fields();

        while (fields.hasNext()) {
            Map.Entry<String, JsonNode> field = fields.next();
            String fieldName = field.getKey();

            if (asList(skipFields).contains(fieldName)) {
                continue;
            }

            JsonNode newValue = field.getValue();
            JsonNode oldValue = oldJson.get(fieldName);

            if (oldValue == null || !oldValue.equals(newValue)) {
                if (oldValue != null) {
                    oldValues.set(fieldName, oldValue);
                }
                newValues.set(fieldName, newValue);
            }
        }
    }


    private String getCurrentTraceId() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();

        if (attributes != null) {
            String traceId = (String) attributes.getRequest().getAttribute("traceId");

            if (traceId == null) {
                traceId = java.util.UUID.randomUUID().toString();
                attributes.getRequest().setAttribute("traceId", traceId);
            }

            return traceId;
        }

        return java.util.UUID.randomUUID().toString();
    }

    private String getCurrentUserType() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth != null && auth.getAuthorities() != null) {
            boolean hasScope = auth.getAuthorities().stream()
                    .anyMatch(authority -> authority.getAuthority().startsWith("SCOPE_"));
            return hasScope ? "CLIENT" : "USER";
        }

        return "SYSTEM";
    }
}