package com.ptit.training.reporting.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ptit.training.auth.entity.User;
import com.ptit.training.auth.repository.UserRepository;
import com.ptit.training.reporting.entity.AuditLog;
import com.ptit.training.reporting.repository.AuditLogRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;

    public AuditLogService(AuditLogRepository auditLogRepository,
                           UserRepository userRepository,
                           ObjectMapper objectMapper) {
        this.auditLogRepository = auditLogRepository;
        this.userRepository = userRepository;
        this.objectMapper = objectMapper;
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public void record(Long actorUserId, String action, String entityType, String entityId,
                       Object oldVal, Object newVal, String reason) {
        User actor = userRepository.getReferenceById(actorUserId);
        AuditLog log = new AuditLog();
        log.setActorUser(actor);
        log.setAction(action);
        log.setEntityType(entityType);
        log.setEntityId(entityId);
        log.setOldValueJson(toJson(oldVal));
        log.setNewValueJson(toJson(newVal));
        log.setReason(reason);
        auditLogRepository.save(log);
    }

    private String toJson(Object value) {
        if (value == null) return null;
        if (value instanceof String s) return s;
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            return "{\"raw\":\"" + value.toString().replace("\"", "'") + "\"}";
        }
    }
}
