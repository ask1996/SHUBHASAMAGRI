package com.shubhasamagri.service;

import com.shubhasamagri.dto.request.AddToCartRequest;
import com.shubhasamagri.dto.response.CartItemResponse;
import com.shubhasamagri.dto.response.CartResponse;
import com.shubhasamagri.entity.CartItem;
import com.shubhasamagri.entity.PoojaKit;
import com.shubhasamagri.entity.User;
import com.shubhasamagri.exception.BadRequestException;
import com.shubhasamagri.exception.ResourceNotFoundException;
import com.shubhasamagri.mapper.PoojaKitMapper;
import com.shubhasamagri.repository.CartItemRepository;
import com.shubhasamagri.repository.PoojaKitRepository;
import com.shubhasamagri.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Manages user shopping cart operations.
 * Cart is persisted in DB tied to user account (survives session).
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class CartService {

    private final CartItemRepository cartItemRepository;
    private final UserRepository userRepository;
    private final PoojaKitRepository poojaKitRepository;
    private final PoojaKitMapper poojaKitMapper;

    /** Get user's current cart with computed totals */
    public CartResponse getCart(Long userId) {
        List<CartItem> cartItems = cartItemRepository.findByUserId(userId);
        return buildCartResponse(cartItems);
    }

    /**
     * Add a kit to cart or update quantity if already exists.
     * If the kit is already in cart, quantity is incremented.
     */
    @Transactional
    public CartResponse addToCart(Long userId, AddToCartRequest request) {
        User user = findUserById(userId);
        PoojaKit kit = findKitById(request.getKitId());

        if (!kit.getIsActive()) {
            throw new BadRequestException("This kit is currently unavailable.");
        }

        // Check if kit already in cart - update quantity if so
        Optional<CartItem> existingItem = cartItemRepository
                .findByUserIdAndPoojaKitId(userId, request.getKitId());

        if (existingItem.isPresent()) {
            CartItem item = existingItem.get();
            int newQuantity = item.getQuantity() + request.getQuantity();
            if (newQuantity > 10) throw new BadRequestException("Maximum 10 of same kit allowed in cart.");
            item.setQuantity(newQuantity);
            cartItemRepository.save(item);
        } else {
            CartItem cartItem = CartItem.builder()
                    .user(user)
                    .poojaKit(kit)
                    .quantity(request.getQuantity())
                    .build();
            cartItemRepository.save(cartItem);
        }

        log.debug("Added kit {} to cart for user {}", kit.getName(), userId);
        return getCart(userId);
    }

    /** Update quantity of a specific cart item */
    @Transactional
    public CartResponse updateCartItem(Long userId, Long cartItemId, Integer quantity) {
        CartItem cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new ResourceNotFoundException("CartItem", cartItemId));

        // Security: ensure this cart item belongs to the requesting user
        if (!cartItem.getUser().getId().equals(userId)) {
            throw new BadRequestException("Cart item does not belong to this user.");
        }

        if (quantity <= 0) {
            cartItemRepository.delete(cartItem);
        } else {
            cartItem.setQuantity(quantity);
            cartItemRepository.save(cartItem);
        }

        return getCart(userId);
    }

    /** Remove a specific item from cart */
    @Transactional
    public CartResponse removeFromCart(Long userId, Long cartItemId) {
        CartItem cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new ResourceNotFoundException("CartItem", cartItemId));

        if (!cartItem.getUser().getId().equals(userId)) {
            throw new BadRequestException("Cart item does not belong to this user.");
        }

        cartItemRepository.delete(cartItem);
        log.debug("Removed cart item {} for user {}", cartItemId, userId);
        return getCart(userId);
    }

    /** Clear entire cart (called after order placement) */
    @Transactional
    public void clearCart(Long userId) {
        cartItemRepository.deleteByUserId(userId);
        log.debug("Cleared cart for user {}", userId);
    }

    /** Build CartResponse DTO with computed subtotals and totals */
    private CartResponse buildCartResponse(List<CartItem> cartItems) {
        List<CartItemResponse> itemResponses = cartItems.stream()
                .map(item -> CartItemResponse.builder()
                        .id(item.getId())
                        .poojaKit(poojaKitMapper.toResponse(item.getPoojaKit()))
                        .quantity(item.getQuantity())
                        .subtotal(item.getPoojaKit().getPrice()
                                .multiply(BigDecimal.valueOf(item.getQuantity())))
                        .addedAt(item.getAddedAt())
                        .build())
                .collect(Collectors.toList());

        BigDecimal totalAmount = itemResponses.stream()
                .map(CartItemResponse::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        int totalItems = cartItems.stream()
                .mapToInt(CartItem::getQuantity)
                .sum();

        return CartResponse.builder()
                .items(itemResponses)
                .totalItems(totalItems)
                .totalAmount(totalAmount)
                .build();
    }

    private User findUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));
    }

    private PoojaKit findKitById(Long kitId) {
        return poojaKitRepository.findById(kitId)
                .orElseThrow(() -> new ResourceNotFoundException("PoojaKit", kitId));
    }
}
