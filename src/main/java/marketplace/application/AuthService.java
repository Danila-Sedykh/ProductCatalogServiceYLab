package marketplace.application;

import marketplace.domain.User;
import marketplace.out.repository.UserRepositoryJdbc;


import java.util.Optional;

/**
 * Авторизация: хранит пользователей в файле через FileUserRepository.
 */

public class AuthService {
    private final UserRepositoryJdbc userRepo;

    public AuthService(UserRepositoryJdbc userRepo) {
        this.userRepo = userRepo;
    }

    public Optional<User> authenticate(String username, String password) {
        Optional<User> u = userRepo.findByUsername(username);
        if (u.isPresent() && u.get().getPassword().equals(password)) return u;
        return Optional.empty();
    }

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


