package com.hunghv.inmobitestjava.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "app.payment.stripe")
public class StripePaymentProperties {

    private String secretKey;
    private String webhookSecret;
    private String apiBaseUrl = "https://api.stripe.com";
    private String successUrl = "http://localhost:5173/payment/success?session_id={CHECKOUT_SESSION_ID}";
    private String cancelUrl = "http://localhost:5173/payment/cancel";
    private String currency = "usd";
    private long turnPackageAmount = 199;
}
