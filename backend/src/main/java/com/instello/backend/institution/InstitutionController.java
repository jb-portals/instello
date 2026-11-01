package com.instello.backend.institution;

import com.instello.backend.institution.dto.CreateInstitutionRequest;
import com.instello.backend.institution.dto.CreateInstitutionResponse;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/organization/{organizationId}/institutions")
public class InstitutionController {

    private final InstitutionService institutionService;

    public InstitutionController(InstitutionService institutionService) {
        this.institutionService = institutionService;
    }

    @PostMapping
    @Operation(operationId = "createInstitution")
    public ResponseEntity<CreateInstitutionResponse> create(@PathVariable String organizationId, @RequestBody CreateInstitutionRequest createInstitutionRequest){
        return ResponseEntity.status(HttpStatus.CREATED).body(institutionService.create(createInstitutionRequest, organizationId));
    }

}
