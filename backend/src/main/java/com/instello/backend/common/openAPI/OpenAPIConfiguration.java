package com.instello.backend.common.openAPI;

import com.instello.backend.common.web.ApiVersions;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Paths;
import io.swagger.v3.oas.models.info.Info;
import org.springdoc.core.customizers.GlobalOpenApiCustomizer;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

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

    /// **API Docs:** /api-docs/v0.1
    @Bean
    GroupedOpenApi groupedOpenApi_V0_1(){
        return GroupedOpenApi
                .builder()
                .group("v" + ApiVersions.V0_1)
                .packagesToScan("com.instello.backend")
                .build();
    }

    @Bean
    GlobalOpenApiCustomizer versionedPathCustomizer() {
        String versionPrefix = "/v" + ApiVersions.V0_1;
        String placeholder = "/v{version}";

        return openApi -> {
            Paths paths = openApi.getPaths();
            if (paths == null || paths.isEmpty()) {
                return;
            }

            Paths rewritten = new Paths();

            paths.forEach((path, item) -> rewritten.addPathItem(toPublicPath(path, placeholder, versionPrefix), item));

            if (paths.getExtensions() != null) {
                paths.getExtensions().forEach(rewritten::addExtension);
            }

            openApi.setPaths(rewritten);
        };
    }

    private static String toPublicPath(String path, String placeholder, String versionPrefix) {
        if (path.startsWith(placeholder)) {
            return versionPrefix + path.substring(placeholder.length());
        }
        return path;
    }
}
