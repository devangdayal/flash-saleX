# Prompt for ChatGPT — FlashSaleX: JWT Filter + Global Exception Handler

You are acting as a Senior Backend Engineer doing a code review and implementation pass on a Spring Boot project called **FlashSaleX**. Follow the project's existing conventions exactly — do not restructure or rename anything that already works.

## Project Context

- **Stack**: Java 21, Spring Boot 4, Spring Security, Spring Data JPA, PostgreSQL, Flyway, Redis, Kafka, Lombok, JJWT (`jjwt-api`, `jjwt-impl`, `jjwt-jackson`)
- **Package root**: `com.devangdayal.flashsale`
- **Architecture**: feature-based packages (`auth`, `user`, `product`, `inventory`, `order`, `config`, `properties`), each with `controller / service / service.impl / repository / entity / dto / mapper` as applicable
- **Principles already in force**: constructor injection via `@RequiredArgsConstructor`, DTO-in/DTO-out (never expose JPA entities via API), Builder pattern + Lombok, service interfaces with separate `impl` classes, `LocalDateTime`, `EnumType.STRING`, BCrypt password hashing, optimistic locking (`@Version`) on Inventory

## What already exists (don't rewrite these — build on them)

**`auth/service/JwtService`** (impl: `JwtServiceImpl`) already has:
```java
String generateToken(User user);
String extractUsername(String token);
boolean isTokenValid(String token, User user);
long getExpirationTime();
```
Signing key comes from `${jwt.secret}` via `Keys.hmacShaKeyFor(...)`.

**`user/service/CustomUserDetailsService`** implements Spring Security's `UserDetailsService` and loads a `User` by email.

**`config/SecurityConfig`** currently has a `SecurityFilterChain` bean with:
```java
http.csrf(csrf -> csrf.disable())
    .httpBasic(AbstractHttpConfigurer::disable)
    .formLogin(AbstractHttpConfigurer::disable)
    .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
    .authorizeHttpRequests(auth -> auth
            .requestMatchers("/api/v1/auth/**").permitAll()
            .anyRequest().authenticated())
    .authenticationProvider(authenticationProvider);
```
**Problem**: there is no filter that reads the `Authorization` header and populates the `SecurityContext`. JWTs are generated on login/register but never validated on subsequent requests. This is the critical gap to fix.

**`config/GlobalExceptionHandler`** is currently an empty stub:
```java
package com.devangdayal.flashsale.config;

public class GlobalExceptionHandler {
}
```
Meanwhile, service classes (e.g. `AuthServiceImpl`) throw raw `RuntimeException` for cases like "Email already exist", "User not found", "Refresh token not found/revoked/expired" — these currently surface as unstyled 500s.

**`auth/entity/RefreshToken`** has `token`, `user`, `expiresAt`, `revoked` fields, managed via `RefreshTokenServiceImpl` (`createRefreshToken`, `verifyRefreshToken`).

**`user/entity/User`** has a `UserRole` enum field (role-based authorization is planned but not wired into `SecurityConfig` yet).

## Tasks

### 1. `JwtAuthenticationFilter`
Create `auth/filter/JwtAuthenticationFilter.java` (new `filter` sub-package under `auth`), extending `OncePerRequestFilter`:
- Extract the `Authorization: Bearer <token>` header; skip filtering gracefully if absent or malformed (pass through to the chain — let `SecurityConfig`'s `anyRequest().authenticated()` handle rejection).
- Extract username via `JwtService.extractUsername`.
- If `SecurityContextHolder` has no existing authentication, load the user via `CustomUserDetailsService`, validate the token with `JwtService.isTokenValid`, and if valid, build a `UsernamePasswordAuthenticationToken` (principal = UserDetails, credentials = null, authorities = `userDetails.getAuthorities()`), set `WebAuthenticationDetailsSource` details, and set it on the `SecurityContextHolder`.
- Use constructor injection (`@RequiredArgsConstructor`), matching project style.
- Wire it into `SecurityConfig` with `.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)`.

### 2. Custom exceptions
Create a `common/exception` package (per the `README.md`'s "Target Code Structure") with typed exceptions, each mapping to a clear HTTP status:
- `EmailAlreadyExistsException` → 409 Conflict
- `InvalidCredentialsException` → 401 Unauthorized
- `UserNotFoundException` → 404 Not Found
- `RefreshTokenNotFoundException` → 404 Not Found
- `RefreshTokenExpiredException` → 401 Unauthorized
- `RefreshTokenRevokedException` → 401 Unauthorized

Refactor `AuthServiceImpl` and `RefreshTokenServiceImpl` to throw these instead of raw `RuntimeException`.

### 3. `GlobalExceptionHandler`
Implement it as a `@RestControllerAdvice` in `config/GlobalExceptionHandler.java`:
- Handle each custom exception above, mapping to the correct status.
- Handle `MethodArgumentNotValidException` (for `@Valid` DTO validation failures) → 400, with field-level error details.
- Handle `AuthenticationException` (from `AuthenticationManager.authenticate`) → 401.
- Add a catch-all `Exception` handler → 500, logged, with a generic message (don't leak stack traces to clients).
- Return a consistent error response DTO — create `common/response/ErrorResponse` (or similar) with fields like `timestamp`, `status`, `error`, `message`, `path`.

### 4. Tests
Add MockMvc-based integration tests (Testcontainers + PostgreSQL, matching the stack already declared in `pom.xml`) covering:
- Register: happy path, duplicate email → 409
- Login: happy path, wrong password → 401
- Refresh: happy path, expired token → 401, revoked token → 401
- A protected endpoint (mock/stub one if needed) accessed with a valid token (200), missing token (401/403), and invalid/expired token (401)

## Constraints

- Keep DTOs out of the entity layer — never return JPA entities from controllers.
- Don't touch unrelated modules (`product`, `inventory`, `order`) — they're intentionally stubbed for later phases.
- Keep changes idiomatic to what's already there: Lombok annotations, constructor injection, `@Transactional` where services already use it.
- Show full file contents for every new/changed file, not diffs or snippets, so they can be copy-pasted directly into the project.