package com.hunghv.inmobitestjava.adapter;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class PaymentCheckoutCommand {

    private Long userId;
    private String email;
    private int turns;
    private long amount;
    private String currency;
    private String successUrl;
    private String cancelUrl;
}
