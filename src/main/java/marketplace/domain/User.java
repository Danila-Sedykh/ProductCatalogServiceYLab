package marketplace.domain;

public class User {
    private static int counter = 0;

    private final Long id;
    private final String username;
    private final String password;
    private final String role;

    public User(String username, String password, String role) {
        this.id = Long.valueOf(++counter);
        this.username = username;
        this.password = password;
        this.role = role;
    }

    public Long getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getRole() {
        return role;
    }
}
