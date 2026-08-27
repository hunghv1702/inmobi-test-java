package com.hunghv.inmobitestjava;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hunghv.inmobitestjava.repository.PaymentTransactionRepository;
import com.hunghv.inmobitestjava.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class AuthAndGameApiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PaymentTransactionRepository paymentTransactionRepository;

    @Autowired
    private com.hunghv.inmobitestjava.repository.RefreshTokenRepository refreshTokenRepository;

    @BeforeEach
    void cleanDatabase() {
        paymentTransactionRepository.deleteAll();
        refreshTokenRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    @DisplayName("Should register, login, buy turns, guess, and read current profile")
    void shouldCompleteMainGameFlow() throws Exception {
        String registerBody = objectMapper.writeValueAsString(Map.of(
            "email", "Player@example.com",
            "password", "secret123"
        ));

        mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(registerBody))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.data.email").value("player@example.com"))
            .andExpect(jsonPath("$.data.score").value(0))
            .andExpect(jsonPath("$.data.turns").value(0));

        mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(registerBody))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.message").value("Email is already registered"));

        String token = login("player@example.com", "secret123");
        assertThat(token).isNotBlank();

        mockMvc.perform(get("/api/v1/me"))
            .andExpect(status().isUnauthorized());

        String initialCheckoutBody = objectMapper.writeValueAsString(Map.of(
            "successUrl", "http://localhost:5173/payment/success?session_id={CHECKOUT_SESSION_ID}",
            "cancelUrl", "http://localhost:5173/payment/cancel"
        ));
        String initialCheckoutResponse = mockMvc.perform(post("/api/v1/payments/turn-packages/checkout")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(initialCheckoutBody))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();

        String initialSessionId = objectMapper.readTree(initialCheckoutResponse)
            .get("data")
            .get("checkoutSessionId")
            .asText();

        mockMvc.perform(post("/api/v1/payments/turn-packages/confirm")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("checkoutSessionId", initialSessionId))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.turns").value(5));

        mockMvc.perform(post("/api/v1/guess")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("number", 9))))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("Request validation failed"));

        mockMvc.perform(post("/api/v1/guess")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("number", 3))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.guess").value(3))
            .andExpect(jsonPath("$.data.serverNumber").isNumber())
            .andExpect(jsonPath("$.data.turns").value(4));

        mockMvc.perform(get("/api/v1/me")
                .header("Authorization", "Bearer " + token))
            .andExpect(status().isOk())
            .andExpect(header().string("Cache-Control", "no-store"))
            .andExpect(jsonPath("$.data.email").value("player@example.com"))
            .andExpect(jsonPath("$.data.turns").value(4));

        String checkoutBody = objectMapper.writeValueAsString(Map.of(
            "successUrl", "http://localhost:5173/payment/success?session_id={CHECKOUT_SESSION_ID}",
            "cancelUrl", "http://localhost:5173/payment/cancel"
        ));
        String checkoutResponse = mockMvc.perform(post("/api/v1/payments/turn-packages/checkout")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(checkoutBody))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.provider").value("STRIPE"))
            .andExpect(jsonPath("$.data.checkoutUrl").isNotEmpty())
            .andExpect(jsonPath("$.data.status").value("PENDING"))
            .andReturn()
            .getResponse()
            .getContentAsString();

        String checkoutSessionId = objectMapper.readTree(checkoutResponse)
            .get("data")
            .get("checkoutSessionId")
            .asText();

        mockMvc.perform(post("/api/v1/payments/turn-packages/confirm")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("checkoutSessionId", checkoutSessionId))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.status").value("PAID"))
            .andExpect(jsonPath("$.data.turns").value(5));

        mockMvc.perform(get("/api/v1/me")
                .header("Authorization", "Bearer " + token))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.turns").value(9));
    }

    @Test
    @DisplayName("Should buy turns directly")
    void shouldBuyTurnsDirectly() throws Exception {
        String registerBody = objectMapper.writeValueAsString(Map.of(
            "email", "buy-turns-test@example.com",
            "password", "secret123"
        ));
        mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(registerBody))
            .andExpect(status().isCreated());

        String token = login("buy-turns-test@example.com", "secret123");

        mockMvc.perform(post("/api/v1/buy-turns")
                .header("Authorization", "Bearer " + token))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.turns").value(5));
    }

    @Test
    @DisplayName("Should reject invalid credentials")
    void shouldRejectInvalidCredentials() throws Exception {
        mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of(
                    "email", "player@example.com",
                    "password", "secret123"
                ))))
            .andExpect(status().isCreated());

        mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of(
                    "email", "player@example.com",
                    "password", "wrong-password"
                ))))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.message").value("Email or password is incorrect"));
    }

    @Test
    @DisplayName("Should login and perform refresh token rotation (RTR) successfully")
    void shouldRotateRefreshToken() throws Exception {
        String registerBody = objectMapper.writeValueAsString(Map.of(
            "email", "refresh-test@example.com",
            "password", "secret123"
        ));
        mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(registerBody))
            .andExpect(status().isCreated());

        String loginResponse = mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of(
                    "email", "refresh-test@example.com",
                    "password", "secret123"
                ))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.accessToken").isNotEmpty())
            .andExpect(jsonPath("$.data.refreshToken").isNotEmpty())
            .andReturn()
            .getResponse()
            .getContentAsString();

        JsonNode loginNode = objectMapper.readTree(loginResponse).get("data");
        String firstAccessToken = loginNode.get("accessToken").asText();
        String firstRefreshToken = loginNode.get("refreshToken").asText();

        String refreshBody = objectMapper.writeValueAsString(Map.of("refreshToken", firstRefreshToken));
        String refreshResponse = mockMvc.perform(post("/api/v1/auth/refresh")
                .contentType(MediaType.APPLICATION_JSON)
                .content(refreshBody))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.accessToken").isNotEmpty())
            .andExpect(jsonPath("$.data.refreshToken").isNotEmpty())
            .andReturn()
            .getResponse()
            .getContentAsString();

        JsonNode refreshNode = objectMapper.readTree(refreshResponse).get("data");
        String secondAccessToken = refreshNode.get("accessToken").asText();
        String secondRefreshToken = refreshNode.get("refreshToken").asText();

        assertThat(secondRefreshToken).isNotEqualTo(firstRefreshToken);

        mockMvc.perform(post("/api/v1/auth/refresh")
                .contentType(MediaType.APPLICATION_JSON)
                .content(refreshBody))
            .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/api/v1/me")
                .header("Authorization", "Bearer " + secondAccessToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.email").value("refresh-test@example.com"));
    }

    private String login(String email, String password) throws Exception {
        String response = mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("email", email, "password", password))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.tokenType").value("Bearer"))
            .andReturn()
            .getResponse()
            .getContentAsString();

        JsonNode json = objectMapper.readTree(response);
        return json.get("data").get("accessToken").asText();
    }
}
