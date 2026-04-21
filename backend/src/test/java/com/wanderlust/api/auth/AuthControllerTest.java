package com.wanderlust.api.auth;

import com.wanderlust.api.common.ApiException;
import com.wanderlust.api.user.User;
import com.wanderlust.api.user.UserDto;
import com.wanderlust.api.user.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private AuthController authController;

    // --- Registration tests ---

    @Test
    void register_success_returnsToken() {
        AuthController.RegisterRequest req = new AuthController.RegisterRequest();
        req.setUsername("traveler");
        req.setDisplayName("World Traveler");
        req.setEmail("traveler@example.com");
        req.setPassword("secret123");

        when(userRepository.existsByUsername("traveler")).thenReturn(false);
        when(userRepository.existsByEmail("traveler@example.com")).thenReturn(false);
        when(passwordEncoder.encode("secret123")).thenReturn("hashed");

        UUID userId = UUID.randomUUID();
        User savedUser = User.builder()
                .username("traveler")
                .displayName("World Traveler")
                .email("traveler@example.com")
                .passwordHash("hashed")
                .build();
        savedUser.setId(userId);

        when(userRepository.save(any(User.class))).thenReturn(savedUser);
        when(jwtService.generateToken(userId, "traveler")).thenReturn("jwt-token");

        ResponseEntity<Map<String, String>> response = authController.register(req);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).containsEntry("token", "jwt-token");
    }

    @Test
    void register_existingUsername_throwsConflict() {
        AuthController.RegisterRequest req = new AuthController.RegisterRequest();
        req.setUsername("taken");
        req.setDisplayName("Taken User");
        req.setEmail("new@example.com");
        req.setPassword("secret123");

        when(userRepository.existsByUsername("taken")).thenReturn(true);

        assertThatThrownBy(() -> authController.register(req))
                .isInstanceOf(ApiException.class)
                .hasMessage("Username already taken")
                .extracting(e -> ((ApiException) e).getStatus())
                .isEqualTo(HttpStatus.CONFLICT);
    }

    @Test
    void register_existingEmail_throwsConflict() {
        AuthController.RegisterRequest req = new AuthController.RegisterRequest();
        req.setUsername("newuser");
        req.setDisplayName("New User");
        req.setEmail("existing@example.com");
        req.setPassword("secret123");

        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(userRepository.existsByEmail("existing@example.com")).thenReturn(true);

        assertThatThrownBy(() -> authController.register(req))
                .isInstanceOf(ApiException.class)
                .hasMessage("Email already registered")
                .extracting(e -> ((ApiException) e).getStatus())
                .isEqualTo(HttpStatus.CONFLICT);
    }

    // --- Login tests ---

    @Test
    void login_success_returnsToken() {
        AuthController.LoginRequest req = new AuthController.LoginRequest();
        req.setEmail("traveler@example.com");
        req.setPassword("secret123");

        UUID userId = UUID.randomUUID();
        User user = User.builder()
                .username("traveler")
                .displayName("World Traveler")
                .email("traveler@example.com")
                .passwordHash("hashed")
                .build();
        user.setId(userId);

        when(userRepository.findByEmail("traveler@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("secret123", "hashed")).thenReturn(true);
        when(jwtService.generateToken(userId, "traveler")).thenReturn("jwt-token");

        ResponseEntity<Map<String, String>> response = authController.login(req);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).containsEntry("token", "jwt-token");
    }

    @Test
    void login_wrongEmail_throwsBadRequest() {
        AuthController.LoginRequest req = new AuthController.LoginRequest();
        req.setEmail("nonexistent@example.com");
        req.setPassword("secret123");

        when(userRepository.findByEmail("nonexistent@example.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authController.login(req))
                .isInstanceOf(ApiException.class)
                .hasMessage("Invalid email or password")
                .extracting(e -> ((ApiException) e).getStatus())
                .isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void login_wrongPassword_throwsBadRequest() {
        AuthController.LoginRequest req = new AuthController.LoginRequest();
        req.setEmail("traveler@example.com");
        req.setPassword("wrong");

        User user = User.builder()
                .username("traveler")
                .displayName("World Traveler")
                .email("traveler@example.com")
                .passwordHash("hashed")
                .build();
        user.setId(UUID.randomUUID());

        when(userRepository.findByEmail("traveler@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong", "hashed")).thenReturn(false);

        assertThatThrownBy(() -> authController.login(req))
                .isInstanceOf(ApiException.class)
                .hasMessage("Invalid email or password")
                .extracting(e -> ((ApiException) e).getStatus())
                .isEqualTo(HttpStatus.BAD_REQUEST);
    }

    // --- me() test ---

    @Test
    void me_returnsCurrentUser() {
        UUID userId = UUID.randomUUID();
        User user = User.builder()
                .username("traveler")
                .displayName("World Traveler")
                .email("traveler@example.com")
                .passwordHash("hashed")
                .build();
        user.setId(userId);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        ResponseEntity<UserDto> response = authController.me(userId);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getId()).isEqualTo(userId);
        assertThat(response.getBody().getUsername()).isEqualTo("traveler");
        assertThat(response.getBody().getDisplayName()).isEqualTo("World Traveler");
    }
}
