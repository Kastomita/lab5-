package ru.mfa.photoprinting.controller;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import ru.mfa.photoprinting.dto.*;
import ru.mfa.photoprinting.model.User;
import ru.mfa.photoprinting.model.UserSession;
import ru.mfa.photoprinting.enums.ApplicationUserRole;
import ru.mfa.photoprinting.enums.SessionStatus;
import ru.mfa.photoprinting.repository.UserRepository;
import ru.mfa.photoprinting.repository.UserSessionRepository;
import ru.mfa.photoprinting.security.JwtTokenProvider;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserRepository userRepository;
    private final UserSessionRepository userSessionRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;

    public AuthController(UserRepository userRepository,
                          UserSessionRepository userSessionRepository,
                          PasswordEncoder passwordEncoder,
                          AuthenticationManager authenticationManager,
                          JwtTokenProvider jwtTokenProvider) {
        this.userRepository = userRepository;
        this.userSessionRepository = userSessionRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@Valid @RequestBody RegistrationRequestDTO request) {
        try {
            if (userRepository.existsByEmail(request.getEmail())) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Email already exists"));
            }

            User user = new User();
            user.setUsername(request.getUsername());
            user.setEmail(request.getEmail());
            user.setPassword(passwordEncoder.encode(request.getPassword()));
            user.setFullName(request.getFullName());
            user.setStudentId(request.getStudentId());
            user.setRole(ApplicationUserRole.USER);
            user.setEnabled(true);
            user.setAccountNonLocked(true);

            User savedUser = userRepository.save(user);

            return ResponseEntity.ok(Map.of(
                    "message", "User registered successfully",
                    "email", savedUser.getEmail(),
                    "username", savedUser.getUsername(),
                    "studentId", savedUser.getStudentId(),
                    "role", savedUser.getRole().name()
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Registration failed: " + e.getMessage()));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody AuthRequestDTO request) {
        try {
            Optional<User> userOpt = userRepository.findByEmail(request.getEmail());
            if (userOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "Invalid credentials"));
            }

            try {
                authenticationManager.authenticate(
                        new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
                );
            } catch (BadCredentialsException e) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "Invalid credentials"));
            }

            User user = userOpt.get();

            // 1. Сначала генерируем access token
            String accessToken = jwtTokenProvider.generateAccessToken(user);
            String accessTokenId = jwtTokenProvider.getJtiFromToken(accessToken);

            // 2. Создаем сессию с access_token_id
            UserSession userSession = new UserSession();
            userSession.setUserId(user.getId());
            userSession.setStatus(SessionStatus.ACTIVE);
            userSession.setExpiresAt(LocalDateTime.now().plusDays(30));
            userSession.setAccessTokenId(accessTokenId);

            UserSession savedSession = userSessionRepository.save(userSession);

            // 3. Генерируем refresh token с ID сессии
            String refreshToken = jwtTokenProvider.generateRefreshToken(user, savedSession.getId().toString());

            // 4. Обновляем сессию с refresh token
            savedSession.setRefreshToken(refreshToken);
            userSessionRepository.save(savedSession);

            AuthResponseDTO response = new AuthResponseDTO();
            response.setAccessToken(accessToken);
            response.setRefreshToken(refreshToken);
            response.setExpiresIn(jwtTokenProvider.getAccessExpirationMs());
            response.setTokenType("Bearer");
            response.setEmail(user.getEmail());
            response.setUsername(user.getUsername());
            response.setFullName(user.getFullName());
            response.setRoles(Set.of(user.getRole().name()));

            return ResponseEntity.ok(response);
        } catch (Exception ex) {
            ex.printStackTrace();
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Authentication failed: " + ex.getMessage()));
        }
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refreshToken(@Valid @RequestBody RefreshRequestDTO request) {
        try {
            if (!jwtTokenProvider.validateRefreshToken(request.getRefreshToken())) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "Invalid refresh token"));
            }

            String sessionId = jwtTokenProvider.getSessionIdFromRefreshToken(request.getRefreshToken());
            Long userId = jwtTokenProvider.getUserIdFromRefreshToken(request.getRefreshToken());

            Optional<UserSession> sessionOpt = userSessionRepository.findById(Long.parseLong(sessionId));
            if (sessionOpt.isEmpty() || !sessionOpt.get().getStatus().equals(SessionStatus.ACTIVE)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "Session is not active"));
            }

            UserSession oldSession = sessionOpt.get();
            if (!oldSession.getUserId().equals(userId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("error", "Access denied"));
            }

            Optional<User> userOpt = userRepository.findById(userId);
            if (userOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "User not found"));
            }

            User user = userOpt.get();

            oldSession.setStatus(SessionStatus.REFRESHED);
            oldSession.setRevokedAt(LocalDateTime.now());
            userSessionRepository.save(oldSession);

            UserSession newSession = new UserSession();
            newSession.setUserId(user.getId());
            newSession.setStatus(SessionStatus.ACTIVE);
            newSession.setExpiresAt(LocalDateTime.now().plusDays(30));

            UserSession savedSession = userSessionRepository.save(newSession);

            String newAccessToken = jwtTokenProvider.generateAccessToken(user);
            String newRefreshToken = jwtTokenProvider.generateRefreshToken(user, savedSession.getId().toString());

            newSession.setRefreshToken(newRefreshToken);
            userSessionRepository.save(newSession);

            AuthResponseDTO response = new AuthResponseDTO();
            response.setAccessToken(newAccessToken);
            response.setRefreshToken(newRefreshToken);
            response.setExpiresIn(jwtTokenProvider.getAccessExpirationMs());
            response.setEmail(user.getEmail());
            response.setUsername(user.getUsername());
            response.setFullName(user.getFullName());
            response.setRoles(Set.of(user.getRole().name()));

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Token refresh failed"));
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            if (jwtTokenProvider.validateAccessToken(token)) {
                String email = jwtTokenProvider.getEmailFromAccessToken(token);
                Optional<User> userOpt = userRepository.findByEmail(email);
                if (userOpt.isPresent()) {
                    userSessionRepository.revokeAllUserSessions(userOpt.get().getId(), SessionStatus.REVOKED);
                }
            }
        }
        return ResponseEntity.ok(Map.of("message", "Logout successful"));
    }

    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            if (jwtTokenProvider.validateAccessToken(token)) {
                String email = jwtTokenProvider.getEmailFromAccessToken(token);
                Optional<User> userOpt = userRepository.findByEmail(email);
                if (userOpt.isPresent()) {
                    User user = userOpt.get();
                    return ResponseEntity.ok(Map.of(
                            "id", user.getId(),
                            "email", user.getEmail(),
                            "username", user.getUsername(),
                            "fullName", user.getFullName(),
                            "studentId", user.getStudentId(),
                            "role", user.getRole().name()
                    ));
                }
            }
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("error", "Not authenticated"));
    }
}