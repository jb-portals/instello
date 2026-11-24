package in.instello.config.openApi;

import in.instello.InstelloApplication;
import in.instello.config.web.ApiVersions;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.security.SecuritySchemes;
import io.swagger.v3.oas.annotations.servers.Server;
import io.swagger.v3.oas.models.Paths;
import org.springdoc.core.customizers.GlobalOpenApiCustomizer;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@SecuritySchemes({
  @SecurityScheme(
      name = "sessionCookie",
      type = SecuritySchemeType.APIKEY,
      in = SecuritySchemeIn.COOKIE,
      paramName = "SESSION",
      description = "Session cookie created after authentication."),
  @SecurityScheme(
      name = "csrfHeader",
      type = SecuritySchemeType.APIKEY,
      in = SecuritySchemeIn.HEADER,
      paramName = "X-CSRF-TOKEN",
      description = "CSRF token returned by the CSRF token endpoint.")
})
@OpenAPIDefinition(
    info =
        @Info(
            title = "Instello API",
            summary = "One Platform. Every Possibility.",
            description = "Manages the complete ERP flows through this api",
            version = "1.0",
            termsOfService =
                """
                        Copyright © 2026 Instello Technologies. All rights reserved.
                        This repository and its contents are proprietary. No license or other permission is granted to use,
                        reproduce, modify, distribute, or create derivative works from any part of this repository without prior
                        written permission from Instello Technologies, except as permitted by applicable law.

                        Third-party components included in this repository may be subject to separate licenses and notices.
                        Their respective terms govern those components.
                        """),
    servers = {
      @Server(description = "Dev server (uses test data)", url = "http://localhost:8080"),
      @Server(description = "Production server (uses live data)", url = "https://api.instello.in")
    })
public class OpenApiConfiguration {

  /// **API Docs:** /api-docs/v1
  @Bean
  GroupedOpenApi groupedOpenApi_V1() {
    return GroupedOpenApi.builder()
        .group("v" + ApiVersions.V1)
        .packagesToScan(InstelloApplication.class.getPackageName())
        .build();
  }

  @Bean
  GlobalOpenApiCustomizer versionedPathCustomizer() {
    String versionPrefix = "/v" + ApiVersions.V1;
    String placeholder = "/v{version}";

    return openApi -> {
      Paths paths = openApi.getPaths();
      if (paths == null || paths.isEmpty()) {
        return;
      }

      Paths rewritten = new Paths();

      paths.forEach(
          (path, item) ->
              rewritten.addPathItem(toPublicPath(path, placeholder, versionPrefix), item));

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
