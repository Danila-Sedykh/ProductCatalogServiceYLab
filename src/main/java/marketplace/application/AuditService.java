package marketplace.application;

import marketplace.domain.User;
import marketplace.out.file.FileAuditRepository;

import java.time.Instant;

/**
 * Сервис аудита, сохраняет активность пользователя в репозиторий аудита чперез FileAuditRepository.
 */

public class AuditService {
    private final FileAuditRepository repo;

    public AuditService(FileAuditRepository repo) {
        this.repo = repo;
    }

    public void record(User user, String action, String details) {
        String entry = String.format("%s | user=%s | action=%s | details=%s",
                Instant.now(), user == null ? "anonymous" : user.getUsername(), action, details);
        repo.append(entry);
    }

}
