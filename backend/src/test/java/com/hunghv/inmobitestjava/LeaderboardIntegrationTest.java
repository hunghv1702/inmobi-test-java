package com.hunghv.inmobitestjava;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hunghv.inmobitestjava.entity.UserAccount;
import com.hunghv.inmobitestjava.repository.PaymentTransactionRepository;
import com.hunghv.inmobitestjava.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class LeaderboardIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PaymentTransactionRepository paymentTransactionRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void cleanDatabase() {
        paymentTransactionRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    @DisplayName("Should return top 10 users ordered by score descending")
    void shouldReturnTopTenLeaderboard() throws Exception {
        String token = registerAndLoginViewer();
        for (int i = 1; i <= 12; i++) {
            UserAccount user = userRepository.save(new UserAccount("user%02d@example.com".formatted(i), passwordEncoder.encode("secret123")));
            jdbcTemplate.update("update users set score = ? where id = ?", i, user.getId());
        }

        mockMvc.perform(get("/api/v1/leaderboard")
                .header("Authorization", "Bearer " + token))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.length()").value(10))
            .andExpect(jsonPath("$.data[0].rank").value(1))
            .andExpect(jsonPath("$.data[0].email").value("user12@example.com"))
            .andExpect(jsonPath("$.data[0].score").value(12))
            .andExpect(jsonPath("$.data[9].rank").value(10))
            .andExpect(jsonPath("$.data[9].email").value("user03@example.com"))
            .andExpect(jsonPath("$.data[9].score").value(3));
    }

    private String registerAndLoginViewer() throws Exception {
        mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of(
                    "email", "viewer@example.com",
                    "password", "secret123"
                ))))
            .andExpect(status().isCreated());

        String response = mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of(
                    "email", "viewer@example.com",
                    "password", "secret123"
                ))))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();

        JsonNode json = objectMapper.readTree(response);
        return json.get("data").get("accessToken").asText();
    }
}
