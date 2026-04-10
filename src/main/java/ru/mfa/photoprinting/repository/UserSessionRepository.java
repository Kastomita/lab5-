package ru.mfa.photoprinting.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import ru.mfa.photoprinting.model.UserSession;
import ru.mfa.photoprinting.enums.SessionStatus;
import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface UserSessionRepository extends JpaRepository<UserSession, Long> {

    Optional<UserSession> findByRefreshToken(String refreshToken);

    @Modifying
    @Transactional
    @Query("UPDATE UserSession s SET s.status = :status, s.revokedAt = CURRENT_TIMESTAMP WHERE s.userId = :userId AND s.status = 'ACTIVE'")
    void revokeAllUserSessions(@Param("userId") Long userId, @Param("status") SessionStatus status);

    @Modifying
    @Transactional
    @Query("UPDATE UserSession s SET s.status = 'EXPIRED' WHERE s.expiresAt < :now AND s.status = 'ACTIVE'")
    void expireOldSessions(@Param("now") LocalDateTime now);
}