package com.shubhasamagri.mapper;

import com.shubhasamagri.dto.response.KitItemResponse;
import com.shubhasamagri.dto.response.PoojaItemResponse;
import com.shubhasamagri.dto.response.PoojaKitResponse;
import com.shubhasamagri.entity.KitItem;
import com.shubhasamagri.entity.PoojaItem;
import com.shubhasamagri.entity.PoojaKit;
import org.springframework.stereotype.Component;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Maps PoojaKit entity to PoojaKitResponse DTO.
 * Includes nested kit items with their individual item details.
 */
@Component
public class PoojaKitMapper {

    public PoojaKitResponse toResponse(PoojaKit kit) {
        if (kit == null) return null;
        return PoojaKitResponse.builder()
                .id(kit.getId())
                .name(kit.getName())
                .description(kit.getDescription())
                .occasionId(kit.getOccasion() != null ? kit.getOccasion().getId() : null)
                .occasionName(kit.getOccasion() != null ? kit.getOccasion().getName() : null)
                .price(kit.getPrice())
                .imageUrl(kit.getImageUrl())
                .estimatedDeliveryDays(kit.getEstimatedDeliveryDays())
                .isActive(kit.getIsActive())
                .kitItems(mapKitItems(kit.getKitItems()))
                .createdAt(kit.getCreatedAt())
                .build();
    }

    public List<PoojaKitResponse> toResponseList(List<PoojaKit> kits) {
        return kits.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    private List<KitItemResponse> mapKitItems(List<KitItem> kitItems) {
        if (kitItems == null) return Collections.emptyList();
        return kitItems.stream()
                .map(this::toKitItemResponse)
                .collect(Collectors.toList());
    }

    private KitItemResponse toKitItemResponse(KitItem kitItem) {
        return KitItemResponse.builder()
                .id(kitItem.getId())
                .poojaItem(toPoojaItemResponse(kitItem.getPoojaItem()))
                .quantity(kitItem.getQuantity())
                .unit(kitItem.getUnit() != null ? kitItem.getUnit() : kitItem.getPoojaItem().getUnit())
                .build();
    }

    private PoojaItemResponse toPoojaItemResponse(PoojaItem item) {
        if (item == null) return null;
        return PoojaItemResponse.builder()
                .id(item.getId())
                .name(item.getName())
                .description(item.getDescription())
                .unit(item.getUnit())
                .imageUrl(item.getImageUrl())
                .isAvailable(item.getIsAvailable())
                .build();
    }
}
