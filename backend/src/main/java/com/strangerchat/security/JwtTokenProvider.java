package com.strangerchat.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Date;
import java.util.UUID;

@Component
public class JwtTokenProvider {

    @Value("${app.jwt.secret}")
    private String jwtSecret;

    @Value("${app.jwt.expiration-ms}")
    private long jwtExpirationMs;

    @Value("${app.jwt.guest-expiration-ms}")
    private long guestExpirationMs;

    private SecretKey key;

    @PostConstruct
    public void init() {
        this.key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
    }

    public String generateToken(Authentication authentication) {
        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
        return generateTokenFromPrincipal(principal);
    }

    public String generateTokenFromPrincipal(UserPrincipal principal) {
        Date now = new Date();
        long expiry = principal.isGuest() ? guestExpirationMs : jwtExpirationMs;
        Date expiryDate = new Date(now.getTime() + expiry);

        var builder = Jwts.builder()
                .subject(principal.getPrincipalKey())
                .claim("isGuest", principal.isGuest())
                .claim("username", principal.getUsername())
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(key);

        if (principal.isGuest()) {
            builder.claim("guestId", principal.getGuestId());
            builder.claim("role", "ROLE_GUEST");
        } else {
            builder.claim("userId", principal.getId().toString());
            builder.claim("email", principal.getEmail());
            builder.claim("role", "ROLE_USER");
        }

        return builder.compact();
    }

    public String generateGuestToken(String guestId, String displayName) {
        UserPrincipal guestPrincipal = UserPrincipal.createGuest(guestId, displayName);
        return generateTokenFromPrincipal(guestPrincipal);
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parser().verifyWith(key).build().parseSignedClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException ex) {
            return false;
        }
    }

    public UserPrincipal getPrincipalFromToken(String token) {
        Claims claims = Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();

        boolean isGuest = claims.get("isGuest", Boolean.class) != null && claims.get("isGuest", Boolean.class);
        String username = claims.get("username", String.class);
        String role = claims.get("role", String.class);
        if (role == null) {
            role = isGuest ? "ROLE_GUEST" : "ROLE_USER";
        }

        if (isGuest) {
            String guestId = claims.get("guestId", String.class);
            return UserPrincipal.builder()
                    .id(null)
                    .guestId(guestId)
                    .username(username)
                    .email(null)
                    .password(null)
                    .isGuest(true)
                    .authorities(Collections.singletonList(new SimpleGrantedAuthority(role)))
                    .build();
        } else {
            String userIdStr = claims.get("userId", String.class);
            UUID userId = UUID.fromString(userIdStr);
            String email = claims.get("email", String.class);

            return UserPrincipal.builder()
                    .id(userId)
                    .guestId(null)
                    .username(username)
                    .email(email)
                    .password(null)
                    .isGuest(false)
                    .authorities(Collections.singletonList(new SimpleGrantedAuthority(role)))
                    .build();
        }
    }
}
