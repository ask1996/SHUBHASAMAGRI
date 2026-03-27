package com.shubhasamagri.service;

import com.shubhasamagri.dto.request.PoojaKitRequest;
import com.shubhasamagri.dto.response.PoojaKitResponse;
import com.shubhasamagri.entity.*;
import com.shubhasamagri.exception.ResourceNotFoundException;
import com.shubhasamagri.mapper.PoojaKitMapper;
import com.shubhasamagri.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.ArrayList;
import java.util.List;

/**
 * Business logic for managing Pooja Kits.
 * Kits are curated collections of pooja items for specific occasions.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class PoojaKitService {

    private final PoojaKitRepository poojaKitRepository;
    private final OccasionRepository occasionRepository;
    private final PoojaItemRepository poojaItemRepository;
    private final PoojaKitMapper poojaKitMapper;

    /** Get all active kits across all occasions */
    public List<PoojaKitResponse> getAllKits() {
        return poojaKitMapper.toResponseList(poojaKitRepository.findByIsActiveTrue());
    }

    /** Get a specific kit by ID with all its items */
    public PoojaKitResponse getKitById(Long id) {
        PoojaKit kit = findKitById(id);
        return poojaKitMapper.toResponse(kit);
    }

    /** Get all kits for a specific occasion */
    public List<PoojaKitResponse> getKitsByOccasion(Long occasionId) {
        // Verify occasion exists
        if (!occasionRepository.existsById(occasionId)) {
            throw new ResourceNotFoundException("Occasion", occasionId);
        }
        List<PoojaKit> kits = poojaKitRepository.findByOccasionIdAndIsActiveTrue(occasionId);
        return poojaKitMapper.toResponseList(kits);
    }

    /** Create a new pooja kit with its items (admin only) */
    @Transactional
    public PoojaKitResponse createKit(PoojaKitRequest request) {
        Occasion occasion = occasionRepository.findById(request.getOccasionId())
                .orElseThrow(() -> new ResourceNotFoundException("Occasion", request.getOccasionId()));

        PoojaKit kit = PoojaKit.builder()
                .name(request.getName())
                .description(request.getDescription())
                .occasion(occasion)
                .price(request.getPrice())
                .imageUrl(request.getImageUrl())
                .estimatedDeliveryDays(request.getEstimatedDeliveryDays() != null
                        ? request.getEstimatedDeliveryDays() : 3)
                .isActive(true)
                .build();

        kit = poojaKitRepository.save(kit);

        // Add kit items if provided
        if (request.getKitItems() != null && !request.getKitItems().isEmpty()) {
            List<KitItem> kitItems = new ArrayList<>();
            for (PoojaKitRequest.KitItemRequest itemReq : request.getKitItems()) {
                PoojaItem poojaItem = poojaItemRepository.findById(itemReq.getPoojaItemId())
                        .orElseThrow(() -> new ResourceNotFoundException("PoojaItem", itemReq.getPoojaItemId()));
                KitItem kitItem = KitItem.builder()
                        .poojaKit(kit)
                        .poojaItem(poojaItem)
                        .quantity(itemReq.getQuantity())
                        .unit(itemReq.getUnit())
                        .build();
                kitItems.add(kitItem);
            }
            kit.setKitItems(kitItems);
            kit = poojaKitRepository.save(kit);
        }

        log.info("Created pooja kit: {} for occasion: {}", kit.getName(), occasion.getName());
        return poojaKitMapper.toResponse(kit);
    }

    private PoojaKit findKitById(Long id) {
        return poojaKitRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("PoojaKit", id));
    }
}
