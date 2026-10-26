package com.instello.backend.common.web;

import org.springframework.context.annotation.Configuration;
import org.springframework.http.server.RequestPath;
import org.springframework.web.method.HandlerTypePredicate;
import org.springframework.web.servlet.config.annotation.ApiVersionConfigurer;
import org.springframework.web.servlet.config.annotation.PathMatchConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

@Configuration
public class WebConfiguration implements WebMvcConfigurer {

    private final static List<String> UNVERSIONED_PATHS = List.of("/error", "/api-docs");

    @Override
    public void configureApiVersioning(ApiVersionConfigurer configurer) {
        configurer
                .usePathSegment(0, WebConfiguration::isVersioned)
                .setVersionRequired(false)
                .addSupportedVersions(ApiVersions.V0_1)
                .setDefaultVersion(ApiVersions.V0_1);
    }

    @Override
    public void configurePathMatch(PathMatchConfigurer configurer) {
        configurer.addPathPrefix("/v{version}", HandlerTypePredicate.forBasePackage("com.instello.backend"));
    }

    private static boolean isVersioned(RequestPath requestPath){
        String value = requestPath.pathWithinApplication().value();

        return UNVERSIONED_PATHS.stream()
                .noneMatch(prefix->
                        value.equals(prefix) || value.startsWith(prefix + "/")
                );
    }

}
