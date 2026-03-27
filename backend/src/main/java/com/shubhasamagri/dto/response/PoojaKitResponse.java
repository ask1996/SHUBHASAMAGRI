package com.shubhasamagri.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PoojaKitResponse {
    private Long id;
    private String name;
    private String description;
    private Long occasionId;
    private String occasionName;
    private BigDecimal price;
    private String imageUrl;
    private Integer estimatedDeliveryDays;
    private Boolean isActive;
    private List<KitItemResponse> kitItems;
    private LocalDateTime createdAt;
}
