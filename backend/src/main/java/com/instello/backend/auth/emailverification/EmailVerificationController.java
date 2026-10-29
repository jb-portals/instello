package com.instello.backend.auth.emailverification;

import com.instello.backend.auth.emailverification.dto.EmailOtpRequest;
import com.instello.backend.auth.emailverification.dto.VerifyEmailRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth/email-verification")
@Tag(name = "auth/email-verification", description = "Verify an account email with a one-time code")
public class EmailVerificationController {

    private final EmailVerificationService emailVerificationService;

    public EmailVerificationController(EmailVerificationService emailVerificationService) {
        this.emailVerificationService = emailVerificationService;
    }

    @PostMapping("/otp")
    @Operation(operationId = "emailVerificationResendOtp")
    public ResponseEntity<Void> resendOtp(@Valid @RequestBody EmailOtpRequest requestBody) {
        emailVerificationService.resendOtp(requestBody.email());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/verify")
    @Operation(operationId = "emailVerificationVerifyOtp")
    public ResponseEntity<Void> verify(@Valid @RequestBody VerifyEmailRequest requestBody) {
        emailVerificationService.verify(requestBody.email(), requestBody.code());
        return ResponseEntity.noContent().build();
    }
}
