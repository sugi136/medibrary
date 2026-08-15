package com.medibrary.api.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public final class AuthDtos {
    private AuthDtos() { }

    public record SignupRequest(
            @NotBlank @Email String email,
            @NotBlank @Size(min = 4) String password,
            @NotBlank @Size(max = 100) String name
    ) { }

    public record LoginRequest(
            @NotBlank @Email String email,
            @NotBlank String password
    ) { }

    public record TokenResponse(String accessToken, String tokenType) { }
}
