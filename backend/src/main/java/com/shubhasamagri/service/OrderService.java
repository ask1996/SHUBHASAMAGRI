package com.shubhasamagri.service;

import com.shubhasamagri.dto.request.PlaceOrderRequest;
import com.shubhasamagri.dto.response.OrderResponse;
import com.shubhasamagri.entity.*;
import com.shubhasamagri.exception.BadRequestException;
import com.shubhasamagri.exception.ResourceNotFoundException;
import com.shubhasamagri.mapper.OrderMapper;
import com.shubhasamagri.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Manages order lifecycle: placement, retrieval, cancellation.
 *
 * Order Flow:
 *   1. User places order (cart items → order + order items)
 *   2. Cart is cleared after successful order
 *   3. Price is snapshotted at order time (immutable)
 *   4. Status transitions: PENDING → CONFIRMED → SHIPPED → DELIVERED
 *   5. PENDING orders can be cancelled
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class OrderService {

    private final OrderRepository orderRepository;
    private final CartItemRepository cartItemRepository;
    private final UserRepository userRepository;
    private final OrderMapper orderMapper;

    /**
     * Place a new order from the user's cart.
     * Atomically creates order + items and clears cart (single transaction).
     */
    @Transactional
    public OrderResponse placeOrder(Long userId, PlaceOrderRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));

        List<CartItem> cartItems = cartItemRepository.findByUserId(userId);
        if (cartItems.isEmpty()) {
            throw new BadRequestException("Cannot place order: cart is empty. Add items first.");
        }

        // Calculate total amount
        BigDecimal totalAmount = cartItems.stream()
                .map(item -> item.getPoojaKit().getPrice()
                        .multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Build the order
        Order order = Order.builder()
                .user(user)
                .status(Order.OrderStatus.PENDING)
                .totalAmount(totalAmount)
                .deliveryAddress(request.getDeliveryAddress())
                .phone(request.getPhone())
                .build();

        order = orderRepository.save(order);
        final Order savedOrder = order;

        // Build order items (snapshot price at order time)
        List<OrderItem> orderItems = cartItems.stream()
                .map(cartItem -> OrderItem.builder()
                        .order(savedOrder)
                        .poojaKit(cartItem.getPoojaKit())
                        .quantity(cartItem.getQuantity())
                        .priceAtOrder(cartItem.getPoojaKit().getPrice())
                        .build())
                .collect(Collectors.toList());

        order.setOrderItems(orderItems);
        order = orderRepository.save(order);

        // Clear the cart after successful order placement
        cartItemRepository.deleteByUserId(userId);

        log.info("Order placed: #{} for user: {} | Amount: Rs.{}", order.getId(), user.getEmail(), totalAmount);
        return orderMapper.toResponse(order);
    }

    /** Get all orders for a user, newest first */
    public List<OrderResponse> getOrdersByUser(Long userId) {
        return orderMapper.toResponseList(
                orderRepository.findByUserIdOrderByCreatedAtDesc(userId));
    }

    /** Get a specific order (must belong to the requesting user) */
    public OrderResponse getOrderById(Long userId, Long orderId) {
        Order order = orderRepository.findByIdAndUserId(orderId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + orderId));
        return orderMapper.toResponse(order);
    }

    /** Cancel a PENDING order */
    @Transactional
    public OrderResponse cancelOrder(Long userId, Long orderId) {
        Order order = orderRepository.findByIdAndUserId(orderId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + orderId));

        if (order.getStatus() != Order.OrderStatus.PENDING) {
            throw new BadRequestException("Only PENDING orders can be cancelled. " +
                    "Current status: " + order.getStatus());
        }

        order.setStatus(Order.OrderStatus.CANCELLED);
        order = orderRepository.save(order);
        log.info("Order #{} cancelled by user {}", orderId, userId);
        return orderMapper.toResponse(order);
    }
}
