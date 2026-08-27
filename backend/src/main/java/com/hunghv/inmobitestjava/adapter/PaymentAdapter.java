package com.hunghv.inmobitestjava.adapter;

public interface PaymentAdapter {

    PaymentCheckoutSession createTurnPackageCheckout(PaymentCheckoutCommand command);

    PaymentSessionStatus retrieveCheckoutSession(String checkoutSessionId);
}
