package in.instello.config.openAPI;

import in.instello.InstelloApplication;
import in.instello.config.web.ApiVersions;
import io.swagger.v3.oas.models.Paths;
import org.springdoc.core.customizers.GlobalOpenApiCustomizer;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenAPIConfiguration {

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
