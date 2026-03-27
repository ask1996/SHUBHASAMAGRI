package com.shubhasamagri.mapper;

import com.shubhasamagri.dto.response.OrderItemResponse;
import com.shubhasamagri.dto.response.OrderResponse;
import com.shubhasamagri.entity.Order;
import com.shubhasamagri.entity.OrderItem;
import org.springframework.stereotype.Component;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Maps Order entity to OrderResponse DTO.
 */
@Component
public class OrderMapper {

    public OrderResponse toResponse(Order order) {
        if (order == null) return null;
        return OrderResponse.builder()
                .id(order.getId())
                .status(order.getStatus().name())
                .totalAmount(order.getTotalAmount())
                .deliveryAddress(order.getDeliveryAddress())
                .phone(order.getPhone())
                .orderItems(mapOrderItems(order.getOrderItems()))
                .createdAt(order.getCreatedAt())
                .updatedAt(order.getUpdatedAt())
                .build();
    }

    public List<OrderResponse> toResponseList(List<Order> orders) {
        return orders.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    private List<OrderItemResponse> mapOrderItems(List<OrderItem> items) {
        if (items == null) return Collections.emptyList();
        return items.stream()
                .map(this::toOrderItemResponse)
                .collect(Collectors.toList());
    }

    private OrderItemResponse toOrderItemResponse(OrderItem item) {
        return OrderItemResponse.builder()
                .id(item.getId())
                .kitId(item.getPoojaKit().getId())
                .kitName(item.getPoojaKit().getName())
                .occasionName(item.getPoojaKit().getOccasion() != null
                        ? item.getPoojaKit().getOccasion().getName() : null)
                .quantity(item.getQuantity())
                .priceAtOrder(item.getPriceAtOrder())
                .subtotal(item.getPriceAtOrder().multiply(
                        java.math.BigDecimal.valueOf(item.getQuantity())))
                .build();
    }
}
