package com.promptwars.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public sealed interface AuthRequest permits AuthRequest.Register, AuthRequest.Login {

    record Register(
            @NotBlank(message = "Username is required")
            @Size(min = 3, max = 50, message = "Username must be 3-50 characters")
            String username,

            @NotBlank(message = "Email is required")
            @Email(message = "Invalid email format")
            String email,

            @NotBlank(message = "Password is required")
            @Size(min = 8, message = "Password must be at least 8 characters")
            String password,

            String displayName
    ) implements AuthRequest {}

    record Login(
            @NotBlank(message = "Email is required")
            @Email
            String email,

            @NotBlank(message = "Password is required")
            String password
    ) implements AuthRequest {}
}
