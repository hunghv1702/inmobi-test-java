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
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class ConcurrentGuessIntegrationTest {

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
    @DisplayName("Should consume only one turn when the same user sends concurrent guess requests")
    void shouldConsumeSingleTurnForConcurrentGuessRequests() throws Exception {
        String email = "race@example.com";
        String token = registerAndLogin(email, "secret123");
        UserAccount user = userRepository.findByEmail(email).orElseThrow();
        user.addTurns(1);
        userRepository.saveAndFlush(user);

        int requestCount = 10;
        ExecutorService executor = Executors.newFixedThreadPool(requestCount);
        CountDownLatch startGate = new CountDownLatch(1);
        List<Callable<Integer>> tasks = new ArrayList<>();
        String requestBody = objectMapper.writeValueAsString(Map.of("number", 1));

        for (int i = 0; i < requestCount; i++) {
            tasks.add(() -> {
                startGate.await(3, TimeUnit.SECONDS);
                return mockMvc.perform(post("/api/v1/guess")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                    .andReturn()
                    .getResponse()
                    .getStatus();
            });
        }

        List<Future<Integer>> futures = tasks.stream().map(executor::submit).toList();
        startGate.countDown();

        List<Integer> statuses = new ArrayList<>();
        for (Future<Integer> future : futures) {
            statuses.add(future.get(10, TimeUnit.SECONDS));
        }
        executor.shutdownNow();

        long successCount = statuses.stream().filter(status -> status == HttpStatus.OK.value()).count();
        long badRequestCount = statuses.stream().filter(status -> status == HttpStatus.BAD_REQUEST.value()).count();
        UserAccount updatedUser = userRepository.findByEmail(email).orElseThrow();

        assertThat(successCount).isEqualTo(1);
        assertThat(badRequestCount).isEqualTo(requestCount - 1);
        assertThat(updatedUser.getTurns()).isZero();
        assertThat(updatedUser.getScore()).isBetween(0, 1);
    }

    private String registerAndLogin(String email, String password) throws Exception {
        mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("email", email, "password", password))))
            .andExpect(status().isCreated());

        String response = mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("email", email, "password", password))))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();

        JsonNode json = objectMapper.readTree(response);
        return json.get("data").get("accessToken").asText();
    }
}
