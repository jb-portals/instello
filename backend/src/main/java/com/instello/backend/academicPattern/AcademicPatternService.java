package com.instello.backend.academicPattern;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;


import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
import com.instello.backend.common.exception.ResourceNotFoundException;
import com.instello.backend.organization.OrganizationMembershipService;
import com.instello.backend.organization.entity.Organization;

@Service
public class AcademicPatternService {

    static final String LOCKED_MESSAGE = "Pattern structure is locked because an institution uses this pattern";

    private static final List<Template> TEMPLATES = List.of(
            new Template(
                    AcademicTemplateKey.ENGINEERING,
                    "Engineering",
                    "4-year semester-based engineering program",
                    4
            ),
            new Template(
                    AcademicTemplateKey.DIPLOMA,
                    "Diploma",
                    "3-year semester-based diploma program",
                    3
            )
    );

    private final AcademicPatternRepository patternRepository;
    private final AcademicStageRepository stageRepository;
    private final AcademicPatternAllocationRepository allocationRepository;
    private final OrganizationMembershipService membershipService;

    public AcademicPatternService(
            AcademicPatternRepository patternRepository,
            AcademicStageRepository stageRepository,
            AcademicPatternAllocationRepository allocationRepository,
            OrganizationMembershipService membershipService
    ) {
        this.patternRepository = patternRepository;
        this.stageRepository = stageRepository;
        this.allocationRepository = allocationRepository;
        this.membershipService = membershipService;
    }

    @Transactional
    public void seedDefaults(Organization organization) {
        for (Template template : TEMPLATES) {
            AcademicPattern pattern = patternRepository.save(AcademicPattern.builder()
                    .organization(organization)
                    .name(template.name())
                    .description(template.description())
                    .durationInYears(template.durationInYears())
                    .systemType(AcademicSystemType.SEMESTER)
                    .templateKey(template.key())
                    .build());

            int stageCount = template.durationInYears() * 2;
            for (int sequence = 1; sequence <= stageCount; sequence++) {
                stageRepository.save(AcademicStage.builder()
                        .academicPattern(pattern)
                        .name("Semester " + sequence)
                        .alias("Sem " + sequence)
                        .sequenceNumber(sequence)
                        .yearNumber((sequence + 1) / 2)
                        .build());
            }
        }
    }

    @Transactional(readOnly = true)
    public List<AcademicPatternResponse> list(String organizationId, String email) {
        membershipService.requireMember(organizationId, email);
        List<AcademicPattern> patterns = patternRepository.findByOrganization_IdOrderByNameAsc(organizationId);
        if (patterns.isEmpty()) {
            return List.of();
        }
        Set<String> allocated = allocationRepository.findAllocatedPatternIds(
                patterns.stream().map(AcademicPattern::getId).toList()
        );
        return patterns.stream()
                .map(pattern -> toResponse(pattern, !allocated.contains(pattern.getId())))
                .toList();
    }

    @Transactional(readOnly = true)
    public AcademicPatternResponse get(String organizationId, String patternId, String email) {
        membershipService.requireMember(organizationId, email);
        AcademicPattern pattern = findPattern(organizationId, patternId);
        return toResponse(pattern, !allocationRepository.existsByAcademicPattern_Id(pattern.getId()));
    }

    @Transactional
    public AcademicPatternResponse update(
            String organizationId,
            String patternId,
            UpdateAcademicPatternRequest request,
            String email
    ) {
        membershipService.requireMember(organizationId, email);
        AcademicPattern pattern = findPattern(organizationId, patternId);
        List<AcademicStage> stages = stagesOf(pattern);

        int duration = request.durationInYears() != null ? request.durationInYears() : pattern.getDurationInYears();
        AcademicSystemType systemType = request.systemType() != null ? request.systemType() : pattern.getSystemType();
        boolean structuralChange = duration != pattern.getDurationInYears() || systemType != pattern.getSystemType();
        boolean allocated = allocationRepository.existsByAcademicPattern_Id(pattern.getId());
        if (allocated && structuralChange) {
            throw new ConflictException(LOCKED_MESSAGE);
        }
        if (structuralChange) {
            assertStagesFit(systemType, duration, yearsOf(stages));
        }

        if (request.name() != null) {
            pattern.setName(requiredText(request.name(), "Name"));
        }
        if (request.description() != null) {
            pattern.setDescription(blankToNull(request.description()));
        }
        pattern.setDurationInYears(duration);
        pattern.setSystemType(systemType);

        return toResponse(pattern, stages, !allocated);
    }

    @Transactional
    public AcademicStageResponse updateStage(
            String organizationId,
            String patternId,
            String stageId,
            UpdateAcademicStageRequest request,
            String email
    ) {
        membershipService.requireMember(organizationId, email);
        AcademicPattern pattern = findPattern(organizationId, patternId);
        AcademicStage stage = stageRepository.findByIdAndAcademicPattern_Id(stageId, pattern.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Academic stage not found"));
        List<AcademicStage> stages = stagesOf(pattern);

        int sequence = request.sequenceNumber() != null ? request.sequenceNumber() : stage.getSequenceNumber();
        int year = request.yearNumber() != null ? request.yearNumber() : stage.getYearNumber();
        boolean structuralChange = sequence != stage.getSequenceNumber() || year != stage.getYearNumber();
        boolean allocated = allocationRepository.existsByAcademicPattern_Id(pattern.getId());
        if (allocated && structuralChange) {
            throw new ConflictException(LOCKED_MESSAGE);
        }
        if (structuralChange) {
            if (stages.stream().anyMatch(other -> !other.getId().equals(stage.getId())
                    && other.getSequenceNumber() == sequence)) {
                throw new BadRequestException("Sequence number is already used");
            }
            assertStagesFit(pattern.getSystemType(), pattern.getDurationInYears(), yearsReplacing(stages, stage.getId(), year));
        }

        if (request.name() != null) {
            stage.setName(requiredText(request.name(), "Name"));
        }
        if (request.alias() != null) {
            stage.setAlias(requiredText(request.alias(), "Alias"));
        }
        if (request.description() != null) {
            stage.setDescription(blankToNull(request.description()));
        }
        stage.setSequenceNumber(sequence);
        stage.setYearNumber(year);

        return toStage(stage);
    }

    private AcademicPattern findPattern(String organizationId, String patternId) {
        return patternRepository.findByIdAndOrganization_Id(patternId, organizationId)
                .orElseThrow(() -> new ResourceNotFoundException("Academic pattern not found"));
    }

    private List<AcademicStage> stagesOf(AcademicPattern pattern) {
        return stageRepository.findByAcademicPattern_IdOrderBySequenceNumberAsc(pattern.getId());
    }

    private AcademicPatternResponse toResponse(AcademicPattern pattern, boolean canBeEdited) {
        return toResponse(pattern, stagesOf(pattern), canBeEdited);
    }

    private AcademicPatternResponse toResponse(
            AcademicPattern pattern,
            List<AcademicStage> stages,
            boolean canBeEdited
    ) {
        return new AcademicPatternResponse(
                pattern.getId(),
                pattern.getOrganization().getId(),
                pattern.getName(),
                pattern.getDescription(),
                pattern.getDurationInYears(),
                pattern.getSystemType(),
                pattern.getTemplateKey(),
                canBeEdited,
                stages.stream().map(this::toStage).toList(),
                pattern.getCreatedAt(),
                pattern.getUpdatedAt()
        );
    }

    private AcademicStageResponse toStage(AcademicStage stage) {
        return new AcademicStageResponse(
                stage.getId(),
                stage.getName(),
                stage.getAlias(),
                stage.getDescription(),
                stage.getSequenceNumber(),
                stage.getYearNumber(),
                stage.getCreatedAt(),
                stage.getUpdatedAt()
        );
    }

    /**
     * Semester patterns hold at most two stages in a year. Annual patterns hold one.
     * Every stage year must sit inside the duration.
     */
    private static void assertStagesFit(AcademicSystemType systemType, int duration, List<Integer> years) {
        int maxPerYear = systemType == AcademicSystemType.ANNUAL ? 1 : 2;
        Map<Integer, Integer> perYear = new HashMap<>();
        for (Integer year : years) {
            if (year < 1 || year > duration) {
                throw new BadRequestException("Duration must cover every stage year");
            }
            perYear.merge(year, 1, Integer::sum);
        }
        if (perYear.values().stream().anyMatch(count -> count > maxPerYear)) {
            throw new BadRequestException("Stages do not fit the selected system type");
        }
    }

    private static List<Integer> yearsOf(List<AcademicStage> stages) {
        return stages.stream().map(AcademicStage::getYearNumber).toList();
    }

    private static List<Integer> yearsReplacing(List<AcademicStage> stages, String stageId, int year) {
        List<Integer> years = new ArrayList<>();
        for (AcademicStage stage : stages) {
            years.add(stage.getId().equals(stageId) ? year : stage.getYearNumber());
        }
        return years;
    }

    private static String requiredText(String value, String field) {
        String trimmed = value.trim();
        if (trimmed.isEmpty()) {
            throw new BadRequestException(field + " must not be blank");
        }
        return trimmed;
    }

    private static String blankToNull(String value) {
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private record Template(
            AcademicTemplateKey key,
            String name,
            String description,
            int durationInYears
    ) {}
}
