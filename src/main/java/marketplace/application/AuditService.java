package marketplace.application;

import marketplace.application.port.AuditRepository;
import org.springframework.stereotype.Service;


/**
 * Сервис аудита, сохраняет активность пользователя в репозиторий аудита чперез FileAuditRepository.
 */
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
