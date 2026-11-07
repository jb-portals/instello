package com.instello.backend.institution;

import com.instello.backend.common.entity.dto.Patch;
import com.instello.backend.institution.dto.CreateInstitutionRequest;
import com.instello.backend.institution.dto.CreateInstitutionResponse;
import com.instello.backend.institution.dto.InstitutionResponse;
import com.instello.backend.institution.dto.UpdateInstitutionDetailsRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/organizations/{organizationId}/institutions")
@Tag(name = "organizations/institutions", description = "Institution management")
public class InstitutionController {

    private final InstitutionService institutionService;

    public InstitutionController(InstitutionService institutionService) {
        this.institutionService = institutionService;
    }

    @PostMapping
    @Operation(operationId = "createInstitution")
    public ResponseEntity<CreateInstitutionResponse> create(@PathVariable String organizationId, @RequestBody CreateInstitutionRequest createInstitutionRequest){
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(institutionService.create(createInstitutionRequest, organizationId));
    }

    @GetMapping("{institutionId}")
    @Operation(operationId = "getInstitution")
    public ResponseEntity<InstitutionResponse> get(@PathVariable String organizationId, @PathVariable String institutionId){
        return ResponseEntity.ok(institutionService.get(organizationId, institutionId));
    }

    @PatchMapping("{institutionId}")
    @Operation(operationId = "updateInstitutionDetails")
    public ResponseEntity<Map<String,String>> update(
            @RequestBody @Validated(Patch.class)
            UpdateInstitutionDetailsRequest updateInstitutionDetails,

            @PathVariable String organizationId,
            @PathVariable String institutionId
    ){
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).body(Map.of("message","Not implemented"));
    }

    @PatchMapping("{institutionId}/slug")
    @Operation(operationId = "updateInstitutionDetails")
    public ResponseEntity<Map<String,String>> updateSlug(
            @RequestBody @Validated
            UpdateInstitutionDetailsRequest updateInstitutionDetails,

            @PathVariable String organizationId,
            @PathVariable String institutionId
    ){
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).body(Map.of("message","Not implemented"));
    }

}
