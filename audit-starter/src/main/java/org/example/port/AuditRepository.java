package org.example.port;

public interface AuditRepository {
    void append(Long userId, String action, String details);
}