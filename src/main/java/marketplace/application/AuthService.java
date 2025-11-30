package marketplace.application;

import marketplace.application.port.UserRepository;
import marketplace.aspect.Auditable;
import marketplace.domain.User;
import org.springframework.stereotype.Service;


import java.util.Optional;

/**
 * Авторизация: хранит пользователей в файле через FileUserRepository.
 */

@Service
public class AuthService {
    private final UserRepository userRepo;

    public AuthService(UserRepository userRepo) {
        this.userRepo = userRepo;
    }

    @Auditable(action = "AUTHENTICATE_USER")
    public Optional<User> authenticate(String username, String password) {
        Optional<User> u = userRepo.findByUsername(username);
        if (u.isPresent() && u.get().getPassword().equals(password)) return u;
        return Optional.empty();
    }

    @Auditable(action = "REGISTER_USER")
    public boolean register(String username, String password, String role) {
        if (userRepo.findByUsername(username).isPresent()) return false;
        User u = new User();
        u.setUsername(username);
        u.setPassword(password);
        u.setRole(role);
        userRepo.save(u);
        return true;
    }
}


