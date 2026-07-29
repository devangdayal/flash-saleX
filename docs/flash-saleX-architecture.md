# Flash Sale X — Project Deep Dive

A file-by-file, layer-by-layer explanation of what exists, what it does, and where it's headed.

---

## 1. What this project actually is

Flash Sale X is a **Spring Boot 4.1.0 (Java 21) backend** for a flash-sale e-commerce system — the kind of system where a small batch of stock goes live at a set time and thousands of users try to buy it in the same few seconds. That's a specific engineering problem: normal CRUD patterns break down under that kind of concurrency (overselling stock, race conditions, DB contention), so the tech choices below aren't arbitrary — each one exists to solve a piece of that problem.

**Stack and why each piece is there:**

| Tech | Why it's in a flash-sale system |
|---|---|
| PostgreSQL + JPA/Hibernate | System of record for users, products, inventory, orders |
| Flyway | Version-controlled, repeatable schema migrations |
| Redis | (planned) Fast in-memory stock counters / distributed locks so the DB isn't hammered on every buy click |
| Kafka | (planned) Decouple "order accepted" from "order processed" — absorb bursts of demand asynchronously |
| Spring Security + JWT | Stateless auth so any instance behind a load balancer can validate a request without a shared session store |
| Actuator + Micrometer/Prometheus | Observability — you need real-time metrics during a flash sale to know if you're falling over |
| Lombok | Cuts entity/DTO boilerplate |

**Current maturity: early-to-mid scaffold.** The auth slice (register/login/refresh) is fully wired end-to-end. Everything else — product, inventory, order, payment — has entities, repositories and thin services, but the actual flash-sale mechanics (stock reservation, Redis counters, Kafka events, controllers to expose any of it over HTTP) don't exist yet. Full breakdown in §5.

---

## 2. The architectural pattern (from the README)

The project follows a **layered / package-by-feature** architecture:

```
Controller  → exposes REST endpoints, no business logic
Service     → all business logic lives here
Mapper      → converts between DTO ↔ Entity
Entity      → maps 1:1 to a database table
Repository  → pure data access (Spring Data JPA)
DTO         → shape of data crossing the HTTP boundary (never the entity itself)
```

Instead of grouping by *type* (`all controllers together`, `all entities together`), it groups by *feature* (`auth/`, `user/`, `product/`, `inventory/`, `order/`), and each feature folder internally repeats the Controller→Service→Mapper→Entity→Repository→DTO shape. This scales better than grouping-by-type because when you touch "orders," everything related to orders is in one place instead of scattered across five parallel type-based folders.

---

## 3. Full directory map — implemented vs. scaffolded

```
com.devangdayal.flashsale
├── FlashsaleApplication.java          ✅ entry point
│
├── config/                            ⚠️ partially implemented
│   ├── SecurityConfig.java            ✅ implemented (just fixed)
│   └── GlobalExceptionHandler.java    ❌ empty stub
│
├── properties/                        ✅ implemented
│   ├── FlashSaleRedisProperties.java
│   └── FlashSaleKafkaProperties.java
│
├── auth/                               ✅ fully implemented (most complete slice)
│   ├── controller/  AuthController.java
│   ├── dto/         LoginRequest, RegisterRequest, RefreshTokenRequest, AuthResponse
│   ├── entity/      RefreshToken.java
│   ├── repository/  RefreshTokenRepository.java
│   ├── service/      AuthService, JwtService, RefreshTokenService (interfaces)
│   │   └── impl/     AuthServiceImpl, JwtServiceImpl, RefreshTokenServiceImpl
│   ├── mapper/       ❌ empty (no auth-specific mapper needed yet — UserMapper covers it)
│   ├── config/       ❌ empty (planned: JWT filter registration probably belongs here)
│   └── security/     ❌ empty (planned: the missing JWT authentication filter)
│
├── user/                               ⚠️ partial
│   ├── entity/       User.java          ✅
│   ├── enums/        UserRole.java       ✅
│   ├── repository/   UserRepository.java ✅
│   ├── mapper/       UserMapper.java     ✅
│   ├── service/      UserService.java, CustomUserDetailsService.java ✅
│   ├── dto/          ❌ empty (no request/response DTOs for user profile yet)
│   └── controller/   ❌ empty (no HTTP endpoints to fetch/manage users yet)
│
├── product/                             ⚠️ partial
│   ├── entity/       Product.java        ✅
│   ├── enums/        ProductStatus.java  ✅
│   ├── repository/   ProductRepository.java ✅
│   ├── service/      ProductService.java  ✅ (bare — create + getById only)
│   ├── dto/          ❌ empty
│   ├── mapper/       ❌ empty
│   └── controller/   ❌ empty (no way to browse/list products over HTTP yet)
│
├── inventory/                            ⚠️ partial
│   ├── entity/       Inventory.java       ✅
│   ├── repository/   InventoryRepository.java ✅
│   ├── service/      InventoryService.java ✅ (bare — read-only lookups only)
│   ├── dto/          ❌ empty
│   ├── mapper/       ❌ empty
│   └── controller/   ❌ empty
│
├── order/                                 ⚠️ partial
│   ├── entity/       Order.java            ✅
│   ├── enums/        OrderStatus.java      ✅
│   ├── repository/   OrderRepository.java  ✅
│   ├── service/      OrderService.java     ✅ (bare — create + list only, no stock logic)
│   ├── dto/          ❌ empty
│   ├── mapper/       ❌ empty
│   └── controller/   ❌ empty (no "place order" endpoint yet)
│
├── payment/                               ❌ entirely empty (not started)
├── kafka/                                 ❌ entirely empty (not started)
├── redis/                                 ❌ entirely empty (not started)
├── metrics/                               ❌ entirely empty (not started)
└── scheduler/                             ❌ entirely empty (not started)
```

Legend: ✅ implemented and wired · ⚠️ entity/repo exist, logic is minimal · ❌ folder exists but is empty (planned)

---

## 4. What each file means, module by module

### `FlashsaleApplication.java`
Standard Spring Boot bootstrap class. The one thing worth noting: `@EnableConfigurationProperties({FlashSaleRedisProperties.class, FlashSaleKafkaProperties.class})` explicitly registers the two custom `@ConfigurationProperties` classes below — this is required because they aren't annotated with `@Component`/`@Configuration` themselves.

### `properties/`
- **`FlashSaleRedisProperties.java`** — binds `spring.data.redis.*` (host, port) into a typed Java object instead of `@Value`-injecting each field individually. Currently unused anywhere else in the code — it's prepared for whatever Redis service gets built.
- **`FlashSaleKafkaProperties.java`** — same idea for `spring.kafka.bootstrap-servers`. Also currently unused elsewhere.

### `config/`
- **`SecurityConfig.java`** — the security backbone: defines the `PasswordEncoder` (BCrypt), the `AuthenticationManager`, the `AuthenticationProvider` (just fixed for the Spring Security 7 API change), and the `SecurityFilterChain` — stateless sessions, CSRF off (correct for a stateless JWT API), `/api/v1/auth/**` open, everything else requires auth. **Gap:** no JWT filter is registered in the chain, so "requires auth" currently has no way to actually read/verify a JWT from a request header yet.
- **`GlobalExceptionHandler.java`** — an empty class with no `@RestControllerAdvice`. Every service in the codebase throws bare `RuntimeException`, which today will surface as generic 500 Internal Server Error responses instead of proper 4xx JSON error bodies.

### `auth/` — registration, login, tokens
- **`controller/AuthController.java`** — three endpoints: `POST /api/v1/auth/register`, `/login`, `/refresh`. Pure delegation to `AuthService`, no logic — exactly per the README's controller philosophy.
- **`dto/`**
  - `RegisterRequest` — firstName, lastName, email, password, all Bean-Validation annotated (`@NotBlank`, `@Email`, `@Size`).
  - `LoginRequest` — email + password.
  - `RefreshTokenRequest` — just the refresh token string.
  - `AuthResponse` — what comes back from all three endpoints: accessToken, refreshToken, tokenType ("Bearer"), expiresIn.
- **`entity/RefreshToken.java`** — a JPA entity, not just a JWT claim. Stores the opaque refresh token string, links to a `User` (`@ManyToOne`), tracks `expiresAt` and a `revoked` boolean. Storing refresh tokens server-side (rather than trusting a long-lived JWT) is what makes revocation possible.
- **`repository/RefreshTokenRepository.java`** — `findByToken`, `deleteByUser`, `findByUser`, `existsByToken`.
- **`service/` (interfaces) + `service/impl/` (implementations)**
  - `AuthService` / `AuthServiceImpl` — orchestrates register/login/refresh: checks for duplicate email, hashes password, saves the user, issues both an access token and a refresh token.
  - `JwtService` / `JwtServiceImpl` — pure JWT mechanics using `jjwt`: signs with an HMAC key derived from `jwt.secret`, sets subject = email, checks expiry. This class's `isTokenValid`/`extractUsername` methods are **written but never called** by anything in the security chain yet (see the missing filter above).
  - `RefreshTokenService` / `RefreshTokenServiceImpl` — creates a new refresh token (UUID string, 7-day expiry) and **deletes any existing one for that user first** — meaning this design supports one active session per user, not multiple concurrent devices. Also verifies a token on refresh: checks existence, revocation, and expiry (auto-deletes if expired).
- **`mapper/`, `config/`, `security/` (all empty)** — placeholders. `security/` is almost certainly meant to hold the missing JWT authentication filter (a class extending `OncePerRequestFilter` that reads the `Authorization` header, validates the JWT, and populates the `SecurityContext`).

### `user/` — accounts
- **`entity/User.java`** — id, firstName, lastName, email (unique), password (hashed), role, enabled, emailVerified, timestamps, and a `@OneToMany` back-reference to `Order`.
- **`enums/UserRole.java`** — `USER`, `ADMIN`.
- **`repository/UserRepository.java`** — a grab-bag of `findBy...` derived queries. Worth double-checking: `findByEnabledContainingIgnoreCase(String enabled)` takes a `String` for a boolean column — that's almost certainly leftover/copy-paste and would misbehave (or fail) if actually called.
- **`mapper/UserMapper.java`** — converts a `RegisterRequest` DTO into a `User` entity, defaulting role to `USER`, `enabled=true`, `emailVerified=false`.
- **`service/UserService.java`** — bare: `getUserById`, `getAllUsers`.
- **`service/CustomUserDetailsService.java`** — the Spring Security bridge: loads a `User` by email and wraps it as Spring's `UserDetails`, mapping `UserRole` → `ROLE_<name>` authority. This is what `DaoAuthenticationProvider` calls under the hood during login.
- **`dto/`, `controller/` (empty)** — no user-facing HTTP endpoints (e.g. "get my profile") yet.

### `product/` — the catalog
- **`entity/Product.java`** — id, name, description, price (`BigDecimal`, precision 12/scale 2 — correct choice for money), status, timestamps, `@OneToMany` to `Order`, `@OneToOne` to `Inventory` (cascades `ALL` — deleting a product deletes its inventory row too).
- **`enums/ProductStatus.java`** — `ACTIVE`, `INACTIVE`, `OUT_OF_STOCK`.
- **`repository/ProductRepository.java`** — just `findByNameContainingIgnoreCase`.
- **`service/ProductService.java`** — `createProduct`, `getProductById`. No listing, filtering, or status-transition logic yet.
- **`dto/`, `mapper/`, `controller/` (empty)** — there is currently **no way to browse products over HTTP** — this is one of the more important missing pieces if the goal is a working demo.

### `inventory/` — stock tracking (the crux of "flash sale")
- **`entity/Inventory.java`** — shares its primary key with `Product` via `@MapsId` (a true 1:1, not a foreign-key-plus-separate-id design). Fields: `availableQuantity`, `reservedQuantity`, and a `@Version` column for **optimistic locking**. That `@Version` field is the single most important detail in the whole codebase for flash-sale correctness: it means Hibernate will reject a concurrent update if another transaction already changed the row, which is exactly the mechanism you'd build "N people click buy, only K succeed" logic on top of.
- **`repository/InventoryRepository.java`** — `findByProductId`, `findByProductIdAndAvailableQuantity`.
- **`service/InventoryService.java`** — read-only wrappers only. **The actual "reserve N units atomically" operation doesn't exist yet** — that's the method this whole system is ultimately for, and it isn't written.
- **`dto/`, `mapper/`, `controller/` (empty)**.

### `order/` — purchases
- **`entity/Order.java`** — id, `@ManyToOne` to `User` and `Product`, unique `orderNumber`, quantity, status enum, `createdAt`.
- **`enums/OrderStatus.java`** — `CREATED`, `PENDING`, `CONFIRMED`, `CANCELLED`, `FAILED` — a sensible state machine for async order processing (e.g. via Kafka later), though nothing currently drives transitions between these states.
- **`repository/OrderRepository.java`** — `findByUserId`, `findByProductId`, `findByUserIdAndProductId`.
- **`service/OrderService.java`** — `createOrder` (plain save, no inventory check!), `getOrders`. **No controller exists**, so there is currently no "place an order" HTTP endpoint at all, and even if there were, `createOrder` as written doesn't decrement inventory or check stock — it would let you oversell.
- **`dto/`, `mapper/`, `controller/` (empty)**.

### `payment/`, `kafka/`, `redis/`, `metrics/`, `scheduler/`
All five packages exist as empty directories only — no files. These are the packages the README's target structure calls for but that haven't been started:
- **`payment/`** — presumably where a payment-provider integration (Stripe/Razorpay-style) would live, tied to `Order`.
- **`kafka/`** — producers/consumers for async order processing (e.g. publish "order placed" → consume → attempt to reserve stock → publish "confirmed"/"failed").
- **`redis/`** — where the actual flash-sale-speed logic would live: e.g. `DECR` a Redis counter per product as the first line of defense before ever touching Postgres, and/or a distributed lock (Redisson-style) around the reservation critical section.
- **`metrics/`** — custom Micrometer counters/timers beyond what Actuator gives for free (e.g. "orders attempted vs. succeeded during sale window").
- **`scheduler/`** — likely for time-boxed logic: opening/closing a sale at a scheduled time, expiring stale `PENDING` orders, etc.

---

## 5. Migrations ↔ entities cross-reference

| Migration | Table | Matches entity |
|---|---|---|
| `V1__create_auth_tables.sql` | `users`, `refresh_tokens` | `User`, `RefreshToken` |
| `V2__create_product.sql` | `product` | `Product` |
| `V3__create_inventory.sql` | `inventory` | `Inventory` |
| `V4__create_order.sql` | `orders` | `Order` |

All four are clean, sequential, and indexed sensibly (email, tokens, product name/status, order number/user/product/status). Schema and entities are in sync — `hibernate.ddl-auto: validate` in `application.yml` will only catch drift, never write it, since Flyway owns the schema.

---

## 6. Request flow — what actually works end-to-end today

```
Client → POST /api/v1/auth/register
       → AuthController.register()
       → AuthServiceImpl.register()
           → UserRepository.existsByEmail()      (reject duplicates)
           → UserMapper.toEntity()                (DTO → entity)
           → PasswordEncoder.encode()             (hash password)
           → UserRepository.save()
           → JwtServiceImpl.generateToken()       (sign JWT)
           → RefreshTokenServiceImpl.createRefreshToken()  (delete old + save new)
       → AuthResponse { accessToken, refreshToken, tokenType, expiresIn }
```

Login and refresh follow the same shape. This slice is genuinely complete and would work once the codebase compiles.

**What does NOT work end-to-end yet:** anything past auth. There's no endpoint to list products, no endpoint to place an order, and even the internal `OrderService.createOrder` doesn't touch `Inventory` at all — so the actual "flash sale" behavior (reserve stock atomically under concurrency) hasn't been written, only the data model it will eventually sit on top of.

---

## 7. Priority gaps, roughly in the order I'd tackle them

1. **JWT authentication filter** (`auth/security/`) — register a filter in `SecurityConfig` so `Authorization: Bearer <token>` headers actually authenticate requests; right now `JwtService.isTokenValid`/`extractUsername` are dead code.
2. **`GlobalExceptionHandler`** — turn service-layer `RuntimeException`s into proper `4xx` JSON responses (`@RestControllerAdvice`).
3. **Product & Order controllers** — there's no way to browse the catalog or place an order over HTTP at all right now.
4. **Inventory reservation logic** — the one method the whole system exists for: atomically checking + decrementing `availableQuantity` (leaning on the existing `@Version` field), rejecting when stock hits zero.
5. **Redis integration** — a fast pre-check/counter in front of Postgres so the database isn't the first thing hit by a burst of concurrent buy requests.
6. **Kafka integration** — decouple "order accepted" from "order fulfilled" so the HTTP request can return fast while processing happens async.
7. **Payment module** — currently nothing here at all.

---

*Generated from a full read of the repository: `pom.xml`, `application.yml`/`.env` (values redacted), all four Flyway migrations, and every `.java` file under `src/main/java`.*
