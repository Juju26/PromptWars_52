package com.promptwars.user.controller;

import com.promptwars.common.dto.ApiResponse;
import com.promptwars.user.dto.AuthRequest;
import com.promptwars.user.dto.AuthResponse;
import com.promptwars.user.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Register, login, and refresh JWT tokens")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Register a new participant")
    public ApiResponse<AuthResponse> register(@Valid @RequestBody AuthRequest.Register request) {
        return ApiResponse.ok("Registration successful", authService.register(request));
    }

    @PostMapping("/login")
    @Operation(summary = "Login and receive a JWT token pair")
    public ApiResponse<AuthResponse> login(@Valid @RequestBody AuthRequest.Login request) {
        return ApiResponse.ok("Login successful", authService.login(request));
    }

    @PostMapping("/refresh")
    @Operation(summary = "Exchange a refresh token for a new access token")
    public ApiResponse<AuthResponse> refresh(@RequestHeader("X-Refresh-Token") String refreshToken) {
        return ApiResponse.ok("Token refreshed", authService.refreshToken(refreshToken));
    }
}
