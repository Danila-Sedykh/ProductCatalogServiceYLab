package marketplace.dto;

public class AuthResponse {
    private String status;
    private String role;

    public AuthResponse(String status, String role) {
        this.status = status;
        this.role = role;
    }

    public String getStatus() {
        return status;
    }

    public String getRole() {
        return role;
    }
}
