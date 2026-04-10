package ru.mfa.photoprinting.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.mfa.photoprinting.enums.SessionStatus;
import ru.mfa.photoprinting.model.User;
import ru.mfa.photoprinting.model.UserSession;
import ru.mfa.photoprinting.repository.UserRepository;
import ru.mfa.photoprinting.repository.UserSessionRepository;
import ru.mfa.photoprinting.security.JwtService;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class TokenRefreshService {

    private static final Logger log = LoggerFactory.getLogger(TokenRefreshService.class);

    private final JwtService jwtService;
    private final UserSessionRepository sessionRepository;
    private final UserRepository userRepository;

    public TokenRefreshService(JwtService jwtService, UserSessionRepository sessionRepository, UserRepository userRepository) {
        this.jwtService = jwtService;
        this.sessionRepository = sessionRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public Map<String, String> createTokenPair(String email, String deviceId) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found: " + email));

        UUID sessionId = UUID.randomUUID();

        Map<String, Object> accessClaims = new HashMap<>();
        accessClaims.put("type", "access");
        accessClaims.put("deviceId", deviceId);
        accessClaims.put("username", user.getUsername());
        accessClaims.put("roles", user.getRoles());

        String accessToken = jwtService.generateAccessToken(email, accessClaims);
        String refreshToken = jwtService.generateRefreshToken(email, sessionId);

        UserSession session = new UserSession();
        session.setUserEmail(email);
        session.setDeviceId(deviceId);
        session.setRefreshToken(refreshToken);
        session.setRefreshTokenExpiry(jwtService.extractExpiration(refreshToken));
        session.setStatus(SessionStatus.ACTIVE);
        session.setLastAccessToken(accessToken);

        sessionRepository.save(session);

        Map<String, String> tokens = new HashMap<>();
        tokens.put("accessToken", accessToken);
        tokens.put("refreshToken", refreshToken);
        return tokens;
    }

    @Transactional
    public Map<String, String> refreshTokens(String refreshToken) {
        if (!jwtService.validateToken(refreshToken)) {
            throw new RuntimeException("Invalid refresh token");
        }

        if (jwtService.isTokenExpired(refreshToken)) {
            throw new RuntimeException("Refresh token expired");
        }

        UserSession session = sessionRepository.findByRefreshTokenAndStatus(refreshToken, SessionStatus.ACTIVE)
                .orElseThrow(() -> {
                    sessionRepository.findByRefreshToken(refreshToken).ifPresent(usedSession -> {
                        log.warn("Replay attack detected for user: {}, revoking all sessions", usedSession.getUserEmail());
                        sessionRepository.revokeAllActiveSessionsForUser(usedSession.getUserEmail(), SessionStatus.REVOKED);
                    });
                    return new RuntimeException("Refresh token not active");
                });

        String userEmail = session.getUserEmail();
        String deviceId = session.getDeviceId();

        session.setStatus(SessionStatus.USED);
        sessionRepository.save(session);

        UUID newSessionId = UUID.randomUUID();
        Map<String, Object> accessClaims = new HashMap<>();
        accessClaims.put("type", "access");
        accessClaims.put("deviceId", deviceId);

        String newAccessToken = jwtService.generateAccessToken(userEmail, accessClaims);
        String newRefreshToken = jwtService.generateRefreshToken(userEmail, newSessionId);

        UserSession newSession = new UserSession();
        newSession.setUserEmail(userEmail);
        newSession.setDeviceId(deviceId);
        newSession.setRefreshToken(newRefreshToken);
        newSession.setRefreshTokenExpiry(jwtService.extractExpiration(newRefreshToken));
        newSession.setStatus(SessionStatus.ACTIVE);
        newSession.setLastAccessToken(newAccessToken);

        sessionRepository.save(newSession);

        Map<String, String> tokens = new HashMap<>();
        tokens.put("accessToken", newAccessToken);
        tokens.put("refreshToken", newRefreshToken);
        return tokens;
    }

    @Transactional
    public void logout(String refreshToken) {
        sessionRepository.findByRefreshTokenAndStatus(refreshToken, SessionStatus.ACTIVE)
                .ifPresent(session -> {
                    session.setStatus(SessionStatus.USED);
                    sessionRepository.save(session);
                });
    }

    @Transactional
    public void logoutAll(String userEmail) {
        sessionRepository.revokeAllActiveSessionsForUser(userEmail, SessionStatus.REVOKED);
    }
}