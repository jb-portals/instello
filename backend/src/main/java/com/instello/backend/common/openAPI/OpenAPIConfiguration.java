package com.instello.backend.common.openAPI;

import java.util.List;
import java.util.regex.Pattern;

import org.springdoc.core.customizers.GlobalOpenApiCustomizer;
import org.springdoc.core.models.GroupedOpenApi;
import org.springdoc.core.utils.SpringDocUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.web.csrf.CsrfToken;

import com.instello.backend.common.web.ApiVersions;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.Paths;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;

@Configuration
public class OpenAPIConfiguration {

    static final String SESSION_COOKIE = "sessionCookie";
    static final String CSRF_TOKEN = "csrfToken";

    private static final String VERSION_SEGMENT = "/{version}/";
    private static final String VERSION_VARIABLE = "version";

    // The first segment is either the {version} placeholder or an already resolved version such as v1
    private static final List<PublicOperation> PUBLIC_OPERATIONS = List.of(
            new PublicOperation(PathItem.HttpMethod.POST, Pattern.compile("^/[^/]+/auth/.*$")),
            new PublicOperation(PathItem.HttpMethod.GET, Pattern.compile("^/[^/]+/auth/csrf-token$")),
            new PublicOperation(PathItem.HttpMethod.POST, Pattern.compile("^/[^/]+/access-requests$"))
    );

    static {
        SpringDocUtils.getConfig().addRequestWrapperToIgnore(CsrfToken.class);
    }

    @Bean
    OpenAPI instelloOpenAPI(@Value("${spring.application.version}") String version) {
        return new OpenAPI()
                .info(new Info()
                        .title("Instello API")
                        .description("""
                                Instello ERP backend API.

                                Authenticated endpoints use the session cookie set by the admin OTP login. \
                                State-changing requests also need the CSRF token from GET /v1/auth/csrf-token \
                                in the X-CSRF-TOKEN header.""")
                        .version(version))

                .components(new Components()
                        .addSecuritySchemes(SESSION_COOKIE, new SecurityScheme()
                                .type(SecurityScheme.Type.APIKEY)
                                .in(SecurityScheme.In.COOKIE)
                                .name("SESSION")
                                .description("Session cookie issued after admin OTP verification"))
                        .addSecuritySchemes(CSRF_TOKEN, new SecurityScheme()
                                .type(SecurityScheme.Type.APIKEY)
                                .in(SecurityScheme.In.HEADER)
                                .name("X-CSRF-TOKEN")
                                .description("Token returned by GET /v1/auth/csrf-token")));
    }

    @Bean
    GroupedOpenApi v1Api() {
        return versionedGroup("v" + ApiVersions.V1, "API v1");
    }

    @Bean
    GlobalOpenApiCustomizer securityRequirementsCustomizer() {
        return openApi -> {
            if (openApi.getPaths() == null) {
                return;
            }
            openApi.getPaths().forEach((path, pathItem) ->
                    pathItem.readOperationsMap().forEach((method, operation) -> {
                        SecurityRequirement requirement = new SecurityRequirement();
                        if (!isPublic(method, path)) {
                            requirement.addList(SESSION_COOKIE);
                        }
                        if (method != PathItem.HttpMethod.GET && method != PathItem.HttpMethod.HEAD) {
                            requirement.addList(CSRF_TOKEN);
                        }
                        if (!requirement.isEmpty()) {
                            operation.setSecurity(List.of(requirement));
                        }
                    })
            );
        };
    }

    private static GroupedOpenApi versionedGroup(String pathVersion, String displayName) {
        return GroupedOpenApi.builder()
                .group(pathVersion)
                .displayName(displayName)
                .pathsToMatch(VERSION_SEGMENT + "**")
                .addOpenApiCustomizer(openApi -> resolveVersionPaths(openApi, pathVersion))
                .build();
    }

    private static void resolveVersionPaths(OpenAPI openApi, String pathVersion) {
        if (openApi.getPaths() == null) {
            return;
        }
        Paths resolved = new Paths();
        openApi.getPaths().forEach((path, pathItem) -> {
            pathItem.readOperations().forEach(operation -> {
                if (operation.getParameters() != null) {
                    operation.getParameters().removeIf(OpenAPIConfiguration::isVersionParameter);
                    if (operation.getParameters().isEmpty()) {
                        operation.setParameters(null);
                    }
                }
            });
            resolved.addPathItem(path.replace(VERSION_SEGMENT, "/" + pathVersion + "/"), pathItem);
        });
        openApi.setPaths(resolved);
    }

    private static boolean isVersionParameter(Parameter parameter) {
        return "path".equals(parameter.getIn()) && VERSION_VARIABLE.equals(parameter.getName());
    }

    private static boolean isPublic(PathItem.HttpMethod method, String path) {
        return PUBLIC_OPERATIONS.stream()
                .anyMatch(operation -> operation.method() == method && operation.path().matcher(path).matches());
    }

    private record PublicOperation(PathItem.HttpMethod method, Pattern path) {
    }
}
