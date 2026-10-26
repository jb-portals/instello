package com.instello.backend.notification.email;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Properties;

import com.instello.backend.common.notification.email.EmailService;
import com.instello.backend.common.notification.email.EmailTemplateService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mail.javamail.JavaMailSender;
import org.thymeleaf.spring6.SpringTemplateEngine;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;

import jakarta.mail.Address;
import jakarta.mail.BodyPart;
import jakarta.mail.Message;
import jakarta.mail.Session;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeMultipart;

class EmailTemplateServiceTest {

    private JavaMailSender mailSender;
    private MimeMessage message;
    private EmailTemplateService emailTemplateService;

    @BeforeEach
    void setUp() {
        mailSender = mock(JavaMailSender.class);
        message = new MimeMessage(Session.getInstance(new Properties()));
        when(mailSender.createMimeMessage()).thenReturn(message);

        ClassLoaderTemplateResolver resolver = new ClassLoaderTemplateResolver();
        resolver.setPrefix("templates/");
        resolver.setSuffix(".html");
        resolver.setTemplateMode(TemplateMode.HTML);
        resolver.setCharacterEncoding("UTF-8");
        resolver.setCacheable(false);

        SpringTemplateEngine templateEngine = new SpringTemplateEngine();
        templateEngine.setTemplateResolver(resolver);

        EmailService emailService = new EmailService(mailSender, "noreply@instello.local");
        emailTemplateService = new EmailTemplateService(templateEngine, emailService);
    }

    @Test
    void sendOtpRendersTemplateAndSendsMail() throws Exception {
        emailTemplateService.sendOtp("student@instello.in", "482913", 15);

        verify(mailSender).send(message);
        message.saveChanges();

        assertThat(message.getSubject()).isEqualTo("Your Instello verification code");

        Address[] recipients = message.getRecipients(Message.RecipientType.TO);
        assertThat(recipients).singleElement()
                .isInstanceOf(InternetAddress.class)
                .extracting(address -> ((InternetAddress) address).getAddress())
                .isEqualTo("student@instello.in");

        InternetAddress from = (InternetAddress) message.getFrom()[0];
        assertThat(from.getAddress()).isEqualTo("noreply@instello.local");
        assertThat(from.getPersonal()).isEqualTo("Instello");

        String html = part(message, "text/html");
        assertThat(html)
                .contains("482913")
                .contains("https://instello.in/instello.svg")
                .contains(">15</span>")
                .contains("Verify your email")
                .contains("letter-spacing")
                .doesNotContain("class=")
                .doesNotContain("@tailwindcss/browser")
                .doesNotContainPattern("\\sth:\\w+=")
                .doesNotContain("000000");

        String plainText = part(message, "text/plain");
        assertThat(plainText)
                .contains("482913")
                .contains("15 minutes");
    }

    @Test
    void sendInvitationRendersTemplateAndSendsMail() throws Exception {
        String inviteUrl = "https://app.instello.in/signup?token=abc123";
        emailTemplateService.sendInvitation("owner@school.in", "Asha", "Sunrise Public School", inviteUrl, 7);

        verify(mailSender).send(message);
        message.saveChanges();

        assertThat(message.getSubject()).isEqualTo("You're invited to Instello");

        String html = part(message, "text/html");
        assertThat(html)
                .contains("Asha")
                .contains("Sunrise Public School")
                .contains("href=\"" + inviteUrl + "\"")
                .contains(">7</span>")
                .doesNotContainPattern("\\sth:\\w+=");

        String plainText = part(message, "text/plain");
        assertThat(plainText)
                .contains(inviteUrl)
                .contains("7 days");
    }

    private static String part(MimeMessage message, String mimeType) throws Exception {
        Object content = message.getContent();
        if (content instanceof String body && message.isMimeType(mimeType)) {
            return body;
        }
        if (content instanceof MimeMultipart multipart) {
            return part(multipart, mimeType);
        }
        throw new AssertionError("No " + mimeType + " part in message");
    }

    private static String part(MimeMultipart multipart, String mimeType) throws Exception {
        for (int index = 0; index < multipart.getCount(); index++) {
            BodyPart bodyPart = multipart.getBodyPart(index);
            if (bodyPart.isMimeType(mimeType)) {
                Object content = bodyPart.getContent();
                if (content instanceof String body) {
                    return body;
                }
            }
            if (bodyPart.getContent() instanceof MimeMultipart nested) {
                String nestedPart = part(nested, mimeType);
                if (nestedPart != null) {
                    return nestedPart;
                }
            }
        }
        return null;
    }
}
