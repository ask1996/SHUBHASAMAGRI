package com.shubhasamagri.mapper;

import com.shubhasamagri.dto.response.OccasionResponse;
import com.shubhasamagri.entity.Occasion;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Maps Occasion entity to OccasionResponse DTO.
 * Manual mapping is used to keep dependencies minimal and mappings explicit.
 */
@Component
public class OccasionMapper {

    public OccasionResponse toResponse(Occasion occasion) {
        if (occasion == null) return null;
        return OccasionResponse.builder()
                .id(occasion.getId())
                .name(occasion.getName())
                .description(occasion.getDescription())
                .imageUrl(occasion.getImageUrl())
                .isActive(occasion.getIsActive())
                .createdAt(occasion.getCreatedAt())
                .build();
    }

    public List<OccasionResponse> toResponseList(List<Occasion> occasions) {
        return occasions.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }
}
