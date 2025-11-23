package marketplace.application.port;

public interface AuditRepository {
    void append(Long id, String action, String details);

}
