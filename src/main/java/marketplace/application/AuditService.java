package marketplace.application;

import marketplace.domain.User;
import marketplace.out.repository.AuditRepositoryJdbc;

import java.time.Instant;

/**
 * Сервис аудита, сохраняет активность пользователя в репозиторий аудита чперез FileAuditRepository.
 */
public class AuditService {
    private final AuditRepositoryJdbc auditRepository;

    public AuditService(AuditRepositoryJdbc repo) {
        this.auditRepository = repo;
    }

    public void record(User user, String action, String details) {
        auditRepository.append(user.getId(), action, details);
    }

}
