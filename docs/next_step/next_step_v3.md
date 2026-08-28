Add a JwtAuthenticationFilter
Create a OncePerRequestFilter that reads the Authorization: Bearer header, validates the token via JwtService.isTokenValid, loads the user via CustomUserDetailsService, and sets the SecurityContext. Register it in SecurityConfig with .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class). Without this, JWTs are decorative — nothing on the server checks them.
2
Build out custom exceptions + GlobalExceptionHandler
Add typed exceptions (EmailAlreadyExistsException, InvalidCredentialsException, RefreshTokenExpiredException, etc.) instead of raw RuntimeException, then implement GlobalExceptionHandler with @RestControllerAdvice mapping each to the correct HTTP status and a consistent error DTO ({timestamp, status, message, path}). This is what turns 'compiles' into 'production-grade.'
3
Add role-based authorization
UserRole exists on the entity but SecurityConfig doesn't use it yet. Add .requestMatchers(...).hasRole("ADMIN") style rules for the endpoints that need it (e.g. future product-management APIs), and make sure JwtService embeds the role as a claim so the filter can set the correct GrantedAuthority.
4
Write auth integration tests
Use Testcontainers (already planned in your stack) to spin up Postgres and write MockMvc tests for register/login/refresh: happy path, duplicate email, wrong password, expired/revoked refresh token. This is your regression safety net before you start layering Product/Inventory/Order on top.
5
Commit application.yml.example values to real config + secrets handling
You only have application.yml.example checked in — good for not leaking secrets, but confirm your actual application.yml (gitignored) sources jwt.secret from an environment variable, not a hardcoded string, before this goes anywhere near a shared environment.
6
Then move to Phase 2 (Product APIs)
Once auth is genuinely locked down and tested, build ProductController + ProductServiceImpl following the same pattern (DTO in, DTO out, no entity leakage) — you already have the entity, repository, and empty service interface waiting.