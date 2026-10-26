package com.instello.backend.common.openAPI;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;

@Configuration
public class OpenAPIConfiguration {

    @Bean
    OpenAPI instelloOpenAPI(@Value("${spring.application.version}") String version) {
        return new OpenAPI()
                .info(new Info()
                        .title("Instello API")
                        .description("""
                                Instello ERP backend API.

                                Authenticated endpoints use the session cookie set by the admin OTP login. \
                                State-changing requests also need the CSRF token from GET /auth/csrf-token \
                                in the X-CSRF-TOKEN header.""")
                        .version(version));
    }
}
