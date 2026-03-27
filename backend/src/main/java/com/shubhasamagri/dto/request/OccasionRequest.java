package com.shubhasamagri.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class OccasionRequest {

    @NotBlank(message = "Occasion name is required")
    @Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
    private String name;

    private String description;
    private String imageUrl;
    private Boolean isActive = true;
}
