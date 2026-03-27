# ShubhaSamagri - Architecture Documentation

## 1. System Overview

```
┌─────────────────────────────────────────────────────────────────┐
│                        CLIENT LAYER                             │
│  Browser (React SPA) ──── Axios ──── JWT in localStorage       │
└─────────────────────────┬───────────────────────────────────────┘
                          │  HTTPS / REST API
                          ▼
┌─────────────────────────────────────────────────────────────────┐
│                    SPRING BOOT BACKEND                          │
│                                                                  │
│  ┌──────────────┐  ┌────────────────┐  ┌───────────────────┐   │
│  │  Controller  │→ │    Service     │→ │    Repository     │   │
│  │  (REST API)  │  │ (Business Logic│  │  (Spring Data JPA)│   │
│  └──────────────┘  └────────────────┘  └─────────┬─────────┘   │
│                                                    │             │
│  ┌──────────────────────────────────────────────┐ │             │
│  │         Spring Security (JWT Filter)         │ │             │
│  └──────────────────────────────────────────────┘ │             │
└───────────────────────────────────────────────────┼─────────────┘
                                                     │
                          ┌──────────────────────────▼────────┐
                          │     DATABASE LAYER                 │
                          │  H2 (dev) / MySQL (prod)          │
                          │  Spring Data JPA + Hibernate ORM  │
                          └───────────────────────────────────┘
```

## 2. Tech Stack

| Layer        | Technology         | Version  | Purpose                            |
|--------------|--------------------|----------|------------------------------------|
| Frontend     | React              | 18.2.0   | SPA with functional components     |
| Frontend     | Vite               | 5.x      | Build tool & dev server            |
| Frontend     | React Router       | 6.x      | Client-side routing                |
| Frontend     | Axios              | 1.6.x    | HTTP client with interceptors      |
| Frontend     | React Hot Toast    | 2.4.x    | Notification toasts                |
| Backend      | Spring Boot        | 3.2.3    | REST API framework                 |
| Backend      | Spring Security    | 6.x      | Authentication & authorization     |
| Backend      | Spring Data JPA    | 3.x      | Data access layer                  |
| Backend      | Hibernate          | 6.x      | ORM                                |
| Backend      | Lombok             | 1.18.x   | Boilerplate reduction              |
| Backend      | JJWT               | 0.11.5   | JWT token generation/validation    |
| Backend      | SpringDoc OpenAPI  | 2.3.0    | Swagger UI / API docs              |
| Database     | H2                 | 2.x      | Development/testing                |
| Database     | MySQL              | 8.x      | Production                         |
| Build        | Maven              | 3.x      | Backend dependency management      |
| Build        | npm                | 10.x     | Frontend dependency management     |

## 3. Backend Layered Architecture

```
Request → Controller → Service → Repository → Database
                    ↕          ↕
                   DTO       Entity
                    ↕
                  Mapper
```

### Layer Responsibilities

| Layer       | Package                         | Responsibility                              |
|-------------|----------------------------------|---------------------------------------------|
| Controller  | `com.shubhasamagri.controller`  | HTTP routing, request validation, response  |
| Service     | `com.shubhasamagri.service`     | Business logic, transactions                |
| Repository  | `com.shubhasamagri.repository`  | Database queries (Spring Data JPA)          |
| Entity      | `com.shubhasamagri.entity`      | JPA-mapped database tables                  |
| DTO         | `com.shubhasamagri.dto`         | Request/Response data transfer objects      |
| Mapper      | `com.shubhasamagri.mapper`      | Entity ↔ DTO conversion                     |
| Security    | `com.shubhasamagri.security`    | JWT filter, UserDetailsService              |
| Config      | `com.shubhasamagri.config`      | Spring Security, CORS, Swagger, Data init   |
| Exception   | `com.shubhasamagri.exception`   | Custom exceptions + global handler          |

## 4. Database Schema

### Tables & Columns

#### `users`
| Column     | Type         | Constraints                    |
|------------|--------------|--------------------------------|
| id         | BIGINT       | PK, AUTO_INCREMENT             |
| name       | VARCHAR(100) | NOT NULL                       |
| email      | VARCHAR(255) | NOT NULL, UNIQUE               |
| password   | VARCHAR(255) | NOT NULL (BCrypt hashed)       |
| phone      | VARCHAR(15)  |                                |
| role       | ENUM         | NOT NULL, DEFAULT 'USER'       |
| created_at | TIMESTAMP    | NOT NULL                       |
| updated_at | TIMESTAMP    |                                |

#### `occasions`
| Column      | Type         | Constraints                   |
|-------------|--------------|-------------------------------|
| id          | BIGINT       | PK, AUTO_INCREMENT            |
| name        | VARCHAR(100) | NOT NULL, UNIQUE              |
| description | TEXT         |                               |
| image_url   | VARCHAR(500) |                               |
| is_active   | BOOLEAN      | NOT NULL, DEFAULT true        |
| created_at  | TIMESTAMP    | NOT NULL                      |

#### `pooja_items`
| Column       | Type         | Constraints                  |
|--------------|--------------|------------------------------|
| id           | BIGINT       | PK, AUTO_INCREMENT           |
| name         | VARCHAR(100) | NOT NULL                     |
| description  | TEXT         |                              |
| unit         | VARCHAR(50)  | NOT NULL (pcs/grams/ml/pack) |
| image_url    | VARCHAR(500) |                              |
| is_available | BOOLEAN      | NOT NULL, DEFAULT true       |
| created_at   | TIMESTAMP    | NOT NULL                     |

#### `pooja_kits`
| Column                 | Type          | Constraints              |
|------------------------|---------------|--------------------------|
| id                     | BIGINT        | PK, AUTO_INCREMENT       |
| name                   | VARCHAR(200)  | NOT NULL                 |
| description            | TEXT          |                          |
| occasion_id            | BIGINT        | FK → occasions(id)       |
| price                  | DECIMAL(10,2) | NOT NULL                 |
| image_url              | VARCHAR(500)  |                          |
| estimated_delivery_days| INT           | DEFAULT 3                |
| is_active              | BOOLEAN       | NOT NULL, DEFAULT true   |
| created_at             | TIMESTAMP     | NOT NULL                 |

#### `kit_items` (Junction: PoojaKit ↔ PoojaItem)
| Column       | Type        | Constraints              |
|--------------|-------------|--------------------------|
| id           | BIGINT      | PK, AUTO_INCREMENT       |
| pooja_kit_id | BIGINT      | FK → pooja_kits(id)      |
| pooja_item_id| BIGINT      | FK → pooja_items(id)     |
| quantity     | INT         | NOT NULL                 |
| unit         | VARCHAR(50) | Override for this context|

#### `cart_items`
| Column       | Type      | Constraints            |
|--------------|-----------|------------------------|
| id           | BIGINT    | PK, AUTO_INCREMENT     |
| user_id      | BIGINT    | FK → users(id)         |
| pooja_kit_id | BIGINT    | FK → pooja_kits(id)    |
| quantity     | INT       | NOT NULL, DEFAULT 1    |
| added_at     | TIMESTAMP | NOT NULL               |

#### `orders`
| Column           | Type          | Constraints                  |
|------------------|---------------|------------------------------|
| id               | BIGINT        | PK, AUTO_INCREMENT           |
| user_id          | BIGINT        | FK → users(id)               |
| status           | ENUM          | PENDING/CONFIRMED/SHIPPED... |
| total_amount     | DECIMAL(10,2) | NOT NULL                     |
| delivery_address | TEXT          | NOT NULL                     |
| phone            | VARCHAR(15)   | NOT NULL                     |
| created_at       | TIMESTAMP     | NOT NULL                     |
| updated_at       | TIMESTAMP     |                              |

#### `order_items`
| Column         | Type          | Constraints             |
|----------------|---------------|-------------------------|
| id             | BIGINT        | PK, AUTO_INCREMENT      |
| order_id       | BIGINT        | FK → orders(id)         |
| pooja_kit_id   | BIGINT        | FK → pooja_kits(id)     |
| quantity       | INT           | NOT NULL                |
| price_at_order | DECIMAL(10,2) | NOT NULL (price snapshot)|

### Entity Relationships (ERD)
```
User ──────────── CartItem ─────── PoojaKit ─── KitItem ─── PoojaItem
  │         (N)                (N)         (N)           (N)
  └── Order (N) ── OrderItem (N) ──── PoojaKit
                                           │
                                      Occasion (1)
```

## 5. API Endpoints

### Authentication (Public)
| Method | Endpoint           | Description                    | Request Body         |
|--------|--------------------|--------------------------------|----------------------|
| POST   | /api/auth/signup   | Register new user              | SignupRequest        |
| POST   | /api/auth/login    | Login, get JWT token           | LoginRequest         |

### Occasions (GET: Public, POST/PUT/DELETE: Admin)
| Method | Endpoint               | Auth     | Description              |
|--------|------------------------|----------|--------------------------|
| GET    | /api/occasions         | None     | Get all active occasions |
| GET    | /api/occasions/{id}    | None     | Get occasion by ID       |
| POST   | /api/occasions         | ADMIN    | Create occasion          |
| PUT    | /api/occasions/{id}    | ADMIN    | Update occasion          |
| DELETE | /api/occasions/{id}    | ADMIN    | Soft delete occasion     |

### Pooja Kits (GET: Public, POST: Admin)
| Method | Endpoint                      | Auth  | Description              |
|--------|-------------------------------|-------|--------------------------|
| GET    | /api/kits                     | None  | Get all active kits      |
| GET    | /api/kits/{id}                | None  | Get kit with all items   |
| GET    | /api/kits/occasion/{id}       | None  | Get kits by occasion     |
| POST   | /api/kits                     | ADMIN | Create kit               |

### Cart (All require JWT)
| Method | Endpoint                | Auth | Description                  |
|--------|-------------------------|------|------------------------------|
| GET    | /api/cart               | JWT  | Get current user's cart      |
| POST   | /api/cart/add           | JWT  | Add kit to cart              |
| PUT    | /api/cart/{cartItemId}  | JWT  | Update item quantity         |
| DELETE | /api/cart/{cartItemId}  | JWT  | Remove item from cart        |
| DELETE | /api/cart/clear         | JWT  | Clear all cart items         |

### Orders (All require JWT)
| Method | Endpoint                     | Auth | Description              |
|--------|------------------------------|------|--------------------------|
| POST   | /api/orders                  | JWT  | Place order from cart    |
| GET    | /api/orders                  | JWT  | Get user's orders        |
| GET    | /api/orders/{orderId}        | JWT  | Get specific order       |
| PUT    | /api/orders/{orderId}/cancel | JWT  | Cancel PENDING order     |

## 6. JWT Authentication Flow

```
1. POST /api/auth/login
   { email, password }
         │
         ▼
2. Spring AuthenticationManager validates credentials
         │
         ▼
3. JwtTokenProvider.generateToken(UserDetails)
   → HMAC-SHA256 signed token (expiry: 24h)
         │
         ▼
4. Client stores token in localStorage
         │
         ▼ (subsequent requests)
5. Request: Authorization: Bearer <token>
         │
         ▼
6. JwtAuthenticationFilter.doFilterInternal()
   → Extract token → Validate → Load UserDetails
   → Set SecurityContextHolder.getContext().setAuthentication()
         │
         ▼
7. Controller: SecurityContextHolder.getContext().getAuthentication().getName()
   → returns user email → lookup user ID from DB
```

## 7. Exception Handling Strategy

All exceptions are caught by `GlobalExceptionHandler` (`@RestControllerAdvice`):

| Exception                      | HTTP Status | Response Body                        |
|--------------------------------|-------------|--------------------------------------|
| ResourceNotFoundException      | 404         | `{ success: false, message: "..." }` |
| BadRequestException            | 400         | `{ success: false, message: "..." }` |
| UnauthorizedException          | 401         | `{ success: false, message: "..." }` |
| BadCredentialsException        | 401         | `{ success: false, message: "..." }` |
| MethodArgumentNotValidException| 400         | `{ success: false, data: {field errors} }` |
| Exception (catch-all)          | 500         | `{ success: false, message: "..." }` |

## 8. Standard API Response Format

All endpoints return:
```json
{
  "success": true,
  "message": "Human-readable message",
  "data": { ... },
  "timestamp": "2024-01-01T10:00:00"
}
```

## 9. Security Configuration

| Endpoint Pattern     | Access Level                 |
|----------------------|------------------------------|
| /api/auth/**         | Public                       |
| GET /api/occasions/**| Public                       |
| GET /api/kits/**     | Public                       |
| /api/cart/**         | Authenticated (any role)     |
| /api/orders/**       | Authenticated (any role)     |
| POST /api/occasions/**| ADMIN role only             |
| PUT/DELETE /api/occasions/**| ADMIN role only      |
| /swagger-ui/**       | Public (dev)                 |
| /h2-console/**       | Public (dev)                 |

## 10. CORS Configuration

Configured in `CorsConfig.java`:
- Allowed origins: read from `app.cors.allowed-origins` property
- Allowed methods: GET, POST, PUT, DELETE, OPTIONS, PATCH
- Allowed headers: Authorization, Content-Type, Accept
- Credentials: true (allows cookies/auth headers)
- Max age: 3600s (preflight cache)

## 11. Project File Structure

```
ShubhaSamagri/
├── backend/
│   ├── pom.xml
│   └── src/main/java/com/shubhasamagri/
│       ├── ShubhaSamagriApplication.java
│       ├── config/
│       │   ├── CorsConfig.java
│       │   ├── DataInitializer.java     ← sample data on startup
│       │   ├── SecurityConfig.java
│       │   └── SwaggerConfig.java
│       ├── controller/
│       │   ├── AuthController.java
│       │   ├── CartController.java
│       │   ├── OccasionController.java
│       │   ├── OrderController.java
│       │   └── PoojaKitController.java
│       ├── dto/
│       │   ├── request/               ← incoming request bodies
│       │   └── response/              ← outgoing JSON responses
│       ├── entity/                    ← JPA entities (DB tables)
│       ├── exception/                 ← custom exceptions + handler
│       ├── mapper/                    ← entity → DTO conversion
│       ├── repository/                ← Spring Data JPA interfaces
│       ├── security/
│       │   ├── JwtAuthenticationFilter.java
│       │   ├── JwtTokenProvider.java
│       │   └── UserDetailsServiceImpl.java
│       └── service/                   ← business logic
├── frontend/
│   ├── src/
│   │   ├── api/                       ← axios API call functions
│   │   ├── components/                ← reusable UI components
│   │   ├── context/                   ← React Context (auth, cart)
│   │   └── pages/                     ← route-level page components
│   ├── package.json
│   └── vite.config.js
├── docs/
│   ├── ARCHITECTURE.md
│   ├── DEPLOYMENT.md
│   └── INTERVIEW_GUIDE.md
└── postman/
    ├── ShubhaSamagri.postman_collection.json
    └── ShubhaSamagri.postman_environment.json
```
