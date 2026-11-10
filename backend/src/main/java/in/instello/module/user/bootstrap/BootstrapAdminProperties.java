package in.instello.module.user.bootstrap;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("instello.bootstrap.admin")
public record BootstrapAdminProperties(
        String email
) {
}
