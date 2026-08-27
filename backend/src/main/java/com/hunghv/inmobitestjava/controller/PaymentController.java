package com.hunghv.inmobitestjava.controller;

import com.hunghv.inmobitestjava.generated.api.PaymentApi;
import com.hunghv.inmobitestjava.generated.model.ConfirmTurnPackagePaymentRequest;
import com.hunghv.inmobitestjava.generated.model.CreateTurnPackageCheckoutRequest;
import com.hunghv.inmobitestjava.generated.model.PaymentCheckoutApiResponse;
import com.hunghv.inmobitestjava.generated.model.PaymentCheckoutResponse;
import com.hunghv.inmobitestjava.generated.model.PaymentConfirmationApiResponse;
import com.hunghv.inmobitestjava.generated.model.PaymentConfirmationResponse;
import com.hunghv.inmobitestjava.mapper.ApiResponseMapper;
import com.hunghv.inmobitestjava.service.IPaymentService;
import com.hunghv.inmobitestjava.utils.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
public class PaymentController implements PaymentApi {

    private final IPaymentService paymentService;
    private final ApiResponseMapper apiResponseMapper;

    @Override
    public ResponseEntity<PaymentCheckoutApiResponse> createTurnPackageCheckout(CreateTurnPackageCheckoutRequest request) {
        Long userId = SecurityUtils.getCurrentUserId();
        log.info("Attempting to create turn package checkout: userId={}", userId);
        PaymentCheckoutResponse response = paymentService.createTurnPackageCheckout(userId, request);
        log.info("Successfully created turn package checkout: userId={}, paymentId={}", userId, response.getPaymentId());
        return ResponseEntity.ok(apiResponseMapper.toSuccessResponse(response));
    }

    @Override
    public ResponseEntity<PaymentConfirmationApiResponse> confirmTurnPackagePayment(ConfirmTurnPackagePaymentRequest request) {
        Long userId = SecurityUtils.getCurrentUserId();
        log.info("Attempting to confirm turn package payment: userId={}, checkoutSessionId={}",
            userId, request.getCheckoutSessionId());
        PaymentConfirmationResponse response = paymentService.confirmTurnPackagePayment(userId, request);
        log.info("Successfully confirmed turn package payment: userId={}, paymentId={}, status={}",
            userId, response.getPaymentId(), response.getStatus());
        return ResponseEntity.ok(apiResponseMapper.toSuccessResponse(response));
    }
}
