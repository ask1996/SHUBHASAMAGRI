package com.shubhasamagri.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CartItemResponse {
    private Long id;
    private PoojaKitResponse poojaKit;
    private Integer quantity;
    private BigDecimal subtotal;
    private LocalDateTime addedAt;
}
