package com.shubhasamagri.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class AddToCartRequest {

    @NotNull(message = "Kit ID is required")
    private Long kitId;

    @NotNull(message = "Quantity is required")
    @Min(value = 1, message = "Quantity must be at least 1")
    @Max(value = 10, message = "Quantity cannot exceed 10")
    private Integer quantity = 1;
}
