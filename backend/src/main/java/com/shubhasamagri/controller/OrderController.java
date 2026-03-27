package com.shubhasamagri.controller;

import com.shubhasamagri.dto.request.PlaceOrderRequest;
import com.shubhasamagri.dto.response.ApiResponse;
import com.shubhasamagri.dto.response.OrderResponse;
import com.shubhasamagri.entity.User;
import com.shubhasamagri.exception.ResourceNotFoundException;
import com.shubhasamagri.repository.UserRepository;
import com.shubhasamagri.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import java.util.List;

/**
 * Order management APIs. All endpoints require JWT authentication.
 *
 * Supports order placement, retrieval, and cancellation.
 * Future: payment gateway integration hooks can be added in placeOrder.
 */
@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
@Tag(name = "Orders", description = "Order placement and management")
@SecurityRequirement(name = "bearerAuth")
public class OrderController {

    private final OrderService orderService;
    private final UserRepository userRepository;

    @PostMapping
    @Operation(summary = "Place order from cart",
               description = "Creates order from cart items, clears cart. Cart must not be empty.")
    public ResponseEntity<ApiResponse<OrderResponse>> placeOrder(
            @Valid @RequestBody PlaceOrderRequest request) {
        Long userId = getCurrentUserId();
        OrderResponse response = orderService.placeOrder(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Order placed successfully! Your pooja kit is on its way 🙏", response));
    }

    @GetMapping
    @Operation(summary = "Get all orders for current user")
    public ResponseEntity<ApiResponse<List<OrderResponse>>> getMyOrders() {
        Long userId = getCurrentUserId();
        return ResponseEntity.ok(ApiResponse.success("Orders fetched successfully",
                orderService.getOrdersByUser(userId)));
    }

    @GetMapping("/{orderId}")
    @Operation(summary = "Get specific order details")
    public ResponseEntity<ApiResponse<OrderResponse>> getOrderById(@PathVariable Long orderId) {
        Long userId = getCurrentUserId();
        return ResponseEntity.ok(ApiResponse.success("Order fetched successfully",
                orderService.getOrderById(userId, orderId)));
    }

    @PutMapping("/{orderId}/cancel")
    @Operation(summary = "Cancel a PENDING order")
    public ResponseEntity<ApiResponse<OrderResponse>> cancelOrder(@PathVariable Long orderId) {
        Long userId = getCurrentUserId();
        return ResponseEntity.ok(ApiResponse.success("Order cancelled successfully",
                orderService.cancelOrder(userId, orderId)));
    }

    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return user.getId();
    }
}
