package com.instello.backend.invitation;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.Locale;
import java.util.Map;



import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.UriComponentsBuilder;

import com.instello.backend.accessRequest.entity.AccessRequest;
import com.instello.backend.auth.otp.OtpService;
import com.instello.backend.common.exception.ConflictException;
import com.instello.backend.common.exception.ResourceNotFoundException;
import com.instello.backend.common.notification.email.EmailTemplateService;
import com.instello.backend.invitation.dto.AcceptInvitationResponse;
import com.instello.backend.invitation.dto.InvitationPreviewResponse;
import com.instello.backend.invitation.entity.Invitation;
import com.instello.backend.invitation.entity.InvitationStatus;
import com.instello.backend.invitation.repository.InvitationRepository;
import com.instello.backend.organization.entity.Organization;
import com.instello.backend.user.entity.AppAuthority;
import com.instello.backend.user.entity.User;
import com.instello.backend.user.entity.UserProfile;
import com.instello.backend.user.repository.UserRepository;

@Service
public class InvitationService {

    static final String EMAIL_METADATA_KEY = "email";

    private static final int TOKEN_BYTES = 32;

    private final SecureRandom secureRandom = new SecureRandom();
    private final InvitationRepository invitationRepository;
    private final EmailTemplateService emailTemplateService;
    private final InvitationProperties properties;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final OtpService otpService;

    public InvitationService(
            InvitationRepository invitationRepository,
            EmailTemplateService emailTemplateService,
            InvitationProperties properties,
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            OtpService otpService
    ) {
        this.invitationRepository = invitationRepository;
        this.emailTemplateService = emailTemplateService;
        this.properties = properties;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.otpService = otpService;
    }

    @Transactional
    public Invitation createOwnerInvitation(
            Organization organization,
            String email,
            String firstName,
            AccessRequest accessRequest,
            User invitedBy
    ) {
        String rawToken = generateToken();
        String normalizedEmail = email.trim().toLowerCase(Locale.ROOT);

        Invitation invitation = invitationRepository.save(Invitation.builder()
                .email(normalizedEmail)
                .organization(organization)
                .role(AppAuthority.ROLE_OWNER)
                .metadata(Map.of(EMAIL_METADATA_KEY, normalizedEmail))
                .accessRequest(accessRequest)
                .tokenHash(hash(rawToken))
                .expiresAt(Instant.now().plus(properties.ttl()))
                .invitedBy(invitedBy)
                .build());

        String inviteUrl = UriComponentsBuilder.fromUriString(properties.acceptUrl())
                .queryParam("token", rawToken)
                .build()
                .toUriString();

        emailTemplateService.sendInvitation(
                invitation.getEmail(),
                firstName,
                organization.getName(),
                inviteUrl,
                (int) properties.ttl().toDays()
        );

        return invitation;
    }

    @Transactional(readOnly = true)
    public InvitationPreviewResponse preview(String token) {
        Invitation invitation = findPending(token);
        return new InvitationPreviewResponse(
                lockedEmail(invitation),
                invitation.getOrganization().getName(),
                invitation.getExpiresAt()
        );
    }

    /**
     * Creates (or refreshes) an unverified account for the invited email and sends the verification OTP.
     * The invitation stays open until the email is verified, so an abandoned attempt can be retried.
     */
    @Transactional
    public AcceptInvitationResponse accept(String token, String password) {
        Invitation invitation = findPending(token);
        String email = lockedEmail(invitation);

        User user = userRepository.findByEmailIgnoreCase(email).orElse(null);
        if (user != null && user.getEmailVerifiedAt() != null) {
            throw new ConflictException("An account with this email already exists");
        }

        if (user == null) {
            user = User.builder()
                    .email(email)
                    .authorities(new ArrayList<>())
                    .build();
            user.setUserProfile(UserProfile.builder()
                    .firstName(firstNameFor(invitation, email))
                    .lastName(invitation.getAccessRequest() != null ? invitation.getAccessRequest().getLastName() : null)
                    .user(user)
                    .build());
        }
        user.setPasswordHash(passwordEncoder.encode(password));

        try {
            userRepository.saveAndFlush(user);
        } catch (DataIntegrityViolationException exception) {
            throw new ConflictException("An account with this email already exists");
        }

        otpService.requestEmailVerificationOtp(email, () -> true);

        return new AcceptInvitationResponse(email);
    }

    @Transactional
    public void revokeActive(String accessRequestId) {
        Instant now = Instant.now();
        invitationRepository.findByAccessRequestIdAndAcceptedAtIsNullAndRevokedAtIsNull(accessRequestId)
                .forEach(invitation -> invitation.setRevokedAt(now));
    }

    private Invitation findPending(String token) {
        return invitationRepository.findByTokenHash(hash(token.trim()))
                .filter(invitation -> invitation.getStatus() == InvitationStatus.PENDING)
                .orElseThrow(() -> new ResourceNotFoundException("Invitation not found or no longer valid"));
    }

    // The email is fixed at invite time; the invitee can never choose it, and it must match the invitation row
    private static String lockedEmail(Invitation invitation) {
        Map<String, String> metadata = invitation.getMetadata();
        String email = metadata != null ? metadata.get(EMAIL_METADATA_KEY) : null;
        if (email == null || email.isBlank() || !email.trim().equalsIgnoreCase(invitation.getEmail())) {
            throw new ConflictException("Invitation is invalid, please ask for a new one");
        }
        return email.trim().toLowerCase(Locale.ROOT);
    }

    private static String firstNameFor(Invitation invitation, String email) {
        if (invitation.getAccessRequest() != null) {
            return invitation.getAccessRequest().getFirstName();
        }
        return email.substring(0, email.indexOf('@') > 0 ? email.indexOf('@') : email.length());
    }

    private String generateToken() {
        byte[] bytes = new byte[TOKEN_BYTES];
        secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    static String hash(String rawToken) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(rawToken.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 is not available", exception);
        }
    }
}
