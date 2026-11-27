package in.instello.module.academicPattern;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import in.instello.GlobalExceptionHandler;
import in.instello.module.academicPattern.dto.AcademicPatternResponse;
import in.instello.module.academicPattern.entity.AcademicSystemType;
import in.instello.module.academicPattern.service.AcademicPatternService;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class AcademicPatternControllerTest {

  private static final String ROOT = "/academic-patterns/org";
  private static final String EMAIL = "owner@example.com";

  private final AcademicPatternService service = mock(AcademicPatternService.class);
  private final MockMvc mvc =
      MockMvcBuilders.standaloneSetup(new AcademicPatternController(service))
          .setControllerAdvice(new GlobalExceptionHandler())
          .build();
  private final UsernamePasswordAuthenticationToken authentication =
      UsernamePasswordAuthenticationToken.authenticated(EMAIL, null, List.of());

  @Test
  void createReturnsLocationAndLockState() throws Exception {
    Instant now = Instant.parse("2026-01-01T00:00:00Z");
    when(service.create(any(), any(), any()))
        .thenReturn(
            new AcademicPatternResponse(
                "pattern",
                "org",
                "Engineering",
                null,
                4,
                AcademicSystemType.SEMESTER,
                false,
                true,
                List.of(),
                now,
                now));

    mvc.perform(
            post(ROOT)
                .principal(authentication)
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {"name":"Engineering","durationInYears":4,"systemType":"SEMESTER"}
                    """))
        .andExpect(status().isCreated())
        .andExpect(header().string("Location", "http://localhost" + ROOT + "/pattern"))
        .andExpect(jsonPath("$.locked").value(false));
  }

  @Test
  void deleteReturnsNoContent() throws Exception {
    mvc.perform(delete(ROOT + "/pattern").principal(authentication))
        .andExpect(status().isNoContent());

    verify(service).delete("org", "pattern", EMAIL);
  }

  @Test
  void patchRejectsStructuralFieldsBeforeCallingTheService() throws Exception {
    mvc.perform(
            patch(ROOT + "/pattern")
                .principal(authentication)
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {"name":"New name","durationInYears":null}
                    """))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.code").value("BAD_REQUEST"));

    verifyNoInteractions(service);
  }
}
