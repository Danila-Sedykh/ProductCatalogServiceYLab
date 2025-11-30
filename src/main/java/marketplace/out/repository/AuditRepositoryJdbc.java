package marketplace.out.repository;

import marketplace.application.port.AuditRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;

@Repository
public class AuditRepositoryJdbc implements AuditRepository {
    private final DataSource ds;
    private final String schema;

    public AuditRepositoryJdbc(DataSource ds, @Value("${db.appSchema}") String schema) {
        this.ds = ds;
        this.schema = schema;
    }

    public void append(Long id, String action, String details) {
        final String sql = String.format(
                "INSERT INTO %s.audit (id, user_id, action, description, created_at) " +
                        "VALUES (nextval('%s.audit_seq'), ?, ?, ?, ?)", schema, schema);

        try (Connection c = ds.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, id);
            ps.setString(2, action);
            ps.setString(3, details);
            ps.setTimestamp(4, Timestamp.from(Instant.now()));
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to append audit", e);
        }
    }
}
