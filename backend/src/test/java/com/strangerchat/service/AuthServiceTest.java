package com.strangerchat.service;

import com.strangerchat.dto.AuthResponse;
import com.strangerchat.dto.GuestAuthResponse;
import com.strangerchat.dto.LoginRequest;
import com.strangerchat.dto.RegisterRequest;
import com.strangerchat.entity.User;
import com.strangerchat.exception.BadRequestException;
import com.strangerchat.exception.UnauthorizedException;
import com.strangerchat.repository.UserRepository;
import com.strangerchat.security.JwtTokenProvider;
import com.strangerchat.security.UserPrincipal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtTokenProvider tokenProvider;

    @InjectMocks
    private AuthService authService;

    private User sampleUser;

    @BeforeEach
    void setUp() {
        sampleUser = User.builder()
                .id(UUID.randomUUID())
                .username("alice")
                .email("alice@example.com")
                .passwordHash("hashed_pw")
                .role("ROLE_USER")
                .build();
    }

    @Test
    void testRegisterSuccess() {
        RegisterRequest req = RegisterRequest.builder()
                .username("alice")
                .email("alice@example.com")
                .password("secret123")
                .build();

        when(userRepository.existsByUsernameIgnoreCase("alice")).thenReturn(false);
        when(userRepository.existsByEmailIgnoreCase("alice@example.com")).thenReturn(false);
        when(passwordEncoder.encode("secret123")).thenReturn("hashed_pw");
        when(userRepository.save(any(User.class))).thenReturn(sampleUser);
        when(tokenProvider.generateTokenFromPrincipal(any(UserPrincipal.class))).thenReturn("mock_token");

        AuthResponse response = authService.register(req);

        assertNotNull(response);
        assertEquals("mock_token", response.getToken());
        assertEquals("alice", response.getUser().getUsername());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void testRegisterDuplicateUsernameThrowsBadRequest() {
        RegisterRequest req = RegisterRequest.builder()
                .username("alice")
                .email("alice2@example.com")
                .password("secret123")
                .build();

        when(userRepository.existsByUsernameIgnoreCase("alice")).thenReturn(true);

        assertThrows(BadRequestException.class, () -> authService.register(req));
        verify(userRepository, never()).save(any());
    }

    @Test
    void testLoginSuccess() {
        LoginRequest req = LoginRequest.builder()
                .usernameOrEmail("alice")
                .password("secret123")
                .build();

        UserPrincipal principal = UserPrincipal.create(sampleUser);
        Authentication auth = new UsernamePasswordAuthenticationToken(principal, null);

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(auth);
        when(userRepository.findById(sampleUser.getId())).thenReturn(Optional.of(sampleUser));
        when(tokenProvider.generateToken(auth)).thenReturn("login_jwt");

        AuthResponse res = authService.login(req);

        assertNotNull(res);
        assertEquals("login_jwt", res.getToken());
        assertEquals("alice", res.getUser().getUsername());
    }

    @Test
    void testLoginInvalidCredentialsThrowsUnauthorized() {
        LoginRequest req = LoginRequest.builder()
                .usernameOrEmail("alice")
                .password("wrong_password")
                .build();

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new RuntimeException("Bad credentials"));

        assertThrows(UnauthorizedException.class, () -> authService.login(req));
    }

    @Test
    void testCreateGuestSession() {
        when(tokenProvider.generateGuestToken(anyString(), anyString())).thenReturn("guest_jwt");

        GuestAuthResponse guestRes = authService.createGuestSession();

        assertNotNull(guestRes);
        assertEquals("guest_jwt", guestRes.getToken());
        assertTrue(guestRes.isGuest());
        assertTrue(guestRes.getDisplayName().startsWith("Guest-"));
        assertNotNull(guestRes.getGuestId());
    }
}
