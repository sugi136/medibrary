package com.medibrary.api.dto;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SignupRequestValidationTest {
    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    void acceptsPasswordWithLettersNumbersSymbolsAndAllRequiredConsents() {
        var request = new AuthDtos.SignupRequest(
                "user@example.com", "Medibrary1!", "사용자", true, true
        );

        assertTrue(validator.validate(request).isEmpty());
    }

    @Test
    void rejectsWeakPasswordOrMissingRequiredConsent() {
        var weakPassword = new AuthDtos.SignupRequest(
                "user@example.com", "onlyletters", "사용자", true, true
        );
        var missingTerms = new AuthDtos.SignupRequest(
                "user@example.com", "Medibrary1!", "사용자", false, true
        );
        var missingMedicalNotice = new AuthDtos.SignupRequest(
                "user@example.com", "Medibrary1!", "사용자", true, false
        );

        assertFalse(validator.validate(weakPassword).isEmpty());
        assertFalse(validator.validate(missingTerms).isEmpty());
        assertFalse(validator.validate(missingMedicalNotice).isEmpty());
    }
}
