package com.shubhasamagri.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PoojaItemResponse {
    private Long id;
    private String name;
    private String description;
    private String unit;
    private String imageUrl;
    private Boolean isAvailable;
}
