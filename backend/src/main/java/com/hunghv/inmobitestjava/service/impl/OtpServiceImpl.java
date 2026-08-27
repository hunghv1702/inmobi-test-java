package com.hunghv.inmobitestjava.service.impl;

import com.hunghv.inmobitestjava.service.OtpService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
public class OtpServiceImpl implements OtpService {

    private static final int OTP_LENGTH = 6;
    private static final long OTP_VALIDITY_SECONDS = 300; // 5 minutes

    private final SecureRandom random = new SecureRandom();
    private final Map<String, OtpEntry> otpStore = new ConcurrentHashMap<>();

    @Value("${app.auth.otp-bypass-enabled:false}")
    private boolean otpBypassEnabled;

    @Override
    public String generateOtp(String email) {
        String code = String.format("%06d", random.nextInt(1_000_000));
        Instant expiresAt = Instant.now().plusSeconds(OTP_VALIDITY_SECONDS);
        otpStore.put(email.toLowerCase(), new OtpEntry(code, expiresAt));

        log.info("📧 [SIMULATED EMAIL SERVICE] Sent Password Reset OTP to email={}: OTP={}", email, code);
        if (otpBypassEnabled) {
            log.info("🔓 [OTP BYPASS ACTIVE] You can enter any OTP code (e.g. 123456) to reset password for email={}", email);
        }
        return code;
    }

    @Override
    public boolean validateOtp(String email, String otpCode) {
        if (!StringUtils.hasText(email) || !StringUtils.hasText(otpCode)) {
            return false;
        }

        if (otpBypassEnabled) {
            log.info("🔓 [OTP BYPASS ACTIVE] Accepting OTP verification for email={} (Bypass = true)", email);
            return true;
        }

        OtpEntry entry = otpStore.get(email.toLowerCase());
        if (entry == null) {
            log.warn("OTP validation failed: No OTP found for email={}", email);
            return false;
        }

        if (Instant.now().isAfter(entry.expiresAt())) {
            log.warn("OTP validation failed: OTP expired for email={}", email);
            otpStore.remove(email.toLowerCase());
            return false;
        }

        boolean match = entry.code().equals(otpCode.trim());
        if (!match) {
            log.warn("OTP validation failed: Invalid code for email={}", email);
        }
        return match;
    }

    @Override
    public void clearOtp(String email) {
        if (StringUtils.hasText(email)) {
            otpStore.remove(email.toLowerCase());
        }
    }

    @Override
    public boolean isBypassEnabled() {
        return otpBypassEnabled;
    }

    private record OtpEntry(String code, Instant expiresAt) {}
}
