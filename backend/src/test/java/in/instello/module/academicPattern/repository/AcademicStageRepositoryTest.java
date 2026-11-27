package in.instello.module.academicPattern.repository;

import static org.assertj.core.api.Assertions.assertThat;

import in.instello.common.address.Address;
import in.instello.module.academicPattern.entity.AcademicPattern;
import in.instello.module.academicPattern.entity.AcademicStage;
import in.instello.module.academicPattern.entity.AcademicSystemType;
import in.instello.module.institution.entity.Institution;
import in.instello.module.organization.entity.Organization;
import in.instello.support.PostgresJpaTest;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

@PostgresJpaTest
class AcademicStageRepositoryTest {

  @Autowired private AcademicStageRepository academicStageRepository;
  @Autowired private EntityManager entityManager;
  @Autowired private JdbcTemplate jdbc;

  @Test
  void findByIdOrAliasInInstitution_ReturnsOnlyStagesInThatInstitution() {
    Organization organization =
        Organization.builder()
            .name("Test organization")
            .email("organization@example.test")
            .phone("9000000000")
            .address(
                Address.builder()
                    .lineOne("1 Main Road")
                    .city("Pune")
                    .state("MH")
                    .postalCode("411001")
                    .build())
            .build();
    entityManager.persist(organization);

    AcademicStage stageA = createStage(organization, "Pattern A");
    AcademicStage stageB = createStage(organization, "Pattern B");
    entityManager.flush();

    createInstitution("test-a", stageA.getAcademicPattern());
    createInstitution("test-b", stageB.getAcademicPattern());
    entityManager.clear();

    Institution institutionA = entityManager.getReference(Institution.class, "test-a");

    assertThat(academicStageRepository.findByIdOrAliasInInstitution(institutionA, stageA.getId()))
        .map(AcademicStage::getId)
        .contains(stageA.getId());
    assertThat(academicStageRepository.findByIdOrAliasInInstitution(institutionA, "s1"))
        .map(AcademicStage::getId)
        .contains(stageA.getId());

    assertThat(academicStageRepository.findByIdOrAliasInInstitution(institutionA, stageB.getId()))
        .isEmpty();
    assertThat(academicStageRepository.findByIdOrAliasInInstitution(institutionA, "missing"))
        .isEmpty();
  }

  private AcademicStage createStage(Organization organization, String name) {
    AcademicPattern pattern =
        AcademicPattern.builder()
            .organization(organization)
            .name(name)
            .durationInYears(1)
            .systemType(AcademicSystemType.SEMESTER)
            .build();
    entityManager.persist(pattern);

    AcademicStage stage =
        AcademicStage.builder()
            .academicPattern(pattern)
            .name("Sem 1")
            .alias("s1")
            .sequenceNumber(1)
            .yearNumber(1)
            .build();
    entityManager.persist(stage);
    return stage;
  }

  private void createInstitution(String id, AcademicPattern pattern) {
    // Insert both sides of the required institution/faculty relationship in one statement.
    jdbc.update(
        """
        WITH institution AS (
          INSERT INTO institutions
            (id, name, slug, code, organization_id, academic_pattern_id, faculty_id,
             line_one, city, state, postal_code, country, created_at, updated_at)
          VALUES (?, ?, ?, ?, ?, ?, ?, '1 Main Road', 'Pune', 'MH', '411001', 'India',
                  CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
          RETURNING id, faculty_id
        )

        INSERT INTO faculty
          (id, institution_id, first_name, date_of_birth, email, staff_id, designation,
           primary_phone_number, active, line_one, city, state, postal_code, country,
           created_at, updated_at)
        SELECT faculty_id, id, 'Test', DATE '1990-01-01', id || '@example.test', id, 'Principal',
               '9000000001', true, '1 Main Road', 'Pune', 'MH', '411001', 'India',
               CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
        FROM institution
        """,
        id,
        id,
        id,
        id,
        pattern.getOrganization().getId(),
        pattern.getId(),
        id + "-faculty");
  }
}
