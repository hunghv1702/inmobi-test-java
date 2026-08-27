package com.hunghv.inmobitestjava.adapter;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class PaymentCheckoutSession {

    private String provider;
    private String sessionId;
    private String checkoutUrl;
    private String status;
    private String paymentStatus;
}
