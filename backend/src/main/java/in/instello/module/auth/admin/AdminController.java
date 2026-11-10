package in.instello.module.auth.admin;

import in.instello.module.auth.admin.dto.AdminLoginResponse;
import in.instello.module.auth.admin.dto.AdminOtpRequest;
import in.instello.module.auth.admin.dto.AdminVerifyOtpRequest;
import in.instello.module.auth.otp.OtpService;
import in.instello.module.user.entity.AppAuthority;
import in.instello.module.user.entity.User;
import in.instello.security.SessionAuthenticator;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
  @Operation(operationId = "requestAdminOtp")
  public ResponseEntity<Void> requestOtp(@Valid @RequestBody AdminOtpRequest requestBody) {
    otpService.requestAdminOtp(requestBody.email());
    return ResponseEntity.noContent().build();
  }

  @PostMapping("/verify-otp")
  @Operation(operationId = "verifyAdminOtp")
  public ResponseEntity<AdminLoginResponse> verifyOtp(
      @Valid @RequestBody AdminVerifyOtpRequest requestBody,
      HttpServletRequest request,
      HttpServletResponse response) {
    User admin = otpService.verifyAdminOtp(requestBody.email(), requestBody.code());

    sessionAuthenticator.authenticate(
        request, response, admin.getEmail(), List.of(AppAuthority.ROLE_ADMIN.toGrantedAuthority()));

    return ResponseEntity.ok(new AdminLoginResponse(admin.getId(), admin.getEmail()));
  }
}
