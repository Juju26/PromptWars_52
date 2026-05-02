package com.promptwars.user.service;

import com.promptwars.common.exception.BusinessException;
import com.promptwars.common.security.JwtTokenProvider;
import com.promptwars.user.domain.User;
import com.promptwars.user.dto.AuthRequest;
import com.promptwars.user.dto.AuthResponse;
import com.promptwars.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @InjectMocks
    private AuthService authService;

    private User user;
    private UUID userId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        user = User.builder()
                .id(userId)
                .username("testuser")
                .email("test@example.com")
                .passwordHash("hashedpassword")
                .role(User.Role.PARTICIPANT)
                .build();
        
        ReflectionTestUtils.setField(authService, "accessTokenExpiryMs", 900000L);
    }

    @Test
    void register_Success() {
        AuthRequest.Register request = new AuthRequest.Register("testuser", "test@example.com", "password", "Test User");

        when(userRepository.existsByEmail(request.email())).thenReturn(false);
        when(userRepository.existsByUsername(request.username())).thenReturn(false);
        when(passwordEncoder.encode(request.password())).thenReturn("hashedpassword");
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(jwtTokenProvider.generateAccessToken(anyString(), any())).thenReturn("access-token");
        when(jwtTokenProvider.generateRefreshToken(anyString())).thenReturn("refresh-token");

        AuthResponse response = authService.register(request);

        assertNotNull(response);
        assertEquals("access-token", response.accessToken());
        assertEquals("testuser", response.user().username());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void register_EmailAlreadyExists() {
        AuthRequest.Register request = new AuthRequest.Register("testuser", "test@example.com", "password", "Test User");

        when(userRepository.existsByEmail(request.email())).thenReturn(true);

        assertThrows(BusinessException.class, () -> authService.register(request));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void register_UsernameAlreadyExists() {
        AuthRequest.Register request = new AuthRequest.Register("testuser", "test@example.com", "password", "Test User");

        when(userRepository.existsByEmail(request.email())).thenReturn(false);
        when(userRepository.existsByUsername(request.username())).thenReturn(true);

        assertThrows(BusinessException.class, () -> authService.register(request));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void login_Success() {
        AuthRequest.Login request = new AuthRequest.Login("test@example.com", "password");

        when(userRepository.findActiveByEmail(request.email())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(request.password(), user.getPasswordHash())).thenReturn(true);
        when(jwtTokenProvider.generateAccessToken(anyString(), any())).thenReturn("access-token");
        when(jwtTokenProvider.generateRefreshToken(anyString())).thenReturn("refresh-token");

        AuthResponse response = authService.login(request);

        assertNotNull(response);
        assertEquals("access-token", response.accessToken());
        assertEquals("test@example.com", response.user().email());
    }

    @Test
    void login_InvalidPassword() {
        AuthRequest.Login request = new AuthRequest.Login("test@example.com", "wrongpassword");

        when(userRepository.findActiveByEmail(request.email())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(request.password(), user.getPasswordHash())).thenReturn(false);

        assertThrows(BusinessException.class, () -> authService.login(request));
    }

    @Test
    void login_UserNotFound() {
        AuthRequest.Login request = new AuthRequest.Login("test@example.com", "password");

        when(userRepository.findActiveByEmail(request.email())).thenReturn(Optional.empty());

        assertThrows(BusinessException.class, () -> authService.login(request));
    }
}
