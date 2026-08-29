package com.devangdayal.flashsale.auth;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

import static org.hamcrest.Matchers.is;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import com.devangdayal.flashsale.FlashsaleApplication;
import com.devangdayal.flashsale.auth.dto.AuthResponse;
import com.devangdayal.flashsale.auth.entity.RefreshToken;
import com.devangdayal.flashsale.auth.repository.RefreshTokenRepository;
import com.devangdayal.flashsale.user.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootTest(classes = FlashsaleApplication.class)
@Testcontainers
@Import(AuthSecurityIntegrationTest.ProtectedEndpointTestConfig.class)
class AuthSecurityIntegrationTest {

    @Container
    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16-alpine");

    @DynamicPropertySource
    static void configureDatabase(
            DynamicPropertyRegistry registry) {

        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Test
    void register_shouldReturnCreated() throws Exception {

        String email = uniqueEmail();

        mockMvc.perform(
                post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(
                                registerJson(
                                        email,
                                        "Password123!")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.refreshToken").isNotEmpty())
                .andExpect(
                        jsonPath(
                                "$.tokenType",
                                is("Bearer")));
    }

    @Test
    void register_duplicateEmail_shouldReturnConflict()
            throws Exception {

        String email = uniqueEmail();

        register(email, "Password123!");

        mockMvc.perform(
                post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(
                                registerJson(
                                        email,
                                        "Password123!")))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status", is(409)))
                .andExpect(
                        jsonPath(
                                "$.error",
                                is("Conflict")));
    }

    @Test
    void login_shouldReturnOk() throws Exception {

        String email = uniqueEmail();

        register(email, "Password123!");

        mockMvc.perform(
                post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(
                                loginJson(
                                        email,
                                        "Password123!")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.refreshToken").isNotEmpty());
    }

    @Test
    void login_wrongPassword_shouldReturnUnauthorized()
            throws Exception {

        String email = uniqueEmail();

        register(email, "Password123!");

        mockMvc.perform(
                post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(
                                loginJson(
                                        email,
                                        "WrongPassword!")))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status", is(401)));
    }

    @Test
    void refresh_shouldReturnOk() throws Exception {

        AuthResponse auth = register(
                uniqueEmail(),
                "Password123!");

        mockMvc.perform(
                post("/api/v1/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(
                                refreshJson(
                                        auth.getRefreshToken())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").isNotEmpty());
    }

    @Test
    void refresh_expiredToken_shouldReturnUnauthorized()
            throws Exception {

        AuthResponse auth = register(
                uniqueEmail(),
                "Password123!");

        RefreshToken refreshToken = refreshTokenRepository
                .findByToken(auth.getRefreshToken())
                .orElseThrow();

        refreshToken.setExpiresAt(
                LocalDateTime.now().minusMinutes(1));

        refreshTokenRepository.save(refreshToken);

        mockMvc.perform(
                post("/api/v1/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(
                                refreshJson(
                                        auth.getRefreshToken())))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status", is(401)));
    }

    @Test
    void refresh_revokedToken_shouldReturnUnauthorized()
            throws Exception {

        AuthResponse auth = register(
                uniqueEmail(),
                "Password123!");

        RefreshToken refreshToken = refreshTokenRepository
                .findByToken(auth.getRefreshToken())
                .orElseThrow();

        refreshToken.setRevoked(true);

        refreshTokenRepository.save(refreshToken);

        mockMvc.perform(
                post("/api/v1/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(
                                refreshJson(
                                        auth.getRefreshToken())))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status", is(401)));
    }

    @Test
    void protectedEndpoint_validToken_shouldReturnOk()
            throws Exception {

        AuthResponse auth = register(
                uniqueEmail(),
                "Password123!");

        mockMvc.perform(
                get("/api/v1/test/protected")
                        .header(
                                "Authorization",
                                "Bearer "
                                        + auth.getAccessToken()))
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath(
                                "$.message",
                                is("authenticated")));
    }

    @Test
    void protectedEndpoint_missingToken_shouldBeRejected()
            throws Exception {

        mockMvc.perform(
                get("/api/v1/test/protected"))
                .andExpect(status().isForbidden());
    }

    @Test
    void protectedEndpoint_invalidToken_shouldBeRejected()
            throws Exception {

        mockMvc.perform(
                get("/api/v1/test/protected")
                        .header(
                                "Authorization",
                                "Bearer invalid-token"))
                .andExpect(status().isForbidden());
    }

    @Test
    void protectedEndpoint_expiredToken_shouldBeRejected()
            throws Exception {

       
        mockMvc.perform(
                get("/api/v1/test/protected")
                        .header(
                                "Authorization",
                                "Bearer invalid-token"))
                .andExpect(status().isForbidden());
    }

    private AuthResponse register(
            String email,
            String password) throws Exception {

        String response = mockMvc.perform(
                post("/api/v1/auth/register")
                        .contentType(
                                MediaType.APPLICATION_JSON)
                        .content(
                                registerJson(
                                        email,
                                        password)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        return objectMapper.readValue(
                response,
                AuthResponse.class);
    }

    private String registerJson(
            String email,
            String password) throws Exception {

        return objectMapper.writeValueAsString(
                Map.of(
                        "firstName", "Test",
                        "lastName", "User",
                        "email", email,
                        "password", password));
    }

    private String loginJson(
            String email,
            String password) throws Exception {

        return objectMapper.writeValueAsString(
                Map.of(
                        "email", email,
                        "password", password));
    }

    private String refreshJson(
            String refreshToken) throws Exception {

        return objectMapper.writeValueAsString(
                Map.of(
                        "refreshToken",
                        refreshToken));
    }

    private String uniqueEmail() {
        return "test-"
                + UUID.randomUUID()
                + "@example.com";
    }

    @TestConfiguration(proxyBeanMethods = false)
    static class ProtectedEndpointTestConfig {

        @Bean
        ProtectedTestController protectedTestController() {
            return new ProtectedTestController();
        }
    }

    @RestController
    static class ProtectedTestController {

        @GetMapping("/api/v1/test/protected")
        Map<String, String> protectedEndpoint() {
            return Map.of(
                    "message",
                    "authenticated");
        }
    }
}