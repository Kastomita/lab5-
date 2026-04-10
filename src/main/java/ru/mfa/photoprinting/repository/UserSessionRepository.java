package ru.mfa.photoprinting.repository;

import ru.mfa.photoprinting.model.UserSession;
import ru.mfa.photoprinting.enums.SessionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;
import java.util.Optional;
import java.util.UUID;

public interface UserSessionRepository extends JpaRepository<UserSession, UUID> {

    Optional<UserSession> findByRefreshTokenAndStatus(String refreshToken, SessionStatus status);

    Optional<UserSession> findByRefreshToken(String refreshToken);

    @Modifying
    @Transactional
    @Query("UPDATE UserSession s SET s.status = :status WHERE s.refreshToken = :refreshToken")
    void updateStatusByRefreshToken(String refreshToken, SessionStatus status);

    @Modifying
    @Transactional
    @Query("UPDATE UserSession s SET s.status = :status WHERE s.userEmail = :email AND s.status = 'ACTIVE'")
    void revokeAllActiveSessionsForUser(String email, SessionStatus status);
}