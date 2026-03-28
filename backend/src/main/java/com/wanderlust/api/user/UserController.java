package com.wanderlust.api.user;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "Users")
public class UserController {

    private final UserService userService;

    @GetMapping("/{id}")
    @Operation(summary = "Get user profile by ID")
    public ResponseEntity<UserDto> getUser(@PathVariable UUID id) {
        return ResponseEntity.ok(userService.getById(id));
    }

    @GetMapping("/username/{username}")
    @Operation(summary = "Get user profile by username")
    public ResponseEntity<UserDto> getUserByUsername(@PathVariable String username) {
        return ResponseEntity.ok(userService.getByUsername(username));
    }

    @PutMapping("/me")
    @Operation(summary = "Update own profile")
    public ResponseEntity<UserDto> updateProfile(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody UpdateProfileRequest request) {
        UUID userId = userService.resolveUserId(jwt);
        return ResponseEntity.ok(userService.updateProfile(userId, request));
    }

    @GetMapping("/{id}/stats")
    @Operation(summary = "Get user statistics")
    public ResponseEntity<UserStatsDto> getStats(@PathVariable UUID id) {
        return ResponseEntity.ok(userService.getStats(id));
    }
}
