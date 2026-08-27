package com.hunghv.inmobitestjava.adapter;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class PaymentSessionStatus {

    private String provider;
    private String sessionId;
    private String status;
    private String paymentStatus;

    public boolean isPaid() {
        return "complete".equalsIgnoreCase(status) && "paid".equalsIgnoreCase(paymentStatus);
    }

    public boolean isTerminalButUnpaid() {
        return "expired".equalsIgnoreCase(status)
            || ("complete".equalsIgnoreCase(status) && !"paid".equalsIgnoreCase(paymentStatus));
    }
}
