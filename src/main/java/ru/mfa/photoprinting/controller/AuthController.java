package ru.mfa.photoprinting.controller;

import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import ru.mfa.photoprinting.dto.*;
import ru.mfa.photoprinting.model.User;
import ru.mfa.photoprinting.security.JwtService;
import ru.mfa.photoprinting.service.TokenRefreshService;
import ru.mfa.photoprinting.service.UserService;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private static final Logger log = LoggerFactory.getLogger(AuthController.class);

    private final UserService userService;
    private final AuthenticationManager authenticationManager;
    private final TokenRefreshService tokenRefreshService;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;

    public AuthController(UserService userService, AuthenticationManager authenticationManager,
                          TokenRefreshService tokenRefreshService, JwtService jwtService,
                          PasswordEncoder passwordEncoder) {
        this.userService = userService;
        this.authenticationManager = authenticationManager;
        this.tokenRefreshService = tokenRefreshService;
        this.jwtService = jwtService;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@Valid @RequestBody RegistrationRequestDTO request) {
        try {
            if (userService.existsByUsername(request.getUsername())) {
                return ResponseEntity.badRequest()
                        .body(new AuthResponseDTO(null, null, null, null, null, null, null, null,
                                "Username already exists: " + request.getUsername()));
            }

            if (userService.existsByEmail(request.getEmail())) {
                return ResponseEntity.badRequest()
                        .body(new AuthResponseDTO(null, null, null, null, null, null, null, null,
                                "Email already exists: " + request.getEmail()));
            }

            Set<String> roles = new HashSet<>();
            roles.add("USER");

            User user = new User(
                    request.getUsername(),
                    request.getEmail(),
                    passwordEncoder.encode(request.getPassword()),
                    request.getFullName(),
                    roles
            );

            User savedUser = userService.saveUser(user);

            Map<String, String> tokens = tokenRefreshService.createTokenPair(savedUser.getEmail(), "web-registration");

            AuthResponseDTO response = new AuthResponseDTO(
                    savedUser.getId(), savedUser.getUsername(), savedUser.getEmail(), savedUser.getFullName(),
                    savedUser.getRoles(), tokens.get("accessToken"), tokens.get("refreshToken"),
                    jwtService.getAccessExpirationSeconds(), "User registered successfully"
            );

            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (RuntimeException e) {
            log.error("Registration error: ", e);
            return ResponseEntity.badRequest()
                    .body(new AuthResponseDTO(null, null, null, null, null, null, null, null, e.getMessage()));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody AuthRequestDTO request) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
            );

            SecurityContextHolder.getContext().setAuthentication(authentication);

            Map<String, String> tokens = tokenRefreshService.createTokenPair(
                    request.getEmail(),
                    request.getDeviceId() != null ? request.getDeviceId() : "web-client"
            );

            User user = userService.findByEmail(request.getEmail()).orElse(null);

            AuthResponseDTO response = new AuthResponseDTO(
                    user != null ? user.getId() : null,
                    user != null ? user.getUsername() : null,
                    request.getEmail(),
                    user != null ? user.getFullName() : null,
                    user != null ? user.getRoles() : null,
                    tokens.get("accessToken"),
                    tokens.get("refreshToken"),
                    jwtService.getAccessExpirationSeconds(),
                    "Login successful"
            );

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Login error: ", e);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new AuthResponseDTO(null, null, null, null, null, null, null, null, "Invalid credentials"));
        }
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(@Valid @RequestBody RefreshRequestDTO request) {
        try {
            Map<String, String> tokens = tokenRefreshService.refreshTokens(request.getRefreshToken());

            AuthResponseDTO response = new AuthResponseDTO(
                    null, null, null, null, null,
                    tokens.get("accessToken"), tokens.get("refreshToken"),
                    jwtService.getAccessExpirationSeconds(), "Token refreshed successfully"
            );

            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {
            log.error("Refresh error: ", e);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new AuthResponseDTO(null, null, null, null, null, null, null, null, e.getMessage()));
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(@RequestBody(required = false) RefreshRequestDTO request) {
        if (request != null && request.getRefreshToken() != null) {
            tokenRefreshService.logout(request.getRefreshToken());
        }
        return ResponseEntity.ok(new AuthResponseDTO(null, null, null, null, null, null, null, null, "Logout successful"));
    }

    @PostMapping("/logout-all")
    public ResponseEntity<?> logoutAll(Authentication authentication) {
        if (authentication != null) {
            String email = authentication.getName();
            tokenRefreshService.logoutAll(email);
        }
        return ResponseEntity.ok(new AuthResponseDTO(null, null, null, null, null, null, null, null, "All sessions logged out"));
    }

    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new AuthResponseDTO(null, null, null, null, null, null, null, null, "Not authenticated"));
        }

        String email = authentication.getName();
        User user = userService.findByEmail(email).orElse(null);

        if (user == null) {
            return ResponseEntity.notFound().build();
        }

        AuthResponseDTO response = new AuthResponseDTO(
                user.getId(), user.getUsername(), user.getEmail(), user.getFullName(), user.getRoles(),
                null, null, null, "Current user info"
        );

        return ResponseEntity.ok(response);
    }
}