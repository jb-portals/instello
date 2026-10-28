package com.instello.backend.auth.admin;

import com.instello.backend.auth.admin.dto.AdminLoginResponse;
import com.instello.backend.auth.admin.dto.AdminOtpRequest;
import com.instello.backend.auth.admin.dto.AdminVerifyOtpRequest;
import com.instello.backend.auth.otp.OtpService;
import com.instello.backend.common.security.SessionAuthenticator;
import com.instello.backend.user.entity.AppAuthority;
import com.instello.backend.user.entity.User;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/auth/admin")
@Tag(name = "auth/admin", description = "Platform admin authentication operations")
public class AdminController {

    private final OtpService otpService;
    private final SessionAuthenticator sessionAuthenticator;

    public AdminController(OtpService otpService, SessionAuthenticator sessionAuthenticator) {
        this.otpService = otpService;
        this.sessionAuthenticator = sessionAuthenticator;
    }

    @PostMapping("/otp")
    public ResponseEntity<Void> requestOTP(@Valid @RequestBody AdminOtpRequest requestBody) {
        otpService.requestAdminOtp(requestBody.email());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<AdminLoginResponse> verifyOTP(
            @Valid @RequestBody AdminVerifyOtpRequest requestBody,
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        User admin = otpService.verifyAdminOtp(requestBody.email(), requestBody.code());

        sessionAuthenticator.authenticate(
                request,
                response,
                admin.getEmail(),
                List.of(AppAuthority.ROLE_ADMIN.toGrantedAuthority())
        );

        return ResponseEntity.ok(new AdminLoginResponse(admin.getId(), admin.getEmail()));
    }
}
