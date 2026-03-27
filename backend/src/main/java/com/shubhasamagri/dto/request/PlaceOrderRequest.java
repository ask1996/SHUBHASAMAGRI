package com.shubhasamagri.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class PlaceOrderRequest {

    @NotBlank(message = "Delivery address is required")
    @Size(min = 10, max = 500, message = "Address must be between 10 and 500 characters")
    private String deliveryAddress;

    @NotBlank(message = "Phone number is required")
    @Pattern(regexp = "^[0-9]{10}$", message = "Phone must be a valid 10-digit number")
    private String phone;
}
