package com.instello.backend.common.notification.email;

import java.util.Map;

import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@Service
public class EmailTemplateService {

    static final String LOGO_URL = "https://instello.in/instello.svg";
    static final int OTP_EXPIRES_IN_MINUTES = 10;

    private static final String OTP_TEMPLATE = "email/otp";
    private static final String INVITATION_TEMPLATE = "email/invitation";

    private final TemplateEngine templateEngine;
    private final EmailService emailService;

    public EmailTemplateService(TemplateEngine templateEngine, EmailService emailService) {
        this.templateEngine = templateEngine;
        this.emailService = emailService;
    }

    public void sendOtp(String to, String code, int expiresInMinutes) {
        if (to == null || to.isBlank()) {
            throw new IllegalArgumentException("Recipient is required");
        }
        if (code == null || code.isBlank()) {
            throw new IllegalArgumentException("OTP code is required");
        }
        if (expiresInMinutes <= 0) {
            throw new IllegalArgumentException("OTP expiry must be positive");
        }

        String plainText = """
                Your Instello verification code is %s.
                It expires in %d minutes.

                If you did not request this code, you can ignore this email.
                """.formatted(code, expiresInMinutes).strip();

        send(
                to,
                "Your Instello verification code",
                OTP_TEMPLATE,
                plainText,
                Map.of(
                        "code", code,
                        "expiresInMinutes", expiresInMinutes,
                        "logoUrl", LOGO_URL
                )
        );
    }

    public void sendInvitation(
            String to,
            String firstName,
            String organizationName,
            String inviteUrl,
            int expiresInDays
    ) {
        if (to == null || to.isBlank()) {
            throw new IllegalArgumentException("Recipient is required");
        }
        if (inviteUrl == null || inviteUrl.isBlank()) {
            throw new IllegalArgumentException("Invite URL is required");
        }
        if (expiresInDays <= 0) {
            throw new IllegalArgumentException("Invitation expiry must be positive");
        }

        String plainText = """
                Hi %s,

                Your request to use Instello for %s has been approved.
                Create your account using the link below:

                %s

                This invitation expires in %d days.

                If you did not request access to Instello, you can ignore this email.
                """.formatted(firstName, organizationName, inviteUrl, expiresInDays).strip();

        send(
                to,
                "You're invited to Instello",
                INVITATION_TEMPLATE,
                plainText,
                Map.of(
                        "firstName", firstName,
                        "organizationName", organizationName,
                        "inviteUrl", inviteUrl,
                        "expiresInDays", expiresInDays,
                        "logoUrl", LOGO_URL
                )
        );
    }

    private void send(
            String to,
            String subject,
            String template,
            String plainText,
            Map<String, Object> variables
    ) {
        Context context = new Context();
        context.setVariables(variables);
        String html = templateEngine.process(template, context);
        emailService.sendEmail(to, subject, plainText, html);
    }
}
