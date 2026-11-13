package in.instello.config.web;

import in.instello.InstelloApplication;
import java.util.List;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.server.RequestPath;
import org.springframework.web.method.HandlerTypePredicate;
import org.springframework.web.servlet.config.annotation.ApiVersionConfigurer;
import org.springframework.web.servlet.config.annotation.PathMatchConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfiguration implements WebMvcConfigurer {

  private static final List<String> UNVERSIONED_PATHS = List.of("/error", "/api-docs");

  @Override
  public void configureApiVersioning(ApiVersionConfigurer configurer) {
    configurer
        .usePathSegment(0, WebConfiguration::isVersioned)
        .setVersionRequired(false)
        .addSupportedVersions(ApiVersions.V1)
        .setDefaultVersion(ApiVersions.V1);
  }

  @Override
  public void configurePathMatch(PathMatchConfigurer configurer) {
    configurer.addPathPrefix(
        "/v{version}",
        HandlerTypePredicate.forBasePackage(InstelloApplication.class.getPackageName()));
  }

  private static boolean isVersioned(RequestPath requestPath) {
    String value = requestPath.pathWithinApplication().value();

    return UNVERSIONED_PATHS.stream()
        .noneMatch(prefix -> value.equals(prefix) || value.startsWith(prefix + "/"));
  }
}
