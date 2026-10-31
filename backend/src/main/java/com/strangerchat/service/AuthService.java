package com.strangerchat.service;

import com.strangerchat.dto.*;
import com.strangerchat.entity.User;
import com.strangerchat.exception.BadRequestException;
import com.strangerchat.exception.UnauthorizedException;
import com.strangerchat.repository.UserRepository;
import com.strangerchat.security.JwtTokenProvider;
import com.strangerchat.security.UserPrincipal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.UUID;

@Service
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider tokenProvider;
    private final SecureRandom secureRandom = new SecureRandom();

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder,
                       AuthenticationManager authenticationManager, JwtTokenProvider tokenProvider) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.tokenProvider = tokenProvider;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        String trimmedUsername = request.getUsername().trim();
        String trimmedEmail = request.getEmail().trim().toLowerCase();

        if (userRepository.existsByUsernameIgnoreCase(trimmedUsername)) {
            throw new BadRequestException("Username is already taken");
        }

        if (userRepository.existsByEmailIgnoreCase(trimmedEmail)) {
            throw new BadRequestException("Email is already registered");
        }

        User user = User.builder()
                .username(trimmedUsername)
                .email(trimmedEmail)
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .role("ROLE_USER")
                .lastLoginAt(Instant.now())
                .build();

        user = userRepository.save(user);

        UserPrincipal principal = UserPrincipal.create(user);
        String token = tokenProvider.generateTokenFromPrincipal(principal);

        return AuthResponse.builder()
                .token(token)
                .tokenType("Bearer")
                .user(mapToUserDto(user))
                .build();
    }

    @Transactional
    public AuthResponse login(LoginRequest request) {
        String identifier = request.getUsernameOrEmail().trim();

        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(identifier, request.getPassword())
            );

            UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
            User user = userRepository.findById(principal.getId())
                    .orElseThrow(() -> new UnauthorizedException("User not found"));

            user.setLastLoginAt(Instant.now());
            userRepository.save(user);

            String token = tokenProvider.generateToken(authentication);

            return AuthResponse.builder()
                .token(token)
                .tokenType("Bearer")
                .user(mapToUserDto(user))
                .build();
        } catch (Exception ex) {
            log.warn("Login failed for: {}", identifier);
            throw new UnauthorizedException("Invalid username/email or password");
        }
    }

    public GuestAuthResponse createGuestSession() {
        int randomCode = 1000 + secureRandom.nextInt(9000);
        String guestId = UUID.randomUUID().toString();
        String displayName = "Guest-" + randomCode;

        String token = tokenProvider.generateGuestToken(guestId, displayName);

        return GuestAuthResponse.builder()
                .token(token)
                .tokenType("Bearer")
                .guestId(guestId)
                .displayName(displayName)
                .isGuest(true)
                .build();
    }

    @Transactional(readOnly = true)
    public UserDto getCurrentUser(UserPrincipal principal) {
        if (principal.isGuest()) {
            return UserDto.builder()
                    .id(null)
                    .username(principal.getUsername())
                    .email(null)
                    .role("ROLE_GUEST")
                    .createdAt(null)
                    .lastLoginAt(Instant.now())
                    .build();
        }

        User user = userRepository.findById(principal.getId())
                .orElseThrow(() -> new UnauthorizedException("User not found"));

        return mapToUserDto(user);
    }

    private UserDto mapToUserDto(User user) {
        return UserDto.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .role(user.getRole())
                .createdAt(user.getCreatedAt())
                .lastLoginAt(user.getLastLoginAt())
                .build();
    }
}
