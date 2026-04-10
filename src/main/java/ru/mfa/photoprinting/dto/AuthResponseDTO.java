package ru.mfa.photoprinting.dto;

import java.util.Set;

public class AuthResponseDTO {
    private Long id;
    private String username;
    private String email;
    private String fullName;
    private Set<String> roles;
    private String accessToken;
    private String refreshToken;
    private Long expiresIn;
    private String message;

    public AuthResponseDTO() {}

    public AuthResponseDTO(Long id, String username, String email, String fullName, Set<String> roles,
                           String accessToken, String refreshToken, Long expiresIn, String message) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.fullName = fullName;
        this.roles = roles;
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.expiresIn = expiresIn;
        this.message = message;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public Set<String> getRoles() { return roles; }
    public void setRoles(Set<String> roles) { this.roles = roles; }

    public String getAccessToken() { return accessToken; }
    public void setAccessToken(String accessToken) { this.accessToken = accessToken; }

    public String getRefreshToken() { return refreshToken; }
    public void setRefreshToken(String refreshToken) { this.refreshToken = refreshToken; }

    public Long getExpiresIn() { return expiresIn; }
    public void setExpiresIn(Long expiresIn) { this.expiresIn = expiresIn; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}