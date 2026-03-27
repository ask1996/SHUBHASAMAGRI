package com.shubhasamagri.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KitItemResponse {
    private Long id;
    private PoojaItemResponse poojaItem;
    private Integer quantity;
    private String unit;
}
