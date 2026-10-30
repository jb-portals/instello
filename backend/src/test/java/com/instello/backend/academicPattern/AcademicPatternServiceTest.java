package com.instello.backend.academicPattern;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import io.github.thibaultmeyer.cuid.CUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.instello.backend.academicPattern.dto.AcademicPatternResponse;
import com.instello.backend.academicPattern.dto.AcademicStageResponse;
import com.instello.backend.academicPattern.dto.UpdateAcademicPatternRequest;
import com.instello.backend.academicPattern.dto.UpdateAcademicStageRequest;
import com.instello.backend.academicPattern.entity.AcademicPattern;
import com.instello.backend.academicPattern.entity.AcademicStage;
import com.instello.backend.academicPattern.entity.AcademicSystemType;
import com.instello.backend.academicPattern.entity.AcademicTemplateKey;
import com.instello.backend.academicPattern.repository.AcademicPatternAllocationRepository;
import com.instello.backend.academicPattern.repository.AcademicPatternRepository;
import com.instello.backend.academicPattern.repository.AcademicStageRepository;
import com.instello.backend.common.exception.BadRequestException;
import com.instello.backend.common.exception.ConflictException;
import com.instello.backend.organization.OrganizationMembershipService;
import com.instello.backend.organization.entity.Organization;

@ExtendWith(MockitoExtension.class)
class AcademicPatternServiceTest {

    private static final String ORGANIZATION_ID = CUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa").toString();
    private static final String ENGINEERING_ID = CUID.fromString("bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb").toString();
    private static final String DIPLOMA_ID = CUID.fromString("cccccccc-cccc-cccc-cccc-cccccccccccc").toString();
    private static final String EMAIL = "owner@example.com";

    @Mock
    private AcademicPatternRepository patternRepository;
    @Mock
    private AcademicStageRepository stageRepository;
    @Mock
    private AcademicPatternAllocationRepository allocationRepository;
    @Mock
    private OrganizationMembershipService membershipService;

    private AcademicPatternService service;

    @BeforeEach
    void setUp() {
        service = new AcademicPatternService(
                patternRepository,
                stageRepository,
                allocationRepository,
                membershipService
        );
    }

    @Test
    void seedDefaultsCreatesEngineeringAndDiploma() {
        Organization organization = organization();
        when(patternRepository.save(any())).thenAnswer(invocation -> {
            AcademicPattern pattern = invocation.getArgument(0);
            pattern.setId(pattern.getTemplateKey() == AcademicTemplateKey.ENGINEERING ? ENGINEERING_ID : DIPLOMA_ID);
            return pattern;
        });
        when(stageRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        service.seedDefaults(organization);

        ArgumentCaptor<AcademicPattern> patterns = ArgumentCaptor.forClass(AcademicPattern.class);
        verify(patternRepository, times(2)).save(patterns.capture());
        AcademicPattern engineering = patterns.getAllValues().getFirst();
        AcademicPattern diploma = patterns.getAllValues().get(1);

        assertThat(engineering.getTemplateKey()).isEqualTo(AcademicTemplateKey.ENGINEERING);
        assertThat(engineering.getName()).isEqualTo("Engineering");
        assertThat(engineering.getSystemType()).isEqualTo(AcademicSystemType.SEMESTER);
        assertThat(engineering.getDurationInYears()).isEqualTo(4);
        assertThat(engineering.getOrganization()).isSameAs(organization);

        assertThat(diploma.getTemplateKey()).isEqualTo(AcademicTemplateKey.DIPLOMA);
        assertThat(diploma.getName()).isEqualTo("Diploma");
        assertThat(diploma.getSystemType()).isEqualTo(AcademicSystemType.SEMESTER);
        assertThat(diploma.getDurationInYears()).isEqualTo(3);

        ArgumentCaptor<AcademicStage> stages = ArgumentCaptor.forClass(AcademicStage.class);
        verify(stageRepository, times(14)).save(stages.capture());
        List<AcademicStage> engineeringStages = stages.getAllValues().stream()
                .filter(stage -> stage.getAcademicPattern().getId().equals(ENGINEERING_ID))
                .toList();
        List<AcademicStage> diplomaStages = stages.getAllValues().stream()
                .filter(stage -> stage.getAcademicPattern().getId().equals(DIPLOMA_ID))
                .toList();

        assertThat(engineeringStages).hasSize(8);
        assertThat(engineeringStages).extracting(AcademicStage::getSequenceNumber).containsExactly(1, 2, 3, 4, 5, 6, 7, 8);
        assertThat(engineeringStages).extracting(AcademicStage::getYearNumber).containsExactly(1, 1, 2, 2, 3, 3, 4, 4);
        assertThat(engineeringStages.getFirst().getName()).isEqualTo("Semester 1");
        assertThat(engineeringStages.getFirst().getAlias()).isEqualTo("Sem 1");
        assertThat(engineeringStages.getLast().getName()).isEqualTo("Semester 8");
        assertThat(engineeringStages.getLast().getYearNumber()).isEqualTo(4);

        assertThat(diplomaStages).hasSize(6);
        assertThat(diplomaStages).extracting(AcademicStage::getYearNumber).containsExactly(1, 1, 2, 2, 3, 3);
        assertThat(diplomaStages.getLast().getName()).isEqualTo("Semester 6");
        assertThat(diplomaStages.getLast().getAlias()).isEqualTo("Sem 6");
    }

    @Test
    void unallocatedUpdatesChangePatternAndStageFields() {
        AcademicPattern pattern = engineering();
        List<AcademicStage> stages = stagesFor(pattern, 8);
        stubPattern(pattern, stages, false);

        AcademicPatternResponse updated = service.update(
                ORGANIZATION_ID,
                pattern.getId(),
                new UpdateAcademicPatternRequest("B.Tech", "Updated description", 5, null),
                EMAIL
        );

        assertThat(updated.name()).isEqualTo("B.Tech");
        assertThat(updated.description()).isEqualTo("Updated description");
        assertThat(updated.durationInYears()).isEqualTo(5);
        assertThat(updated.canBeEdited()).isTrue();
        assertThat(updated.systemType()).isEqualTo(AcademicSystemType.SEMESTER);

        AcademicStage last = stages.getLast();
        when(stageRepository.findByIdAndAcademicPattern_Id(last.getId(), pattern.getId())).thenReturn(Optional.of(last));

        AcademicStageResponse stage = service.updateStage(
                ORGANIZATION_ID,
                pattern.getId(),
                last.getId(),
                new UpdateAcademicStageRequest("Final semester", "Final", "Capstone", 9, 5),
                EMAIL
        );

        assertThat(stage.name()).isEqualTo("Final semester");
        assertThat(stage.alias()).isEqualTo("Final");
        assertThat(stage.description()).isEqualTo("Capstone");
        assertThat(stage.sequenceNumber()).isEqualTo(9);
        assertThat(stage.yearNumber()).isEqualTo(5);
    }

    @Test
    void durationOrSystemTypeThatStagesCannotSatisfyIsRejected() {
        AcademicPattern shrinking = engineering();
        List<AcademicStage> shrinkingStages = stagesFor(shrinking, 8);
        stubPattern(shrinking, shrinkingStages, false);

        assertThatThrownBy(() -> service.update(
                ORGANIZATION_ID,
                shrinking.getId(),
                new UpdateAcademicPatternRequest(null, null, 3, null),
                EMAIL
        ))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("cover");
        assertThat(shrinking.getDurationInYears()).isEqualTo(4);

        AcademicPattern annual = engineering();
        List<AcademicStage> annualStages = stagesFor(annual, 8);
        stubPattern(annual, annualStages, false);

        assertThatThrownBy(() -> service.update(
                ORGANIZATION_ID,
                annual.getId(),
                new UpdateAcademicPatternRequest(null, null, null, AcademicSystemType.ANNUAL),
                EMAIL
        ))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("system type");
        assertThat(annual.getSystemType()).isEqualTo(AcademicSystemType.SEMESTER);
    }

    @Test
    void allocatedPatternAllowsLabelsAndRejectsStructure() {
        AcademicPattern pattern = engineering();
        List<AcademicStage> stages = stagesFor(pattern, 8);
        stubPattern(pattern, stages, true);

        AcademicPatternResponse updated = service.update(
                ORGANIZATION_ID,
                pattern.getId(),
                new UpdateAcademicPatternRequest("Engineering track", "Still editable", null, null),
                EMAIL
        );

        assertThat(updated.name()).isEqualTo("Engineering track");
        assertThat(updated.description()).isEqualTo("Still editable");
        assertThat(updated.canBeEdited()).isFalse();

        assertThatThrownBy(() -> service.update(
                ORGANIZATION_ID,
                pattern.getId(),
                new UpdateAcademicPatternRequest(null, null, 5, null),
                EMAIL
        )).isInstanceOf(ConflictException.class);
        assertThat(pattern.getDurationInYears()).isEqualTo(4);

        assertThatThrownBy(() -> service.update(
                ORGANIZATION_ID,
                pattern.getId(),
                new UpdateAcademicPatternRequest(null, null, null, AcademicSystemType.ANNUAL),
                EMAIL
        )).isInstanceOf(ConflictException.class);
        assertThat(pattern.getSystemType()).isEqualTo(AcademicSystemType.SEMESTER);

        AcademicStage first = stages.getFirst();
        when(stageRepository.findByIdAndAcademicPattern_Id(first.getId(), pattern.getId())).thenReturn(Optional.of(first));

        AcademicStageResponse stage = service.updateStage(
                ORGANIZATION_ID,
                pattern.getId(),
                first.getId(),
                new UpdateAcademicStageRequest("First year", "FY", "Intro", null, null),
                EMAIL
        );
        assertThat(stage.name()).isEqualTo("First year");
        assertThat(stage.alias()).isEqualTo("FY");
        assertThat(stage.description()).isEqualTo("Intro");

        assertThatThrownBy(() -> service.updateStage(
                ORGANIZATION_ID,
                pattern.getId(),
                first.getId(),
                new UpdateAcademicStageRequest(null, null, null, 9, null),
                EMAIL
        )).isInstanceOf(ConflictException.class);
        assertThat(first.getSequenceNumber()).isEqualTo(1);

        assertThatThrownBy(() -> service.updateStage(
                ORGANIZATION_ID,
                pattern.getId(),
                first.getId(),
                new UpdateAcademicStageRequest(null, null, null, null, 2),
                EMAIL
        )).isInstanceOf(ConflictException.class);
        assertThat(first.getYearNumber()).isEqualTo(1);
    }

    private void stubPattern(AcademicPattern pattern, List<AcademicStage> stages, boolean allocated) {
        when(patternRepository.findByIdAndOrganization_Id(pattern.getId(), ORGANIZATION_ID)).thenReturn(Optional.of(pattern));
        when(stageRepository.findByAcademicPattern_IdOrderBySequenceNumberAsc(pattern.getId())).thenReturn(stages);
        when(allocationRepository.existsByAcademicPattern_Id(pattern.getId())).thenReturn(allocated);
    }

    private static Organization organization() {
        Organization organization = Organization.builder()
                .name("Acme")
                .email("acme@example.com")
                .phone("1234567890")
                .build();
        organization.setId(ORGANIZATION_ID);
        return organization;
    }

    private static AcademicPattern engineering() {
        AcademicPattern pattern = AcademicPattern.builder()
                .organization(organization())
                .name("Engineering")
                .description("4-year semester-based engineering program")
                .durationInYears(4)
                .systemType(AcademicSystemType.SEMESTER)
                .templateKey(AcademicTemplateKey.ENGINEERING)
                .build();
        pattern.setId(ENGINEERING_ID);
        return pattern;
    }

    private static List<AcademicStage> stagesFor(AcademicPattern pattern, int count) {
        List<AcademicStage> stages = new ArrayList<>();
        for (int sequence = 1; sequence <= count; sequence++) {
            AcademicStage stage = AcademicStage.builder()
                    .academicPattern(pattern)
                    .name("Semester " + sequence)
                    .alias("Sem " + sequence)
                    .sequenceNumber(sequence)
                    .yearNumber((sequence + 1) / 2)
                    .build();
            stage.setId(CUID.randomCUID2().toString());
            stages.add(stage);
        }
        return stages;
    }
}
