package org.example.service;

import org.example.port.AuditRepository;
import org.springframework.stereotype.Service;

@Service
public class AuditService {
    private final AuditRepository auditRepository;

    public AuditService(AuditRepository repo) {
        this.auditRepository = repo;
    }

    public void record(Long id, String action, String details) {
        auditRepository.append(id, action, details);
    }

}