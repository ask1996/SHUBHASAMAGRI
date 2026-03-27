package com.shubhasamagri.service;

import com.shubhasamagri.dto.request.OccasionRequest;
import com.shubhasamagri.dto.response.OccasionResponse;
import com.shubhasamagri.entity.Occasion;
import com.shubhasamagri.exception.ResourceNotFoundException;
import com.shubhasamagri.mapper.OccasionMapper;
import com.shubhasamagri.repository.OccasionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

/**
 * Business logic for managing Hindu occasions (Marriage, Gruha Pravesh, etc.)
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class OccasionService {

    private final OccasionRepository occasionRepository;
    private final OccasionMapper occasionMapper;

    /** Get all active occasions for the home page */
    public List<OccasionResponse> getAllOccasions() {
        log.debug("Fetching all active occasions");
        return occasionMapper.toResponseList(occasionRepository.findByIsActiveTrue());
    }

    /** Get occasion by ID */
    public OccasionResponse getOccasionById(Long id) {
        Occasion occasion = findOccasionById(id);
        return occasionMapper.toResponse(occasion);
    }

    /** Create a new occasion (admin only) */
    @Transactional
    public OccasionResponse createOccasion(OccasionRequest request) {
        Occasion occasion = Occasion.builder()
                .name(request.getName())
                .description(request.getDescription())
                .imageUrl(request.getImageUrl())
                .isActive(request.getIsActive() != null ? request.getIsActive() : true)
                .build();
        occasion = occasionRepository.save(occasion);
        log.info("Created occasion: {}", occasion.getName());
        return occasionMapper.toResponse(occasion);
    }

    /** Update an existing occasion (admin only) */
    @Transactional
    public OccasionResponse updateOccasion(Long id, OccasionRequest request) {
        Occasion occasion = findOccasionById(id);
        occasion.setName(request.getName());
        occasion.setDescription(request.getDescription());
        if (request.getImageUrl() != null) occasion.setImageUrl(request.getImageUrl());
        if (request.getIsActive() != null) occasion.setIsActive(request.getIsActive());
        occasion = occasionRepository.save(occasion);
        log.info("Updated occasion: {}", occasion.getName());
        return occasionMapper.toResponse(occasion);
    }

    /** Soft delete (deactivate) an occasion */
    @Transactional
    public void deleteOccasion(Long id) {
        Occasion occasion = findOccasionById(id);
        occasion.setIsActive(false);
        occasionRepository.save(occasion);
        log.info("Deactivated occasion: {}", occasion.getName());
    }

    private Occasion findOccasionById(Long id) {
        return occasionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Occasion", id));
    }
}
