package com.instello.backend.common.web;

import java.util.List;

import org.springframework.context.annotation.Configuration;
import org.springframework.http.server.RequestPath;
import org.springframework.web.method.HandlerTypePredicate;
import org.springframework.web.servlet.config.annotation.ApiVersionConfigurer;
import org.springframework.web.servlet.config.annotation.PathMatchConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfiguration implements WebMvcConfigurer {

    private static final String API_BASE_PACKAGE = "com.instello.backend";

    // Served outside the /{version} prefix, so their first segment must not be parsed as a version
    private static final List<String> UNVERSIONED_PATHS = List.of("/error", "/v3/api-docs", "/scalar");

    @Override
    public void configureApiVersioning(ApiVersionConfigurer configurer) {
        configurer
                .usePathSegment(0, WebConfiguration::isVersioned)
                .setVersionRequired(false)
                .addSupportedVersions(ApiVersions.V1);
    }

    @Override
    public void configurePathMatch(PathMatchConfigurer configurer) {
        configurer.addPathPrefix("/{version}", HandlerTypePredicate.forBasePackage(API_BASE_PACKAGE));
    }

    private static boolean isVersioned(RequestPath path) {
        String value = path.pathWithinApplication().value();
        return UNVERSIONED_PATHS.stream()
                .noneMatch(prefix -> value.equals(prefix) || value.startsWith(prefix + "/"));
    }
}
