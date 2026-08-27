package com.hunghv.inmobitestjava.adapter.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hunghv.inmobitestjava.adapter.PaymentAdapter;
import com.hunghv.inmobitestjava.adapter.PaymentCheckoutCommand;
import com.hunghv.inmobitestjava.adapter.PaymentCheckoutSession;
import com.hunghv.inmobitestjava.adapter.PaymentSessionStatus;
import com.hunghv.inmobitestjava.config.StripePaymentProperties;
import com.hunghv.inmobitestjava.exception.PaymentGatewayException;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.apache.hc.core5.util.Timeout;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

@Slf4j
@Component
@RequiredArgsConstructor
public class StripePaymentAdapter implements PaymentAdapter {

    public static final String PROVIDER = "STRIPE";

    private static final int CONNECT_TIMEOUT_MS = 2_000;
    private static final int READ_TIMEOUT_MS = 10_000;

    private final StripePaymentProperties properties;
    private final ObjectMapper objectMapper;

    private RestClient stripeClient;

    @PostConstruct
    void init() {
        RequestConfig requestConfig = RequestConfig.custom()
            .setConnectTimeout(Timeout.ofMilliseconds(CONNECT_TIMEOUT_MS))
            .setResponseTimeout(Timeout.ofMilliseconds(READ_TIMEOUT_MS))
            .build();

        HttpComponentsClientHttpRequestFactory factory =
            new HttpComponentsClientHttpRequestFactory(
                HttpClientBuilder.create()
                    .setDefaultRequestConfig(requestConfig)
                    .build()
            );

        this.stripeClient = RestClient.builder()
            .baseUrl(properties.getApiBaseUrl())
            .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + requireSecretKey())
            .requestFactory(factory)
            .build();
    }

    @Override
    public PaymentCheckoutSession createTurnPackageCheckout(PaymentCheckoutCommand command) {
        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("mode", "payment");
        form.add("success_url", command.getSuccessUrl());
        form.add("cancel_url", command.getCancelUrl());
        form.add("client_reference_id", String.valueOf(command.getUserId()));
        form.add("customer_email", command.getEmail());
        form.add("line_items[0][quantity]", "1");
        form.add("line_items[0][price_data][currency]", command.getCurrency());
        form.add("line_items[0][price_data][unit_amount]", String.valueOf(command.getAmount()));
        form.add("line_items[0][price_data][product_data][name]", command.getTurns() + " Guess Game turns");
        form.add("metadata[userId]", String.valueOf(command.getUserId()));
        form.add("metadata[turns]", String.valueOf(command.getTurns()));

        JsonNode response = postForm("/v1/checkout/sessions", form);
        String sessionId = requiredText(response, "id");
        String checkoutUrl = requiredText(response, "url");
        String status = optionalText(response, "status");
        String paymentStatus = optionalText(response, "payment_status");
        log.info("Stripe checkout session created: userId={}, sessionId={}, status={}, paymentStatus={}",
            command.getUserId(), sessionId, status, paymentStatus);
        return new PaymentCheckoutSession(PROVIDER, sessionId, checkoutUrl, status, paymentStatus);
    }

    @Override
    public PaymentSessionStatus retrieveCheckoutSession(String checkoutSessionId) {
        JsonNode response = get("/v1/checkout/sessions/" + checkoutSessionId);
        String sessionId = requiredText(response, "id");
        String status = optionalText(response, "status");
        String paymentStatus = optionalText(response, "payment_status");
        log.info("Stripe checkout session retrieved: sessionId={}, status={}, paymentStatus={}",
            sessionId, status, paymentStatus);
        return new PaymentSessionStatus(PROVIDER, sessionId, status, paymentStatus);
    }

    private JsonNode postForm(String uri, MultiValueMap<String, String> form) {
        try {
            String body = stripeClient.post()
                .uri(uri)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(form)
                .retrieve()
                .body(String.class);
            return readJson(body);
        } catch (RestClientResponseException exception) {
            throw stripeException(exception);
        } catch (RuntimeException exception) {
            log.error("Stripe connection error: uri={}", uri, exception);
            throw new PaymentGatewayException("Payment gateway service is currently unavailable");
        }
    }

    private JsonNode get(String uri) {
        try {
            String body = stripeClient.get()
                .uri(uri)
                .retrieve()
                .body(String.class);
            return readJson(body);
        } catch (RestClientResponseException exception) {
            throw stripeException(exception);
        } catch (RuntimeException exception) {
            log.error("Stripe lookup error: uri={}", uri, exception);
            throw new PaymentGatewayException("Payment gateway service is currently unavailable");
        }
    }

    private String requireSecretKey() {
        if (!StringUtils.hasText(properties.getSecretKey())) {
            throw new PaymentGatewayException("Payment configuration missing");
        }
        return properties.getSecretKey();
    }

    private JsonNode readJson(String body) {
        try {
            return objectMapper.readTree(body);
        } catch (Exception exception) {
            throw new PaymentGatewayException("Invalid response format from payment gateway");
        }
    }

    private PaymentGatewayException stripeException(RestClientResponseException exception) {
        try {
            JsonNode error = objectMapper.readTree(exception.getResponseBodyAsString()).path("error");
            if (StringUtils.hasText(error.path("message").asText())) {
                log.error("Stripe gateway returned error: status={}, rawMessage={}", 
                    exception.getStatusCode(), error.path("message").asText());
            }
        } catch (Exception ignored) {
            log.error("Stripe request failed with status={}", exception.getStatusCode(), exception);
        }
        // Mask raw Stripe API errors to prevent Information Disclosure to End-Users
        return new PaymentGatewayException("Payment gateway service is temporarily unavailable. Please try again later.");
    }

    private String requiredText(JsonNode node, String field) {
        String value = optionalText(node, field);
        if (!StringUtils.hasText(value)) {
            throw new PaymentGatewayException("Payment gateway response structure error");
        }
        return value;
    }

    private String optionalText(JsonNode node, String field) {
        JsonNode value = node.path(field);
        return value.isMissingNode() || value.isNull() ? null : value.asText();
    }
}
