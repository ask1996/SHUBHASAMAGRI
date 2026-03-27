package com.shubhasamagri.controller;

import com.shubhasamagri.dto.request.OccasionRequest;
import com.shubhasamagri.dto.response.ApiResponse;
import com.shubhasamagri.dto.response.OccasionResponse;
import com.shubhasamagri.service.OccasionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

/**
 * CRUD APIs for Hindu occasions (Marriage, Gruha Pravesh, etc.)
 * GET endpoints are public; POST/PUT/DELETE require ADMIN role.
 */
@RestController
@RequestMapping("/api/occasions")
@RequiredArgsConstructor
@Tag(name = "Occasions", description = "Manage Hindu occasions and events")
public class OccasionController {

    private final OccasionService occasionService;

    @GetMapping
    @Operation(summary = "Get all active occasions", description = "Returns all occasions shown on the home page")
    public ResponseEntity<ApiResponse<List<OccasionResponse>>> getAllOccasions() {
        return ResponseEntity.ok(ApiResponse.success("Occasions fetched successfully",
                occasionService.getAllOccasions()));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get occasion by ID")
    public ResponseEntity<ApiResponse<OccasionResponse>> getOccasionById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Occasion fetched successfully",
                occasionService.getOccasionById(id)));
    }

    @PostMapping
    @Operation(summary = "Create occasion (Admin)", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse<OccasionResponse>> createOccasion(
            @Valid @RequestBody OccasionRequest request) {
        OccasionResponse response = occasionService.createOccasion(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Occasion created successfully", response));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update occasion (Admin)", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse<OccasionResponse>> updateOccasion(
            @PathVariable Long id, @Valid @RequestBody OccasionRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Occasion updated successfully",
                occasionService.updateOccasion(id, request)));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete occasion (Admin)", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse<Void>> deleteOccasion(@PathVariable Long id) {
        occasionService.deleteOccasion(id);
        return ResponseEntity.ok(ApiResponse.success("Occasion deactivated successfully", null));
    }
}
