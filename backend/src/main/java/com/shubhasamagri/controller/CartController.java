package com.shubhasamagri.controller;

import com.shubhasamagri.dto.request.AddToCartRequest;
import com.shubhasamagri.dto.response.ApiResponse;
import com.shubhasamagri.dto.response.CartResponse;
import com.shubhasamagri.entity.User;
import com.shubhasamagri.exception.ResourceNotFoundException;
import com.shubhasamagri.repository.UserRepository;
import com.shubhasamagri.service.CartService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

/**
 * Cart management APIs. All endpoints require JWT authentication.
 *
 * The authenticated user's ID is extracted from the JWT token via SecurityContext.
 */
@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
@Tag(name = "Cart", description = "Shopping cart management")
@SecurityRequirement(name = "bearerAuth")
public class CartController {

    private final CartService cartService;
    private final UserRepository userRepository;

    @GetMapping
    @Operation(summary = "Get current user's cart")
    public ResponseEntity<ApiResponse<CartResponse>> getCart() {
        Long userId = getCurrentUserId();
        return ResponseEntity.ok(ApiResponse.success("Cart fetched successfully",
                cartService.getCart(userId)));
    }

    @PostMapping("/add")
    @Operation(summary = "Add a kit to cart (or increase quantity if already present)")
    public ResponseEntity<ApiResponse<CartResponse>> addToCart(
            @Valid @RequestBody AddToCartRequest request) {
        Long userId = getCurrentUserId();
        return ResponseEntity.ok(ApiResponse.success("Kit added to cart",
                cartService.addToCart(userId, request)));
    }

    @PutMapping("/{cartItemId}")
    @Operation(summary = "Update quantity of a cart item")
    public ResponseEntity<ApiResponse<CartResponse>> updateCartItem(
            @PathVariable Long cartItemId,
            @RequestParam Integer quantity) {
        Long userId = getCurrentUserId();
        return ResponseEntity.ok(ApiResponse.success("Cart updated",
                cartService.updateCartItem(userId, cartItemId, quantity)));
    }

    @DeleteMapping("/{cartItemId}")
    @Operation(summary = "Remove a specific item from cart")
    public ResponseEntity<ApiResponse<CartResponse>> removeFromCart(@PathVariable Long cartItemId) {
        Long userId = getCurrentUserId();
        return ResponseEntity.ok(ApiResponse.success("Item removed from cart",
                cartService.removeFromCart(userId, cartItemId)));
    }

    @DeleteMapping("/clear")
    @Operation(summary = "Clear all items from cart")
    public ResponseEntity<ApiResponse<Void>> clearCart() {
        Long userId = getCurrentUserId();
        cartService.clearCart(userId);
        return ResponseEntity.ok(ApiResponse.success("Cart cleared", null));
    }

    /** Extract authenticated user's ID from JWT via SecurityContext */
    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return user.getId();
    }
}
