package in.instello.security;

import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("instello.cors")
public record CorsProperties(List<String> allowedOrigins) {
}
