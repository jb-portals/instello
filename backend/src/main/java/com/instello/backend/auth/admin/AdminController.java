package com.instello.backend.auth.admin;

import com.instello.backend.auth.admin.dto.AdminLoginResponse;
import com.instello.backend.auth.admin.dto.AdminOtpRequest;
import com.instello.backend.auth.admin.dto.AdminVerifyOtpRequest;
import com.instello.backend.auth.otp.OtpService;
import com.instello.backend.common.response.ApiResponse;
import com.instello.backend.user.entity.User;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/auth/admin")
public class AdminController {

    private final OtpService otpService;
    private final SecurityContextRepository securityContextRepository;

    public AdminController(OtpService otpService, SecurityContextRepository securityContextRepository) {
        this.otpService = otpService;
        this.securityContextRepository = securityContextRepository;
    }

    @PostMapping("/otp")
    public ResponseEntity<ApiResponse<Void>> requestAdminOtp(@Valid @RequestBody AdminOtpRequest requestBody) {
        otpService.requestAdminOtp(requestBody.email());

        return ResponseEntity.ok(ApiResponse.ok("If this email belongs to an admin, an OTP has been sent"));
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<ApiResponse<AdminLoginResponse>> verifyAdminOtp(
            @Valid @RequestBody AdminVerifyOtpRequest requestBody,
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        User admin = otpService.verifyAdminOtp(requestBody.email(), requestBody.code());

        UsernamePasswordAuthenticationToken authentication = UsernamePasswordAuthenticationToken.authenticated(
                admin.getEmail(),
                null,
                List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))
        );

        request.getSession(true);
        request.changeSessionId();

        SecurityContext context = SecurityContextHolder.getContextHolderStrategy().createEmptyContext();
        context.setAuthentication(authentication);
        SecurityContextHolder.getContextHolderStrategy().setContext(context);
        securityContextRepository.saveContext(context, request, response);

        return ResponseEntity.ok(ApiResponse.ok(
                "Login successful",
                new AdminLoginResponse(admin.getId(), admin.getEmail())
        ));
    }
}
