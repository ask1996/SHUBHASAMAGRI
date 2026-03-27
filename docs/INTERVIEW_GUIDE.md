# ShubhaSamagri - Interview Preparation Guide

## 1. Project Overview (30-second pitch)

> "ShubhaSamagri is a full-stack ecommerce application for pooja essentials. Users can browse Hindu occasions like Marriage or Gruha Pravesh, view curated ritual kits recommended by temple poojaris, add them to cart, and place orders. The backend is Spring Boot 3 with JWT authentication, and the frontend is React 18 with Context API for state management."

---

## 2. Architecture Questions

### Q: Why did you choose this architecture?

**Layered architecture (Controller → Service → Repository → Entity):**
- **Separation of concerns**: Each layer has one job
- **Testability**: Services can be unit-tested by mocking repositories
- **Maintainability**: Changes in DB schema don't ripple to controllers
- **Industry standard**: Aligns with Spring Boot conventions

**DTOs instead of exposing entities directly:**
- Prevents Hibernate lazy-loading exceptions in JSON serialization
- Decouples API contract from DB schema
- Security: never accidentally expose password hash
- Flexibility: API shape can differ from DB shape

### Q: Why Spring Boot + React instead of a monolith?

- **Independent deployment**: Frontend (Netlify) and backend (Render) deploy independently
- **Scalability**: Can scale frontend CDN and backend servers separately
- **Technology flexibility**: Could swap React for Vue/Angular without touching backend
- **Modern industry standard**: Most companies use this pattern

---

## 3. Spring Security & JWT Deep Dive

### Q: Explain the JWT authentication flow step by step.

1. **Login request**: Client sends `{ email, password }` to `POST /api/auth/login`
2. **Spring validates**: `AuthenticationManager.authenticate()` triggers `UserDetailsServiceImpl.loadUserByUsername()` which loads user from DB and BCrypt-compares passwords
3. **Token generated**: `JwtTokenProvider.generateToken()` creates an HMAC-SHA256 signed JWT with email as subject, 24h expiry
4. **Client stores**: Frontend stores token in `localStorage`
5. **Subsequent requests**: Client sends `Authorization: Bearer <token>` header
6. **Filter intercepts**: `JwtAuthenticationFilter.doFilterInternal()` extracts and validates the token
7. **SecurityContext set**: On valid token, `UsernamePasswordAuthenticationToken` is set in `SecurityContextHolder`
8. **Controller accesses user**: `SecurityContextHolder.getContext().getAuthentication().getName()` returns the email

### Q: Why is JWT stateless? Why does it matter?

JWT is stateless because **all user info is encoded in the token itself**. The server doesn't store sessions. This matters because:
- **Horizontal scaling**: Any server instance can validate the token (no session stickiness needed)
- **Performance**: No DB lookup needed to validate session
- **Microservices ready**: Each service can independently validate tokens

### Q: How do you invalidate a JWT token?

JWT tokens can't be "deleted" from the server since they're stateless. Common approaches:
1. **Short expiry** (our approach: 24h) - minimize damage window
2. **Token blacklist** (Redis) - store invalidated tokens until expiry
3. **Refresh token rotation** - short-lived access tokens + long-lived refresh tokens
4. **Version claim** - store a `tokenVersion` in DB, increment on logout/password change

### Q: Why BCrypt for passwords?

BCrypt is specifically designed for password hashing:
- **Adaptive cost factor**: `BCryptPasswordEncoder` defaults to strength 10 (2^10 = 1024 hash rounds)
- **Built-in salt**: Each hash includes a random salt, preventing rainbow table attacks
- **Slow by design**: Computationally expensive to brute-force even with GPUs

---

## 4. Spring Data JPA & Hibernate

### Q: Explain the JPA relationships in this project.

```
User      1──N  CartItem
User      1──N  Order
Occasion  1──N  PoojaKit
PoojaKit  1──N  KitItem
PoojaKit  1──N  CartItem (via kitItems)
PoojaKit  1──N  OrderItem
KitItem   N──1  PoojaItem
Order     1──N  OrderItem
OrderItem N──1  PoojaKit
```

**Key decision - KitItem (junction entity)**:
Instead of `@ManyToMany` between PoojaKit and PoojaItem, I used a junction entity `KitItem` because:
- Need to store **quantity** on the relationship
- Need to store **unit** override per kit context
- `@ManyToMany` with extra columns requires a junction entity anyway (JPA doesn't support it directly)

### Q: What is EAGER vs LAZY loading?

- **LAZY** (default for collections): Related entities loaded only when accessed. Better performance - avoids N+1 queries
- **EAGER**: Related entities loaded immediately with parent. Used when you always need the data.

In this project:
- `PoojaKit.kitItems` is `FetchType.EAGER` because we almost always need items with the kit
- `Order.orderItems` is `FetchType.EAGER` for the same reason
- `CartItem.poojaKit` is `FetchType.EAGER` to compute cart totals

### Q: What is the N+1 query problem?

If you have 10 orders and each order has items loaded lazily, fetching all orders triggers:
- 1 query for orders
- 10 queries for each order's items → **N+1 = 11 queries**

Solutions: `FetchType.EAGER`, `@EntityGraph`, `JOIN FETCH` in JPQL, or batch fetching.

---

## 5. Design Patterns Used

### Repository Pattern
`UserRepository extends JpaRepository` — abstracts data access logic. Controllers never touch SQL.

### DTO Pattern
Request DTOs (`SignupRequest`) validate input. Response DTOs (`AuthResponse`) control what's exposed. Prevents entity leaks.

### Mapper Pattern
`OccasionMapper`, `PoojaKitMapper`, `OrderMapper` — centralized entity-to-DTO conversion. If DTO shape changes, only the mapper changes.

### Builder Pattern
Lombok `@Builder` on entities and DTOs enables readable object construction:
```java
User user = User.builder().name("Ravi").email("ravi@test.com").build();
```

### Factory Method
`ApiResponse.success(message, data)` and `ApiResponse.error(message)` — consistent response wrapping without repetitive builder calls.

### Chain of Responsibility
Spring Security filter chain — `JwtAuthenticationFilter → UsernamePasswordAuthenticationFilter → ...`. Each filter processes or passes the request.

---

## 6. Transaction Management

```java
@Service
@Transactional(readOnly = true)  // Default for all methods (better performance for reads)
public class OrderService {

    @Transactional  // Overrides to read-write for mutations
    public OrderResponse placeOrder(...) {
        // 1. Create order
        // 2. Create order items
        // 3. Delete cart items
        // All in ONE transaction - if any step fails, everything rolls back
    }
}
```

**Why `@Transactional(readOnly = true)` by default?**
- Hibernate skips dirty checking on read-only transactions (performance)
- Some DB drivers optimize read-only transactions

---

## 7. Validation Strategy

**Two layers of validation:**

1. **Bean Validation (input)**: `@Valid` + annotations like `@NotBlank`, `@Email`, `@Size` on DTOs
   - Handled by Spring automatically before controller method executes
   - `MethodArgumentNotValidException` thrown, caught by GlobalExceptionHandler

2. **Business validation (logic)**: In service layer
   - `if (userRepository.existsByEmail(email)) throw new BadRequestException(...)`
   - Empty cart check before order placement

---

## 8. Frontend Architecture Questions

### Q: Why Context API instead of Redux?

For this project's scale, Context API is sufficient:
- **Simplicity**: Less boilerplate than Redux (no actions, reducers, dispatch)
- **Two contexts only**: AuthContext (user state) + CartContext (cart state)
- **React built-in**: No extra dependency

Use Redux when: complex state interactions, time-travel debugging needed, large team with strict patterns.

### Q: Explain the Axios interceptors.

```javascript
// Request interceptor
axiosInstance.interceptors.request.use(config => {
  config.headers.Authorization = `Bearer ${localStorage.getItem('token')}`
  return config
})

// Response interceptor
axiosInstance.interceptors.response.use(
  response => response,
  error => {
    if (error.response?.status === 401) {
      // Token expired - redirect to login
      window.location.href = '/login'
    }
    return Promise.reject(error)
  }
)
```

**Why not add token manually in every request?** DRY principle — single place to change auth mechanism.

---

## 9. Scalability Considerations

### Payment Gateway Integration
- Add `PaymentService` with Razorpay/Paytm SDK
- Add `payment_status` and `payment_id` fields to Order entity
- Add `POST /api/orders/{id}/payment/initiate` and `/verify` endpoints
- No other changes needed (existing order flow unchanged)

### Admin Panel
- Already have `User.Role.ADMIN` enum and admin security rules
- Create `AdminController` with endpoints for order management, user management
- Add `PUT /api/orders/{id}/status` for admin to update order status

### Microservices Migration
The layered architecture maps directly to microservices:
- `auth-service` → AuthController + AuthService + UserRepository
- `catalog-service` → OccasionController + PoojaKitController
- `cart-service` → CartController + CartService
- `order-service` → OrderController + OrderService

### Database Migration (H2 → MySQL)
1. Change `application.properties` profile to `prod`
2. Set `DATABASE_URL`, `DATABASE_USERNAME`, `DATABASE_PASSWORD` env vars
3. `spring.jpa.hibernate.ddl-auto=update` creates tables automatically
4. **Zero code changes in Java** - that's the benefit of JPA abstraction

---

## 10. Common Interview Questions & Answers

**Q: How do you handle database connection pooling?**
Spring Boot auto-configures HikariCP (fastest Java connection pool). Default pool size is 10 connections. Configurable via `spring.datasource.hikari.maximum-pool-size`.

**Q: How would you implement pagination?**
Spring Data JPA: `JpaRepository` supports `Pageable`:
```java
Page<PoojaKit> findByIsActiveTrue(Pageable pageable);
// Controller: pageable = PageRequest.of(page, size, Sort.by("createdAt").descending())
```

**Q: How do you prevent duplicate orders?**
Idempotency key: generate a UUID on frontend, send with order request, check if order with that key already exists before creating.

**Q: What is @Transactional and when would a transaction fail to rollback?**
By default, `@Transactional` only rolls back on **unchecked exceptions** (RuntimeException). Checked exceptions (like IOException) do NOT trigger rollback unless you add `@Transactional(rollbackFor = Exception.class)`.

**Q: How would you add caching?**
```java
@EnableCaching  // in config
@Cacheable(value = "occasions")  // on service method
public List<OccasionResponse> getAllOccasions() { ... }
// Uses Caffeine or Redis as backend
```

**Q: How would you handle concurrency in cart?**
Use optimistic locking (`@Version` on CartItem entity). When two requests try to update the same cart item, one will get an `OptimisticLockException` and can retry.
