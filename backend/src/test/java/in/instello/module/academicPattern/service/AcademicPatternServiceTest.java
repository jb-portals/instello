package in.instello.module.academicPattern.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import in.instello.common.exceptions.ConflictException;
import in.instello.common.exceptions.ForbiddenException;
import in.instello.common.exceptions.ResourceNotFoundException;
import in.instello.module.academicPattern.dto.AcademicPatternResponse;
import in.instello.module.academicPattern.dto.AcademicStageResponse;
import in.instello.module.academicPattern.dto.CreateAcademicPatternRequest;
import in.instello.module.academicPattern.dto.UpdateAcademicPatternRequest;
import in.instello.module.academicPattern.entity.AcademicPattern;
import in.instello.module.academicPattern.entity.AcademicStage;
import in.instello.module.academicPattern.entity.AcademicSystemType;
import in.instello.module.academicPattern.repository.AcademicPatternRepository;
import in.instello.module.academicPattern.repository.AcademicStageRepository;
import in.instello.module.institution.repository.InstitutionRepository;
import in.instello.module.organization.OrganizationMembershipService;
import in.instello.module.organization.entity.Organization;
import in.instello.module.organization.repository.OrganizationRepository;
import java.sql.SQLException;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.hibernate.exception.ConstraintViolationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.json.JsonMapper;

@ExtendWith(MockitoExtension.class)
class AcademicPatternServiceTest {

  private static final String ORGANIZATION_ID = "organization";
  private static final String PATTERN_ID = "pattern";
  private static final String EMAIL = "owner@example.com";
  private static final Instant TIME = Instant.parse("2026-01-01T00:00:00Z");

  @Mock private AcademicPatternRepository patterns;
  @Mock private AcademicStageRepository stageRepository;
  @Mock private InstitutionRepository institutions;
  @Mock private OrganizationRepository organizations;
  @Mock private OrganizationMembershipService membership;

  private final Organization organization = Organization.builder().id(ORGANIZATION_ID).build();
  private AcademicStageService stageService;
  private AcademicPatternService service;

  @BeforeEach
  void setUp() {
    stageService = new AcademicStageService(stageRepository);
    service =
        new AcademicPatternService(patterns, stageService, institutions, organizations, membership);
  }

  @Test
  void seedsDefaultPatternsAndSkipsExistingDefaultsOnRetry() {
    when(patterns.save(any())).thenAnswer(call -> call.getArgument(0));
    when(patterns.existsByOrganization_IdAndName(ORGANIZATION_ID, "Engineering"))
        .thenReturn(false, true);
    when(patterns.existsByOrganization_IdAndName(ORGANIZATION_ID, "Diploma"))
        .thenReturn(false, true);
    when(stageRepository.saveAllAndFlush(anyList()))
        .thenAnswer(
            call -> {
              List<AcademicStage> stages = call.getArgument(0);
              AcademicPattern pattern = stages.getFirst().getAcademicPattern();
              int expectedYears = pattern.getName().equals("Engineering") ? 4 : 3;
              assertThat(pattern.getDurationInYears()).isEqualTo(expectedYears);
              assertThat(stages).hasSize(expectedYears * 2);
              assertThat(stages.getFirst())
                  .satisfies(
                      stage -> {
                        assertThat(stage.getName()).isEqualTo("Sem 1");
                        assertThat(stage.getAlias()).isEqualTo("s1");
                        assertThat(stage.getYearNumber()).isEqualTo(1);
                      });
              assertThat(stages.getLast().getYearNumber()).isEqualTo(expectedYears);
              return stages;
            });

    AcademicPatternSeedService seedService = new AcademicPatternSeedService(patterns, stageService);
    seedService.seedDefaults(organization);
    seedService.seedDefaults(organization);

    verify(stageRepository, org.mockito.Mockito.times(2)).saveAllAndFlush(anyList());
  }

  @Test
  void ownerCreatesAnUnlockedAnnualPatternWithOrderedStages() {
    when(organizations.getReferenceById(ORGANIZATION_ID)).thenReturn(organization);
    when(patterns.saveAndFlush(any()))
        .thenAnswer(
            call -> {
              AcademicPattern pattern = call.getArgument(0);
              pattern.setId(PATTERN_ID);
              pattern.setCreatedAt(TIME);
              pattern.setUpdatedAt(TIME);
              return pattern;
            });
    when(stageRepository.saveAllAndFlush(anyList())).thenAnswer(call -> call.getArgument(0));

    AcademicPatternResponse response =
        service.create(
            ORGANIZATION_ID,
            new CreateAcademicPatternRequest(" Annual ", " ", 2, AcademicSystemType.ANNUAL),
            EMAIL);

    verify(membership).requireOwner(ORGANIZATION_ID, EMAIL);
    assertThat(response.name()).isEqualTo("Annual");
    assertThat(response.description()).isNull();
    assertThat(response.locked()).isFalse();
    assertThat(response.canBeEdited()).isTrue();
    assertThat(response.stages())
        .extracting(
            AcademicStageResponse::name,
            AcademicStageResponse::alias,
            AcademicStageResponse::sequenceNumber,
            AcademicStageResponse::yearNumber)
        .containsExactly(tuple("Year 1", "y1", 1, 1), tuple("Year 2", "y2", 2, 2));
  }

  @Test
  void duplicateNameIsRejectedBeforeInsertingStages() {
    when(patterns.existsByOrganization_IdAndName(ORGANIZATION_ID, "Engineering")).thenReturn(true);

    assertThatThrownBy(
            () ->
                service.create(
                    ORGANIZATION_ID,
                    new CreateAcademicPatternRequest(
                        "Engineering", null, 4, AcademicSystemType.SEMESTER),
                    EMAIL))
        .isInstanceOf(ConflictException.class);

    verify(patterns, never()).saveAndFlush(any());
    verify(stageRepository, never()).saveAllAndFlush(anyList());
  }

  @Test
  void listUsesBulkAdoptionLookupAndCanonicalizesLegacyStageLabels() {
    AcademicPattern pattern = pattern();
    AcademicStage stage =
        AcademicStage.builder()
            .id("legacy")
            .academicPattern(pattern)
            .name("Semester 1")
            .alias("Sem 1")
            .sequenceNumber(1)
            .yearNumber(1)
            .createdAt(TIME)
            .updatedAt(TIME)
            .build();
    when(patterns.findByOrganization_IdOrderByNameAsc(ORGANIZATION_ID))
        .thenReturn(List.of(pattern));
    when(institutions.findReferencedAcademicPatternIds(List.of(PATTERN_ID)))
        .thenReturn(Set.of(PATTERN_ID));
    when(stageRepository.findByAcademicPattern_IdOrderBySequenceNumberAsc(PATTERN_ID))
        .thenReturn(List.of(stage));

    AcademicPatternResponse response = service.list(ORGANIZATION_ID, EMAIL).getFirst();

    verify(membership).requireMember(ORGANIZATION_ID, EMAIL);
    verify(institutions, never()).existsByAcademicPattern_Id(any());
    assertThat(response.locked()).isTrue();
    assertThat(response.canBeEdited()).isFalse();
    assertThat(response.stages())
        .extracting(
            AcademicStageResponse::id, AcademicStageResponse::name, AcademicStageResponse::alias)
        .containsExactly(tuple("legacy", "Sem 1", "s1"));
  }

  @Test
  void scopedLookupDoesNotReturnAnotherOrganizationsPattern() {
    assertThatThrownBy(() -> service.get("another-organization", PATTERN_ID, EMAIL))
        .isInstanceOf(ResourceNotFoundException.class);
    verify(membership).requireMember("another-organization", EMAIL);
    verify(patterns).findByIdAndOrganization_Id(PATTERN_ID, "another-organization");
  }

  @Test
  void ownerCanUpdateLockedPatternDescriptionAndLockTracksCurrentReferences() {
    AcademicPattern pattern = stubPattern();
    when(institutions.existsByAcademicPattern_Id(PATTERN_ID)).thenReturn(true, false);

    AcademicPatternResponse updated =
        service.update(
            ORGANIZATION_ID,
            PATTERN_ID,
            new UpdateAcademicPatternRequest(null, " Updated "),
            EMAIL);

    verify(membership).requireOwner(ORGANIZATION_ID, EMAIL);
    assertThat(updated.description()).isEqualTo("Updated");
    assertThat(updated.durationInYears()).isEqualTo(4);
    assertThat(updated.systemType()).isEqualTo(AcademicSystemType.SEMESTER);
    assertThat(updated.locked()).isTrue();
    assertThat(service.get(ORGANIZATION_ID, PATTERN_ID, EMAIL).locked()).isFalse();
    verify(patterns).saveAndFlush(pattern);
  }

  @Test
  void lockedPatternCannotBeDeletedAndUnlockedPatternDeletesItsStagesFirst() {
    AcademicPattern pattern = stubPattern();
    when(institutions.existsByAcademicPattern_Id(PATTERN_ID)).thenReturn(true, false);

    assertThatThrownBy(() -> service.delete(ORGANIZATION_ID, PATTERN_ID, EMAIL))
        .isInstanceOf(ConflictException.class);
    verify(stageRepository, never()).deleteAllForPattern(PATTERN_ID);

    service.delete(ORGANIZATION_ID, PATTERN_ID, EMAIL);

    var order = org.mockito.Mockito.inOrder(stageRepository, patterns);
    order.verify(stageRepository).deleteAllForPattern(PATTERN_ID);
    order.verify(patterns).delete(pattern);
    order.verify(patterns).flush();
  }

  @Test
  void adoptionForeignKeyFailureIsReportedAsLockedConflict() {
    stubPattern();
    doThrow(
            new DataIntegrityViolationException(
                "delete failed",
                new ConstraintViolationException(
                    "foreign key", new SQLException("foreign key", "23503"), "pattern_fk")))
        .when(patterns)
        .flush();

    assertThatThrownBy(() -> service.delete(ORGANIZATION_ID, PATTERN_ID, EMAIL))
        .isInstanceOf(ConflictException.class)
        .hasMessageContaining("locked");
  }

  @Test
  void forbiddenMutationStopsBeforePatternLookup() {
    doThrow(new ForbiddenException("Owner required"))
        .when(membership)
        .requireOwner(ORGANIZATION_ID, EMAIL);

    assertThatThrownBy(() -> service.delete(ORGANIZATION_ID, PATTERN_ID, EMAIL))
        .isInstanceOf(ForbiddenException.class);
    verify(patterns, never()).findByIdAndOrganization_Id(any(), any());
  }

  @Test
  void patchRejectsProtectedFieldsEvenWhenNull() {
    JsonMapper mapper = JsonMapper.builder().build();
    assertThat(mapper.readValue("{\"name\":\"New\"}", UpdateAcademicPatternRequest.class).name())
        .isEqualTo("New");
    for (String field :
        List.of(
            "durationInYears",
            "systemType",
            "totalStagesInYear",
            "locked",
            "organizationId",
            "stages")) {
      String payload = "{\"name\":\"New\",\"" + field + "\":null}";
      assertThatThrownBy(() -> mapper.readValue(payload, UpdateAcademicPatternRequest.class))
          .isInstanceOf(JacksonException.class)
          .hasMessageContaining(field);
    }
  }

  private AcademicPattern stubPattern() {
    AcademicPattern pattern = pattern();
    when(patterns.findByIdAndOrganization_Id(PATTERN_ID, ORGANIZATION_ID))
        .thenReturn(Optional.of(pattern));
    return pattern;
  }

  private AcademicPattern pattern() {
    return AcademicPattern.builder()
        .id(PATTERN_ID)
        .organization(organization)
        .name("Engineering")
        .durationInYears(4)
        .systemType(AcademicSystemType.SEMESTER)
        .createdAt(TIME)
        .updatedAt(TIME)
        .build();
  }
}
