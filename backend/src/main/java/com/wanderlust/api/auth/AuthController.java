package com.wanderlust.api.auth;

import com.wanderlust.api.common.ApiException;
import com.wanderlust.api.user.User;
import com.wanderlust.api.user.UserDto;
import com.wanderlust.api.user.UserRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication")
public class AuthController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    @PostMapping("/register")
    @Operation(summary = "Register a new account")
    public ResponseEntity<Map<String, String>> register(@Valid @RequestBody RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw ApiException.conflict("Username already taken");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw ApiException.conflict("Email already registered");
        }

        User user = User.builder()
                .username(request.getUsername())
                .displayName(request.getDisplayName())
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .build();
        user = userRepository.save(user);

        String token = jwtService.generateToken(user.getId(), user.getUsername());
        return ResponseEntity.ok(Map.of("token", token));
    }

    @PostMapping("/login")
    @Operation(summary = "Login with email and password")
    public ResponseEntity<Map<String, String>> login(@Valid @RequestBody LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> ApiException.badRequest("Invalid email or password"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw ApiException.badRequest("Invalid email or password");
        }

        String token = jwtService.generateToken(user.getId(), user.getUsername());
        return ResponseEntity.ok(Map.of("token", token));
    }

    @GetMapping("/me")
    @Operation(summary = "Get current user")
    public ResponseEntity<UserDto> me(@AuthenticationPrincipal UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> ApiException.notFound("User", userId));
        return ResponseEntity.ok(UserDto.from(user));
    }

    @Data
    public static class RegisterRequest {
        @NotBlank @Size(min = 3, max = 30)
        private String username;
        @NotBlank @Size(max = 100)
        private String displayName;
        @NotBlank @Email
        private String email;
        @NotBlank @Size(min = 6, max = 100)
        private String password;
    }

    @Data
    public static class LoginRequest {
        @NotBlank @Email
        private String email;
        @NotBlank
        private String password;
    }
}
