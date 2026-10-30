package com.instello.backend.academicPattern;

import java.util.List;


import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.instello.backend.academicPattern.dto.AcademicPatternResponse;
import com.instello.backend.academicPattern.dto.AcademicStageResponse;
import com.instello.backend.academicPattern.dto.UpdateAcademicPatternRequest;
import com.instello.backend.academicPattern.dto.UpdateAcademicStageRequest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/organizations/{organizationId}/academic-patterns")
@Tag(name = "academicPatterns")
public class AcademicPatternController {

    private final AcademicPatternService academicPatternService;

    public AcademicPatternController(AcademicPatternService academicPatternService) {
        this.academicPatternService = academicPatternService;
    }

    @GetMapping
    @Operation(operationId = "listAcademicPatterns")
    public ResponseEntity<List<AcademicPatternResponse>> list(
            @PathVariable String organizationId,
            Authentication authentication
    ) {
        return ResponseEntity.ok(academicPatternService.list(organizationId, authentication.getName()));
    }

    @GetMapping("/{patternId}")
    @Operation(operationId = "getAcademicPattern")
    public ResponseEntity<AcademicPatternResponse> get(
            @PathVariable String organizationId,
            @PathVariable String patternId,
            Authentication authentication
    ) {
        return ResponseEntity.ok(academicPatternService.get(organizationId, patternId, authentication.getName()));
    }

    @PatchMapping("/{patternId}")
    @Operation(operationId = "updateAcademicPattern")
    public ResponseEntity<AcademicPatternResponse> update(
            @PathVariable String organizationId,
            @PathVariable String patternId,
            @Valid @RequestBody UpdateAcademicPatternRequest requestBody,
            Authentication authentication
    ) {
        return ResponseEntity.ok(academicPatternService.update(
                organizationId,
                patternId,
                requestBody,
                authentication.getName()
        ));
    }

    @PatchMapping("/{patternId}/stages/{stageId}")
    @Operation(operationId = "updateAcademicStage")
    public ResponseEntity<AcademicStageResponse> updateStage(
            @PathVariable String organizationId,
            @PathVariable String patternId,
            @PathVariable String stageId,
            @Valid @RequestBody UpdateAcademicStageRequest requestBody,
            Authentication authentication
    ) {
        return ResponseEntity.ok(academicPatternService.updateStage(
                organizationId,
                patternId,
                stageId,
                requestBody,
                authentication.getName()
        ));
    }
}
