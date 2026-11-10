package in.instello.module.invitation;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("instello.invitation")
public record InvitationProperties(Duration ttl, String acceptUrl) {}
