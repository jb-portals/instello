package com.instello.backend.common;

import com.instello.backend.accessRequest.AccessRequestController;
import com.instello.backend.accessRequest.AccessRequestService;
import com.instello.backend.accessRequest.dto.CreateAccessRequestResponse;
import com.instello.backend.accessRequest.entity.AccessRequestStatus;
import com.instello.backend.auth.AuthController;
import com.instello.backend.auth.LoginService;
import com.instello.backend.common.exception.GlobalExceptionHandler;
import com.instello.backend.common.security.ApiAccessDeniedHandler;
import com.instello.backend.common.security.ApiAuthenticationEntryPoint;
import com.instello.backend.common.security.ApiErrorWriter;
import com.instello.backend.user.UserController;
import com.instello.backend.user.UserService;
import com.instello.backend.user.dto.CurrentUserResponse;
import io.github.thibaultmeyer.cuid.CUID;
import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.JacksonJsonHttpMessageConverter;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.security.web.csrf.DefaultCsrfToken;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;
import tools.jackson.databind.json.JsonMapper;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class ApiResponseStandardTest {

    @Mock
    private AccessRequestService accessRequestService;

    @Mock
    private UserService userService;

    @Mock
    private SecurityContextRepository securityContextRepository;

    @Mock
    private LoginService loginService;

    private MockMvc mockMvc;
    private ApiErrorWriter apiErrorWriter;

    @BeforeEach
    void setUp() {
        JsonMapper jsonMapper = JsonMapper.builder().build();
        apiErrorWriter = new ApiErrorWriter(jsonMapper);

        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();

        mockMvc = MockMvcBuilders
                .standaloneSetup(
                        new AuthController(securityContextRepository, loginService),
                        new AccessRequestController(accessRequestService),
                        new UserController(userService)
                )
                .setControllerAdvice(new GlobalExceptionHandler())
                .setValidator(validator)
                .setCustomArgumentResolvers(csrfTokenResolver(), authenticationPrincipalResolver())
                .setMessageConverters(new JacksonJsonHttpMessageConverter(jsonMapper))
                .build();
    }

    @Test
    void csrfTokenReturnsBareResourceWithoutEnvelope() throws Exception {
        mockMvc.perform(get("/auth/csrf-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("test-csrf-token"))
                .andExpect(jsonPath("$.headerName").value("X-CSRF-TOKEN"))
                .andExpect(jsonPath("$.parameterName").value("_csrf"))
                .andExpect(jsonPath("$.data").doesNotExist())
                .andExpect(jsonPath("$.message").doesNotExist())
                .andExpect(jsonPath("$.code").doesNotExist());
    }

    @Test
    void createAccessRequestReturnsResourceWithLocation() throws Exception {
        String id = CUID.fromString("l4pto4mg2wo6usf7tvz8wvuk").toString();
        when(accessRequestService.submit(any(), anyString()))
                .thenReturn(new CreateAccessRequestResponse(id, AccessRequestStatus.PENDING));

        String body = """
                {
                  "organizationName": "Acme",
                  "firstName": "Ada",
                  "lastName": "Lovelace",
                  "email": "ada@example.com",
                  "phone": "+1 555 0100",
                  "address": {
                    "addressLine": "1 Main St",
                    "city": "London",
                    "state": "ENG",
                    "country": "GB"
                  }
                }
                """;

        mockMvc.perform(post("/access-requests")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", containsString("/v0.1/access-requests/" + id)))
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.data").doesNotExist())
                .andExpect(jsonPath("$.message").doesNotExist());
    }

    @Test
    void validationFailureReturnsAllFieldDetails() throws Exception {
        mockMvc.perform(post("/access-requests")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("validation_failed"))
                .andExpect(jsonPath("$.error.message").value("One or more fields are invalid"))
                .andExpect(jsonPath("$.error.target").value(nullValue()))
                .andExpect(jsonPath("$.error.details", hasSize(greaterThanOrEqualTo(5))))
                .andExpect(jsonPath("$.error.details[?(@.target == 'email')]").isNotEmpty())
                .andExpect(jsonPath("$.code").doesNotExist())
                .andExpect(jsonPath("$.status").doesNotExist())
                .andExpect(jsonPath("$.timestamp").doesNotExist());
    }

    @Test
    void authenticationEntryPointWritesUnauthorizedApiError() throws Exception {
        ApiAuthenticationEntryPoint entryPoint = new ApiAuthenticationEntryPoint(apiErrorWriter);
        MockHttpServletResponse response = new MockHttpServletResponse();

        entryPoint.commence(
                new MockHttpServletRequest("GET", "/v0.1/users/me"),
                response,
                new InsufficientAuthenticationException("Full authentication is required")
        );

        assertThat(response.getStatus()).isEqualTo(401);
        assertThat(response.getContentAsString())
                .contains("\"code\":\"unauthorized\"")
                .contains("\"message\":\"Authentication is required\"")
                .doesNotContain("\"timestamp\"");
    }

    @Test
    void accessDeniedHandlerWritesForbiddenApiError() throws Exception {
        ApiAccessDeniedHandler handler = new ApiAccessDeniedHandler(apiErrorWriter);
        MockHttpServletResponse response = new MockHttpServletResponse();

        handler.handle(
                new MockHttpServletRequest("GET", "/v0.1/access-requests"),
                response,
                new AccessDeniedException("Access Denied")
        );

        assertThat(response.getStatus()).isEqualTo(403);
        assertThat(response.getContentAsString())
                .contains("\"code\":\"forbidden\"")
                .contains("\"message\":\"Access is denied\"");
    }

    @Test
    void authenticatedMeReturnsBareCurrentUser() throws Exception {
        String id = CUID.fromString("q4u5b8y7en4zh2jif5w8vy8w").toString();
        when(userService.getCurrentUser("admin@example.com"))
                .thenReturn(new CurrentUserResponse(id, "admin@example.com", null, List.of(), null));

        mockMvc.perform(get("/users/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.email").value("admin@example.com"))
                .andExpect(jsonPath("$.data").doesNotExist())
                .andExpect(jsonPath("$.message").doesNotExist());
    }

    private static HandlerMethodArgumentResolver csrfTokenResolver() {
        return new HandlerMethodArgumentResolver() {
            @Override
            public boolean supportsParameter(@NonNull MethodParameter parameter) {
                return CsrfToken.class.isAssignableFrom(parameter.getParameterType());
            }

            @Override
            public Object resolveArgument(
                    @NonNull MethodParameter parameter,
                    ModelAndViewContainer mavContainer,
                    @NonNull NativeWebRequest webRequest,
                    WebDataBinderFactory binderFactory
            ) {
                return new DefaultCsrfToken("X-CSRF-TOKEN", "_csrf", "test-csrf-token");
            }
        };
    }

    private static HandlerMethodArgumentResolver authenticationPrincipalResolver() {
        return new HandlerMethodArgumentResolver() {
            @Override
            public boolean supportsParameter(@NonNull MethodParameter parameter) {
                return parameter.hasParameterAnnotation(AuthenticationPrincipal.class);
            }

            @Override
            public Object resolveArgument(
                    @NonNull MethodParameter parameter,
                    ModelAndViewContainer mavContainer,
                    @NonNull NativeWebRequest webRequest,
                    WebDataBinderFactory binderFactory
            ) {
                return "admin@example.com";
            }
        };
    }
}
