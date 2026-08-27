package com.hunghv.inmobitestjava.service;

import com.hunghv.inmobitestjava.generated.model.ConfirmTurnPackagePaymentRequest;
import com.hunghv.inmobitestjava.generated.model.CreateTurnPackageCheckoutRequest;
import com.hunghv.inmobitestjava.generated.model.PaymentCheckoutResponse;
import com.hunghv.inmobitestjava.generated.model.PaymentConfirmationResponse;

public interface IPaymentService {

    PaymentCheckoutResponse createTurnPackageCheckout(Long userId, CreateTurnPackageCheckoutRequest request);

    PaymentConfirmationResponse confirmTurnPackagePayment(Long userId, ConfirmTurnPackagePaymentRequest request);

    /**
     * Handles an incoming Stripe webhook event.
     *
     * @param payload   raw request body (must be the unmodified byte stream for HMAC verification)
     * @param sigHeader value of the {@code Stripe-Signature} HTTP header
     */
    void handleStripeWebhook(String payload, String sigHeader);
}
