package ru.mfa.photoprinting.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;
import ru.mfa.photoprinting.model.User;

import java.security.Key;
import java.util.Date;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class JwtTokenProvider {

    private static final Logger log = LoggerFactory.getLogger(JwtTokenProvider.class);

    private final UserDetailsService userDetailsService;

    @Value("${jwt.access.secret}")
    private String accessSecret;

    @Value("${jwt.refresh.secret}")
    private String refreshSecret;

    @Value("${jwt.access.expiration}")
    private long accessExpiration;

    @Value("${jwt.refresh.expiration}")
    private long refreshExpiration;

    public JwtTokenProvider(UserDetailsService userDetailsService) {
        this.userDetailsService = userDetailsService;
    }

    private Key getAccessSigningKey() {
        return Keys.hmacShaKeyFor(accessSecret.getBytes());
    }

    private Key getRefreshSigningKey() {
        return Keys.hmacShaKeyFor(refreshSecret.getBytes());
    }

    public String generateAccessToken(User user) {
        Claims claims = Jwts.claims()
                .setSubject(user.getId().toString())
                .setIssuer("photo-printing-service");

        claims.put("email", user.getEmail());
        claims.put("username", user.getUsername());
        claims.put("studentId", user.getStudentId());
        claims.put("fullName", user.getFullName());
        claims.put("authorities", user.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toSet()));
        claims.put("tokenType", "ACCESS");

        Date now = new Date();
        Date expiration = new Date(now.getTime() + accessExpiration);

        return Jwts.builder()
                .setClaims(claims)
                .setId(UUID.randomUUID().toString())
                .setIssuedAt(now)
                .setExpiration(expiration)
                .signWith(getAccessSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public String generateRefreshToken(User user, String sessionId) {
        Claims claims = Jwts.claims()
                .setSubject(user.getId().toString());

        claims.put("email", user.getEmail());
        claims.put("sessionId", sessionId);
        claims.put("tokenType", "REFRESH");

        Date now = new Date();
        Date expiration = new Date(now.getTime() + refreshExpiration);

        return Jwts.builder()
                .setClaims(claims)
                .setId(UUID.randomUUID().toString())
                .setIssuedAt(now)
                .setExpiration(expiration)
                .signWith(getRefreshSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public boolean validateAccessToken(String token) {
        return validateToken(token, getAccessSigningKey(), "ACCESS");
    }

    public boolean validateRefreshToken(String token) {
        return validateToken(token, getRefreshSigningKey(), "REFRESH");
    }

    private boolean validateToken(String token, Key secret, String expectedType) {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(secret)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

            String tokenType = claims.get("tokenType", String.class);
            return expectedType.equals(tokenType) && claims.getExpiration().after(new Date());
        } catch (JwtException | IllegalArgumentException e) {
            log.error("JWT validation error: {}", e.getMessage());
            return false;
        }
    }

    public Long getUserIdFromAccessToken(String token) {
        return Long.parseLong(getClaimsFromToken(token, getAccessSigningKey()).getSubject());
    }

    public Long getUserIdFromRefreshToken(String token) {
        return getClaimsFromToken(token, getRefreshSigningKey()).get("userId", Long.class);
    }
    public String getEmailFromAccessToken(String token) {
        return getClaimsFromToken(token, getAccessSigningKey()).get("email", String.class);
    }

    public String getJtiFromToken(String token) {
        return getClaimsFromToken(token, getAccessSigningKey()).getId();
    }
    public String getSessionIdFromRefreshToken(String token) {
        return getClaimsFromToken(token, getRefreshSigningKey()).get("sessionId", String.class);
    }

    private Claims getClaimsFromToken(String token, Key secret) {
        return Jwts.parserBuilder()
                .setSigningKey(secret)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public long getAccessExpirationMs() {
        return accessExpiration;
    }
}