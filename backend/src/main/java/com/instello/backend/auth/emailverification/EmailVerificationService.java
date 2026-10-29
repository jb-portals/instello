package com.instello.backend.auth.emailverification;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Locale;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.instello.backend.auth.otp.InvalidOtpException;
import com.instello.backend.auth.otp.OtpService;
import com.instello.backend.common.exception.ConflictException;
import com.instello.backend.invitation.entity.Invitation;
import com.instello.backend.invitation.repository.InvitationRepository;
import com.instello.backend.organization.OrganizationMembershipService;
import com.instello.backend.user.entity.Authority;
import com.instello.backend.user.entity.User;
import com.instello.backend.user.repository.UserRepository;

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
            OrganizationMembershipService membershipService
    ) {
        this.otpService = otpService;
        this.userRepository = userRepository;
        this.invitationRepository = invitationRepository;
        this.membershipService = membershipService;
    }

    /**
     * Sends a fresh code only to an unverified account that still has an open invitation. Rate limits apply
     * regardless, and the response never reveals whether the account exists.
     */
    public void resendOtp(String email) {
        String normalizedEmail = normalize(email);
        otpService.requestEmailVerificationOtp(normalizedEmail, () -> isAwaitingVerification(normalizedEmail));
    }

    /**
     * Marks the email as verified, accepts the open invitation and grants the invited authority.
     * The user is not logged in; they sign in afterwards with email and password.
     */
    @Transactional
    public void verify(String email, String code) {
        String normalizedEmail = normalize(email);
        otpService.verifyEmailVerificationOtp(normalizedEmail, code);

        User user = userRepository.findWithAuthoritiesByEmailIgnoreCase(normalizedEmail)
                .filter(candidate -> candidate.getEmailVerifiedAt() == null)
                .orElseThrow(InvalidOtpException::new);

        Invitation invitation = invitationRepository
                .findFirstByEmailAndAcceptedAtIsNullAndRevokedAtIsNullOrderByCreatedAtDesc(normalizedEmail)
                .orElseThrow(() -> new ConflictException("Invitation is no longer valid"));

        Instant now = Instant.now();
        user.setEmailVerifiedAt(now);
        invitation.setAcceptedAt(now);

        if (user.getAuthorities() == null) {
            user.setAuthorities(new ArrayList<>());
        }
        boolean alreadyGranted = user.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority() == invitation.getRole());
        if (!alreadyGranted) {
            user.getAuthorities().add(Authority.builder()
                    .authority(invitation.getRole())
                    .user(user)
                    .build());
        }
        membershipService.ensureMember(user, invitation.getOrganization(), invitation.getRole());
    }

    private boolean isAwaitingVerification(String normalizedEmail) {
        return userRepository.findByEmailIgnoreCase(normalizedEmail)
                .filter(user -> user.getEmailVerifiedAt() == null)
                .isPresent()
                && invitationRepository.existsByEmailAndAcceptedAtIsNullAndRevokedAtIsNull(normalizedEmail);
    }

    private static String normalize(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }
}
