package com.hunghv.inmobitestjava;

import com.hunghv.inmobitestjava.adapter.PaymentAdapter;
import com.hunghv.inmobitestjava.adapter.PaymentCheckoutCommand;
import com.hunghv.inmobitestjava.adapter.PaymentCheckoutSession;
import com.hunghv.inmobitestjava.adapter.PaymentSessionStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.util.UUID;

@Configuration
public class TestPaymentAdapterConfiguration {

    @Bean
    @Primary
    PaymentAdapter testPaymentAdapter() {
        return new PaymentAdapter() {
            @Override
            public PaymentCheckoutSession createTurnPackageCheckout(PaymentCheckoutCommand command) {
                String sessionId = "cs_test_" + command.getUserId() + "_" + UUID.randomUUID().toString().substring(0, 8);
                return new PaymentCheckoutSession(
                    "STRIPE",
                    sessionId,
                    "https://checkout.stripe.com/c/pay/" + sessionId,
                    "open",
                    "unpaid"
                );
            }

            @Override
            public PaymentSessionStatus retrieveCheckoutSession(String checkoutSessionId) {
                return new PaymentSessionStatus("STRIPE", checkoutSessionId, "complete", "paid");
            }
        };
    }
}
