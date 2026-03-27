package com.shubhasamagri.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
public class PoojaKitRequest {

    @NotBlank(message = "Kit name is required")
    private String name;

    private String description;

    @NotNull(message = "Occasion ID is required")
    private Long occasionId;

    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.01", message = "Price must be greater than 0")
    private BigDecimal price;

    private String imageUrl;
    private Integer estimatedDeliveryDays = 3;
    private List<KitItemRequest> kitItems;

    @Data
    public static class KitItemRequest {
        @NotNull private Long poojaItemId;
        @NotNull @Min(1) private Integer quantity;
        private String unit;
    }
}
