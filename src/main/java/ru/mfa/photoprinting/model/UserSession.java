package ru.mfa.photoprinting.model;

import jakarta.persistence.*;
import ru.mfa.photoprinting.enums.SessionStatus;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "user_sessions")
public class UserSession {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "user_email", nullable = false)
    private String userEmail;

    @Column(name = "device_id")
    private String deviceId;

    @Column(name = "refresh_token", length = 512, unique = true)
    private String refreshToken;

    @Column(name = "refresh_token_expiry")
    private Instant refreshTokenExpiry;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SessionStatus status = SessionStatus.ACTIVE;

    @Column(name = "created_at")
    private Instant createdAt;

    @Column(name = "last_access_token", length = 512)
    private String lastAccessToken;

    public UserSession() {}

    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
    }

    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getUserEmail() { return userEmail; }
    public void setUserEmail(String userEmail) { this.userEmail = userEmail; }

    public String getDeviceId() { return deviceId; }
    public void setDeviceId(String deviceId) { this.deviceId = deviceId; }

    public String getRefreshToken() { return refreshToken; }
    public void setRefreshToken(String refreshToken) { this.refreshToken = refreshToken; }

    public Instant getRefreshTokenExpiry() { return refreshTokenExpiry; }
    public void setRefreshTokenExpiry(Instant refreshTokenExpiry) { this.refreshTokenExpiry = refreshTokenExpiry; }

    public SessionStatus getStatus() { return status; }
    public void setStatus(SessionStatus status) { this.status = status; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public String getLastAccessToken() { return lastAccessToken; }
    public void setLastAccessToken(String lastAccessToken) { this.lastAccessToken = lastAccessToken; }
}