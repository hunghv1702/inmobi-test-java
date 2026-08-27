package com.hunghv.inmobitestjava.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hunghv.inmobitestjava.adapter.PaymentAdapter;
import com.hunghv.inmobitestjava.adapter.PaymentCheckoutCommand;
import com.hunghv.inmobitestjava.adapter.PaymentCheckoutSession;
import com.hunghv.inmobitestjava.adapter.PaymentSessionStatus;
import com.hunghv.inmobitestjava.config.StripePaymentProperties;
import com.hunghv.inmobitestjava.constant.GameConstant;
import com.hunghv.inmobitestjava.constant.PaymentStatus;
import com.hunghv.inmobitestjava.entity.PaymentTransaction;
import com.hunghv.inmobitestjava.entity.UserAccount;
import com.hunghv.inmobitestjava.exception.BadRequestException;
import com.hunghv.inmobitestjava.exception.ResourceNotFoundException;
import com.hunghv.inmobitestjava.exception.UnauthorizedException;
import com.hunghv.inmobitestjava.generated.model.ConfirmTurnPackagePaymentRequest;
import com.hunghv.inmobitestjava.generated.model.CreateTurnPackageCheckoutRequest;
import com.hunghv.inmobitestjava.generated.model.PaymentCheckoutResponse;
import com.hunghv.inmobitestjava.generated.model.PaymentConfirmationResponse;
import com.hunghv.inmobitestjava.mapper.PaymentMapper;
import com.hunghv.inmobitestjava.repository.PaymentTransactionRepository;
import com.hunghv.inmobitestjava.repository.UserRepository;
import com.hunghv.inmobitestjava.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final UserRepository userRepository;
    private final PaymentTransactionRepository paymentTransactionRepository;
    private final PaymentAdapter paymentAdapter;
    private final PaymentMapper paymentMapper;
    private final StripePaymentProperties stripePaymentProperties;
    private final ObjectMapper objectMapper;

    @Lazy
    @Autowired
    private PaymentServiceImpl self;

    @Override
    public PaymentCheckoutResponse createTurnPackageCheckout(Long userId, CreateTurnPackageCheckoutRequest request) {
        UserAccount user = userRepository.findById(userId)
            .orElseThrow(() -> userNotFound(userId));

        PaymentCheckoutCommand command = new PaymentCheckoutCommand(
            user.getId(),
            user.getEmail(),
            GameConstant.BUY_TURNS_AMOUNT,
            stripePaymentProperties.getTurnPackageAmount(),
            stripePaymentProperties.getCurrency(),
            resolveSuccessUrl(request),
            resolveCancelUrl(request)
        );

        PaymentCheckoutSession session = paymentAdapter.createTurnPackageCheckout(command);
        return self.persistCheckoutSession(session, command);
    }

    @Override
    public PaymentConfirmationResponse confirmTurnPackagePayment(Long userId, ConfirmTurnPackagePaymentRequest request) {
        PaymentSessionStatus providerStatus = paymentAdapter.retrieveCheckoutSession(request.getCheckoutSessionId());
        return self.applyPaymentConfirmation(userId, request.getCheckoutSessionId(), providerStatus);
    }

    @Transactional(rollbackFor = Exception.class)
    public PaymentCheckoutResponse persistCheckoutSession(PaymentCheckoutSession session, PaymentCheckoutCommand command) {
        PaymentTransaction payment = new PaymentTransaction(
            userRepository.getReferenceById(command.getUserId()),
            session.getProvider(),
            session.getSessionId(),
            session.getCheckoutUrl(),
            PaymentStatus.PENDING,
            command.getTurns(),
            command.getAmount(),
            command.getCurrency()
        );
        paymentTransactionRepository.save(payment);
        log.info("Payment transaction created: paymentId={}, userId={}, providerSessionId={}",
            payment.getId(), command.getUserId(), payment.getProviderSessionId());
        return paymentMapper.toCheckoutResponse(payment);
    }

    @Transactional(rollbackFor = Exception.class)
    public PaymentConfirmationResponse applyPaymentConfirmation(
        Long userId,
        String checkoutSessionId,
        PaymentSessionStatus providerStatus
    ) {
        PaymentTransaction payment = paymentTransactionRepository
            .findByProviderSessionIdForUpdate(checkoutSessionId)
            .orElseThrow(() -> new ResourceNotFoundException("Payment transaction was not found"));

        if (!payment.belongsTo(userId)) {
            throw new BadRequestException("Payment transaction does not belong to current user");
        }

        if (payment.isPaid()) {
            return paymentMapper.toConfirmationResponse(payment);
        }

        if (providerStatus.isPaid()) {
            UserAccount user = userRepository.findByIdForUpdate(userId)
                .orElseThrow(() -> userNotFound(userId));
            user.addTurns(payment.getTurns());
            payment.markPaid();
            log.info("Payment confirmed and turns granted: paymentId={}, userId={}, turns={}",
                payment.getId(), userId, payment.getTurns());
            return paymentMapper.toConfirmationResponse(payment);
        }

        if (providerStatus.isTerminalButUnpaid()) {
            payment.markExpired();
            throw new BadRequestException("Payment was not completed");
        }

        throw new BadRequestException("Payment is not paid yet");
    }

    @Override
    public void handleStripeWebhook(String payload, String sigHeader) {
        verifyStripeSignature(payload, sigHeader);

        JsonNode event;
        try {
            event = objectMapper.readTree(payload);
        } catch (Exception ex) {
            log.warn("Stripe webhook: failed to parse payload");
            throw new BadRequestException("Invalid webhook payload");
        }

        String eventType = event.path("type").asText();
        log.info("Stripe webhook received: type={}", eventType);

        if (!"checkout.session.completed".equals(eventType)) {
            return;
        }

        JsonNode sessionNode = event.path("data").path("object");
        String sessionId = sessionNode.path("id").asText(null);
        String paymentStatus = sessionNode.path("payment_status").asText(null);
        String status = sessionNode.path("status").asText(null);

        if (!StringUtils.hasText(sessionId)) {
            log.warn("Stripe webhook: checkout.session.completed missing session id");
            return;
        }

        PaymentSessionStatus providerStatus = new PaymentSessionStatus("STRIPE", sessionId, status, paymentStatus);
        if (!providerStatus.isPaid()) {
            log.info("Stripe webhook: session {} not yet paid (status={}, paymentStatus={}), skipping",
                sessionId, status, paymentStatus);
            return;
        }

        self.applyWebhookPaymentConfirmation(sessionId, providerStatus);
    }

    @Transactional(rollbackFor = Exception.class)
    public void applyWebhookPaymentConfirmation(String checkoutSessionId, PaymentSessionStatus providerStatus) {
        PaymentTransaction payment = paymentTransactionRepository
            .findByProviderSessionIdForUpdate(checkoutSessionId)
            .orElseGet(() -> {
                log.warn("Stripe webhook: no local transaction found for sessionId={}", checkoutSessionId);
                return null;
            });

        if (payment == null || payment.isPaid()) {
            return;
        }

        Long userId = payment.getUser().getId();
        UserAccount user = userRepository.findByIdForUpdate(userId)
            .orElseThrow(() -> userNotFound(userId));
        user.addTurns(payment.getTurns());
        payment.markPaid();
        log.info("Stripe webhook: turns granted: paymentId={}, userId={}, turns={}",
            payment.getId(), userId, payment.getTurns());
    }

    private void verifyStripeSignature(String payload, String sigHeader) {
        String webhookSecret = stripePaymentProperties.getWebhookSecret();
        if (!StringUtils.hasText(webhookSecret)) {
            log.error("Stripe webhook secret is not configured");
            throw new UnauthorizedException("Webhook not configured");
        }
        if (!StringUtils.hasText(sigHeader)) {
            throw new UnauthorizedException("Missing Stripe-Signature header");
        }

        String timestamp = null;
        String v1Signature = null;
        for (String part : sigHeader.split(",")) {
            if (part.startsWith("t=")) timestamp = part.substring(2);
            else if (part.startsWith("v1=")) v1Signature = part.substring(3);
        }

        if (!StringUtils.hasText(timestamp) || !StringUtils.hasText(v1Signature)) {
            throw new UnauthorizedException("Malformed Stripe-Signature header");
        }

        String signedPayload = timestamp + "." + payload;
        String computedHmac = computeHmacSha256(webhookSecret, signedPayload);

        if (!constantTimeEquals(computedHmac, v1Signature)) {
            log.warn("Stripe webhook: signature mismatch");
            throw new UnauthorizedException("Invalid Stripe webhook signature");
        }
    }

    private String computeHmacSha256(String secret, String data) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            byte[] rawHmac = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(rawHmac);
        } catch (NoSuchAlgorithmException | InvalidKeyException ex) {
            throw new IllegalStateException("HMAC-SHA256 computation failed", ex);
        }
    }

    private boolean constantTimeEquals(String a, String b) {
        if (a.length() != b.length()) return false;
        int diff = 0;
        for (int i = 0; i < a.length(); i++) {
            diff |= a.charAt(i) ^ b.charAt(i);
        }
        return diff == 0;
    }

    private String resolveSuccessUrl(CreateTurnPackageCheckoutRequest request) {
        if (request != null && request.getSuccessUrl() != null) {
            return request.getSuccessUrl().toString();
        }
        return stripePaymentProperties.getSuccessUrl();
    }

    private String resolveCancelUrl(CreateTurnPackageCheckoutRequest request) {
        if (request != null && request.getCancelUrl() != null) {
            return request.getCancelUrl().toString();
        }
        return stripePaymentProperties.getCancelUrl();
    }

    private static ResourceNotFoundException userNotFound(Long userId) {
        return new ResourceNotFoundException("User %d was not found".formatted(userId));
    }
}
