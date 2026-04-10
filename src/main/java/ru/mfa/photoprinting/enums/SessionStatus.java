package ru.mfa.photoprinting.enums;

public enum SessionStatus {
    ACTIVE,      // Активная сессия
    REFRESHED,   // Обновлена (старый refresh токен)
    EXPIRED,     // Истекла по времени
    REVOKED      // Отозвана администратором
}