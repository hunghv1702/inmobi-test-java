package com.hunghv.inmobitestjava.exception;

import org.springframework.http.HttpStatus;

public class PaymentGatewayException extends ApiException {

    public PaymentGatewayException(String message) {
        super(HttpStatus.BAD_GATEWAY, message);
    }
}
