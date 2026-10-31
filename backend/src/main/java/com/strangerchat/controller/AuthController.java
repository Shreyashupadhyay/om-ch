package com.strangerchat.controller;

import com.strangerchat.dto.*;
import com.strangerchat.exception.RateLimitExceededException;
import com.strangerchat.security.RateLimiterService;
import com.strangerchat.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication", description = "Endpoints for login, registration, and guest identity")
public class AuthController {

    private final AuthService authService;
    private final RateLimiterService rateLimiterService;

    public AuthController(AuthService authService, RateLimiterService rateLimiterService) {
        this.authService = authService;
        this.rateLimiterService = rateLimiterService;
    }

    @PostMapping("/register")
    @Operation(summary = "Register a new permanent user account")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request,
                                                 HttpServletRequest httpRequest) {
        checkRateLimit(httpRequest);
        AuthResponse response = authService.register(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PostMapping("/login")
    @Operation(summary = "Login to existing user account")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request,
                                              HttpServletRequest httpRequest) {
        checkRateLimit(httpRequest);
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/guest")
    @Operation(summary = "Generate temporary guest identity and token")
    public ResponseEntity<GuestAuthResponse> guest(HttpServletRequest httpRequest) {
        checkRateLimit(httpRequest);
        GuestAuthResponse response = authService.createGuestSession();
        return ResponseEntity.ok(response);
    }

    private void checkRateLimit(HttpServletRequest request) {
        String ip = getClientIp(request);
        if (!rateLimiterService.tryConsumeAuth(ip)) {
            throw new RateLimitExceededException("Too many requests from your IP. Please try again in a few moments.");
        }
    }

    private String getClientIp(HttpServletRequest request) {
        String xf = request.getHeader("X-Forwarded-For");
        if (xf != null && !xf.isBlank()) {
            return xf.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
