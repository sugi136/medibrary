package com.medibrary.api.dto;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public final class AuthDtos {
    private AuthDtos() { }

    public record SignupRequest(
            @NotBlank @Email String email,
            @NotBlank
            @Size(min = 8, max = 72)
            @Pattern(
                    regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[^A-Za-z0-9])\\S+$",
                    message = "비밀번호는 영문, 숫자, 기호를 각각 포함해야 합니다."
            ) String password,
            @NotBlank @Size(max = 100) String name,
            @AssertTrue(message = "서비스 약관에 동의해야 합니다.") boolean termsAgreed,
            @AssertTrue(message = "의료정보 고지에 동의해야 합니다.") boolean medicalNoticeAgreed
    ) { }

    public record LoginRequest(
            @NotBlank @Email String email,
            @NotBlank String password
    ) { }

    public record TokenResponse(String accessToken, String tokenType) { }
}
