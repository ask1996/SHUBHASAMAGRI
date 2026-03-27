package com.shubhasamagri.controller;

import com.shubhasamagri.dto.request.PoojaKitRequest;
import com.shubhasamagri.dto.response.ApiResponse;
import com.shubhasamagri.dto.response.PoojaKitResponse;
import com.shubhasamagri.service.PoojaKitService;
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
 * APIs for browsing and managing Pooja Kits.
 * GET endpoints are public; POST requires ADMIN role.
 */
@RestController
@RequestMapping("/api/kits")
@RequiredArgsConstructor
@Tag(name = "Pooja Kits", description = "Browse and manage pooja kits curated by poojaris")
public class PoojaKitController {

    private final PoojaKitService poojaKitService;

    @GetMapping
    @Operation(summary = "Get all active kits across all occasions")
    public ResponseEntity<ApiResponse<List<PoojaKitResponse>>> getAllKits() {
        return ResponseEntity.ok(ApiResponse.success("Kits fetched successfully",
                poojaKitService.getAllKits()));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get kit details by ID (includes all items)")
    public ResponseEntity<ApiResponse<PoojaKitResponse>> getKitById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Kit fetched successfully",
                poojaKitService.getKitById(id)));
    }

    @GetMapping("/occasion/{occasionId}")
    @Operation(summary = "Get all kits for a specific occasion")
    public ResponseEntity<ApiResponse<List<PoojaKitResponse>>> getKitsByOccasion(
            @PathVariable Long occasionId) {
        return ResponseEntity.ok(ApiResponse.success("Kits fetched successfully",
                poojaKitService.getKitsByOccasion(occasionId)));
    }

    @PostMapping
    @Operation(summary = "Create a new kit (Admin)", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse<PoojaKitResponse>> createKit(
            @Valid @RequestBody PoojaKitRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Kit created successfully",
                        poojaKitService.createKit(request)));
    }
}
