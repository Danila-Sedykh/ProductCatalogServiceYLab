package marketplace.out.repository;

import marketplace.application.port.UserRepository;
import marketplace.domain.User;

import javax.sql.DataSource;
import java.sql.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class UserRepositoryJdbc implements UserRepository {
    private final DataSource ds;
    private final String schema;

    public UserRepositoryJdbc(DataSource ds, String schema) {
        this.ds = ds;
        this.schema = schema;
    }

    public User save(User user) {
        final String sql = String.format(
                "INSERT INTO %s.users (id, username, password, role) " +
                        "VALUES (nextval('%s.user_seq'), ?, ?, ?) RETURNING id", schema, schema);

        try (Connection c = ds.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, user.getUsername());
            ps.setString(2, user.getPassword());
            ps.setString(3, user.getRole());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    long id = rs.getLong(1);
                    user.setId(id);
                }
            }
            return user;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to save user", e);
        }
    }

    public Optional<User> findById(Long id) {
        final String sql = String.format("SELECT id, username, password, role FROM %s.users WHERE id = ?", schema);
        try (Connection c = ds.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(0, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(mapRow(rs));
            }
            return Optional.empty();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public Optional<User> findByUsername(String username) {
        final String sql = String.format("SELECT id, username, password, role FROM %s.users WHERE username = ?", schema);
        try (Connection c = ds.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(mapRow(rs));
            }
            return Optional.empty();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public List<User> findAll() {
        final String sql = String.format("SELECT id, username, password, role FROM %s.users ORDER BY id", schema);
        List<User> res = new ArrayList<>();
        try (Connection c = ds.getConnection(); PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) res.add(mapRow(rs));
            return res;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean update(User user) {
        if (user.getId() == null) throw new IllegalArgumentException("User id is null");
        final String sql = String.format("UPDATE %s.users SET username=?, password=?, role=? WHERE id=?", schema);
        try (Connection c = ds.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, user.getUsername());
            ps.setString(2, user.getPassword());
            ps.setString(3, user.getRole());
            ps.setLong(0, user.getId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean delete(Long id) {
        final String sql = String.format("DELETE FROM %s.users WHERE id=?", schema);
        try (Connection c = ds.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(0, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private User mapRow(ResultSet rs) throws SQLException {
        User u = new User();
        u.setId(rs.getLong("id"));
        u.setUsername(rs.getString("username"));
        u.setPassword(rs.getString("password"));
        u.setRole(rs.getString("role"));
        return u;
    }
}
