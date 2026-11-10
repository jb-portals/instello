package in.instello.module.auth.emailverification;

import in.instello.common.exception.app.ConflictException;
import in.instello.module.auth.otp.InvalidOtpException;
import in.instello.module.auth.otp.OtpService;
import in.instello.module.invitation.entity.Invitation;
import in.instello.module.invitation.repository.InvitationRepository;
import in.instello.module.organization.OrganizationMembershipService;
import in.instello.module.user.entity.Authority;
import in.instello.module.user.entity.User;
import in.instello.module.user.repository.UserRepository;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class EmailVerificationService {

  private final OtpService otpService;
  private final UserRepository userRepository;
  private final InvitationRepository invitationRepository;
  private final OrganizationMembershipService membershipService;

  public EmailVerificationService(
      OtpService otpService,
      UserRepository userRepository,
      InvitationRepository invitationRepository,
      OrganizationMembershipService membershipService) {
    this.otpService = otpService;
    this.userRepository = userRepository;
    this.invitationRepository = invitationRepository;
    this.membershipService = membershipService;
  }

  /**
   * Sends a fresh code only to an unverified account that still has an open invitation. Rate limits
   * apply regardless, and the response never reveals whether the account exists.
   */
  public void resendOtp(String email) {
    String normalizedEmail = normalize(email);
    otpService.requestEmailVerificationOtp(
        normalizedEmail, () -> isAwaitingVerification(normalizedEmail));
  }

  /**
   * Marks the email as verified, accepts the open invitation and grants the invited authority. The
   * user is not logged in; they sign in afterwards with email and password.
   */
  @Transactional
  public void verify(String email, String code) {
    String normalizedEmail = normalize(email);
    otpService.verifyEmailVerificationOtp(normalizedEmail, code);

    User user =
        userRepository
            .findWithAuthoritiesByEmailIgnoreCase(normalizedEmail)
            .filter(candidate -> candidate.getEmailVerifiedAt() == null)
            .orElseThrow(InvalidOtpException::new);

    Invitation invitation =
        invitationRepository
            .findFirstByEmailAndAcceptedAtIsNullAndRevokedAtIsNullOrderByCreatedAtDesc(
                normalizedEmail)
            .orElseThrow(() -> new ConflictException("Invitation is no longer valid"));

    Instant now = Instant.now();
    user.setEmailVerifiedAt(now);
    invitation.setAcceptedAt(now);

    List<Authority> authorities = user.getAuthorities();
    if (authorities == null) {
      authorities = new ArrayList<>();
      user.setAuthorities(authorities);
    }
    boolean alreadyGranted =
        authorities.stream()
            .anyMatch(authority -> authority.getAuthority() == invitation.getRole());
    if (!alreadyGranted) {
      authorities.add(Authority.builder().authority(invitation.getRole()).user(user).build());
    }
    membershipService.ensureMember(user, invitation.getOrganization(), invitation.getRole());
  }

  private boolean isAwaitingVerification(String normalizedEmail) {
    return userRepository
            .findByEmailIgnoreCase(normalizedEmail)
            .filter(user -> user.getEmailVerifiedAt() == null)
            .isPresent()
        && invitationRepository.existsByEmailAndAcceptedAtIsNullAndRevokedAtIsNull(normalizedEmail);
  }

  private static String normalize(String email) {
    return email.trim().toLowerCase(Locale.ROOT);
  }
}
