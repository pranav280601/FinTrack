package com.fintrack.user_service.service;

import com.fintrack.user_service.dto.LoginRequest;
import com.fintrack.user_service.dto.RegisterRequest;
import com.fintrack.user_service.dto.AuthResponse;
import com.fintrack.user_service.exception.InvalidCredentialsException;
import com.fintrack.user_service.exception.UserAlreadyExistsException;
import com.fintrack.user_service.model.User;
import com.fintrack.user_service.repository.UserRepository;
import com.fintrack.user_service.security.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
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
    private JwtUtil jwtUtil;

    @InjectMocks
    private AuthService authService;

    private RegisterRequest registerRequest;
    private LoginRequest loginRequest;
    private User existingUser;

    @BeforeEach
    void setUp() {
        registerRequest = new RegisterRequest();
        registerRequest.setName("Pranav");
        registerRequest.setEmail("pranav@fintrack.com");
        registerRequest.setPassword("password123");

        loginRequest = new LoginRequest();
        loginRequest.setEmail("pranav@fintrack.com");
        loginRequest.setPassword("password123");

        existingUser = new User();
        existingUser.setId(1L);
        existingUser.setName("Pranav");
        existingUser.setEmail("pranav@fintrack.com");
        existingUser.setPassword("hashedPassword");
        existingUser.setRole(User.Role.USER);
    }

    // ─── Register Tests ───────────────────────────────────────

    @Test
    @DisplayName("Should register user successfully when email is not taken")
    void register_Success() {
        // Arrange
        when(userRepository.existsByEmail(registerRequest.getEmail()))
            .thenReturn(false);
        when(passwordEncoder.encode(registerRequest.getPassword()))
            .thenReturn("hashedPassword");
        when(userRepository.save(any(User.class)))
            .thenReturn(existingUser);

        // Act
        String result = authService.register(registerRequest);

        // Assert
        assertThat(result).isEqualTo("User registered successfully");
        verify(userRepository).save(any(User.class));
        verify(passwordEncoder).encode(registerRequest.getPassword());
    }

    @Test
    @DisplayName("Should throw UserAlreadyExistsException when email is taken")
    void register_DuplicateEmail_ThrowsException() {
        // Arrange
        when(userRepository.existsByEmail(registerRequest.getEmail()))
            .thenReturn(true);

        // Act & Assert
        assertThatThrownBy(() -> authService.register(registerRequest))
            .isInstanceOf(UserAlreadyExistsException.class)
            .hasMessageContaining("pranav@fintrack.com");

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Should never store plain text password")
    void register_PasswordIsHashed() {
        // Arrange
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("hashedPassword");
        when(userRepository.save(any(User.class))).thenReturn(existingUser);

        // Act
        authService.register(registerRequest);

        // Assert — verify encode was called, never save with plain password
        verify(passwordEncoder).encode("password123");
        verify(userRepository).save(argThat(user ->
            !user.getPassword().equals("password123")
        ));
    }

    // ─── Login Tests ──────────────────────────────────────────

    @Test
    @DisplayName("Should return token on successful login")
    void login_Success() {
        // Arrange
        when(userRepository.findByEmail(loginRequest.getEmail()))
            .thenReturn(Optional.of(existingUser));
        when(passwordEncoder.matches(loginRequest.getPassword(),
            existingUser.getPassword()))
            .thenReturn(true);
        when(jwtUtil.generateToken(
            existingUser.getId().toString(),
            existingUser.getEmail(),
            existingUser.getRole().name()))
            .thenReturn("mockToken");

        // Act
        AuthResponse response = authService.login(loginRequest);

        // Assert
        assertThat(response.getToken()).isEqualTo("mockToken");
        assertThat(response.getEmail()).isEqualTo("pranav@fintrack.com");
        assertThat(response.getRole()).isEqualTo("USER");
    }

    @Test
    @DisplayName("Should throw InvalidCredentialsException when user not found")
    void login_UserNotFound_ThrowsException() {
        // Arrange
        when(userRepository.findByEmail(loginRequest.getEmail()))
            .thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> authService.login(loginRequest))
            .isInstanceOf(InvalidCredentialsException.class);

        verify(jwtUtil, never()).generateToken(anyString(), anyString(), anyString());
    }

    @Test
    @DisplayName("Should throw InvalidCredentialsException when password is wrong")
    void login_WrongPassword_ThrowsException() {
        // Arrange
        when(userRepository.findByEmail(loginRequest.getEmail()))
            .thenReturn(Optional.of(existingUser));
        when(passwordEncoder.matches(anyString(), anyString()))
            .thenReturn(false);

        // Act & Assert
        assertThatThrownBy(() -> authService.login(loginRequest))
            .isInstanceOf(InvalidCredentialsException.class);

        verify(jwtUtil, never()).generateToken(anyString(), anyString(), anyString());
    }

    @Test
    @DisplayName("Should return same error for wrong password and unknown email")
    void login_SameErrorForWrongPasswordAndUnknownEmail() {
        // User not found
        when(userRepository.findByEmail("unknown@email.com"))
            .thenReturn(Optional.empty());

        LoginRequest unknownRequest = new LoginRequest();
        unknownRequest.setEmail("unknown@email.com");
        unknownRequest.setPassword("password123");

        // Wrong password
        when(userRepository.findByEmail(loginRequest.getEmail()))
            .thenReturn(Optional.of(existingUser));
        when(passwordEncoder.matches(anyString(), anyString()))
            .thenReturn(false);

        // Both should throw same exception type
        assertThatThrownBy(() -> authService.login(unknownRequest))
            .isInstanceOf(InvalidCredentialsException.class)
            .hasMessage("Invalid email or password");

        assertThatThrownBy(() -> authService.login(loginRequest))
            .isInstanceOf(InvalidCredentialsException.class)
            .hasMessage("Invalid email or password");
    }
}