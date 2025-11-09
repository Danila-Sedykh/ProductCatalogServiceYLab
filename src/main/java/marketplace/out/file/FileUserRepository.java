package marketplace.out.file;

import marketplace.domain.Product;
import marketplace.domain.User;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

/**
 * Репозиторий пользователей, хранит список в `users.db`.
 */

public class FileUserRepository {
    private final Path file;
    private final Map<String, User> usersStorage = new LinkedHashMap<>();

    public FileUserRepository(Path file) {
        this.file = file;
        load();
    }

    private void load() {
        if (!Files.exists(file)) return;
        try (BufferedReader reader = Files.newBufferedReader(file)) {
            reader.lines().forEach(line -> {
                String[] parts = line.split(";");
                if (parts.length < 4) return;
                String username = parts[1];
                String password = parts[2];
                String role = parts[3];
                usersStorage.put(username, new User(username, password, role));
            });
        } catch (IOException e) {
            throw new RuntimeException("Failed to load users: " + e.getMessage(), e);
        }
    }

    private void persist() {
        try (BufferedWriter writer = Files.newBufferedWriter(file)) {
            for (User u : usersStorage.values()) {
                writer.write(String.format("%s;%s;%s;%s%n",u.getId(), u.getUsername(), u.getPassword(), u.getRole()));
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to save users: " + e.getMessage(), e);
        }
    }

    public Optional<User> findByUsername(String username) {
        return Optional.ofNullable(usersStorage.get(username));
    }

    public void save(User user) {
        usersStorage.put(user.getUsername(), user);
        persist();
    }

    public List<User> findAll() {
        return new ArrayList<>(usersStorage.values());
    }
}
