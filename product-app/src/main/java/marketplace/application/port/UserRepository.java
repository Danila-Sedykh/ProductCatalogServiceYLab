package marketplace.application.port;

import marketplace.domain.User;

import java.sql.ResultSet;
import java.util.List;
import java.util.Optional;

public interface UserRepository {
    User save(User user);
    Optional<User> findById(Long id);
    Optional<User> findByUsername(String username);
    List<User> findAll();
    boolean update(User user);
    boolean delete(Long id);
}
