package com.hunghv.inmobitestjava;

import com.hunghv.inmobitestjava.service.impl.OtpServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OtpServiceTest {

    @Test
    @DisplayName("Should generate and validate OTP correctly when bypass is disabled")
    void testNormalOtpFlow() {
        OtpServiceImpl otpService = new OtpServiceImpl();
        ReflectionTestUtils.setField(otpService, "otpBypassEnabled", false);

        String email = "player@example.com";
        String code = otpService.generateOtp(email);

        assertNotNull(code);
        assertTrue(otpService.validateOtp(email, code));
        assertFalse(otpService.validateOtp(email, "999999"));

        otpService.clearOtp(email);
        assertFalse(otpService.validateOtp(email, code));
    }

    @Test
    @DisplayName("Should accept any OTP code when bypass mode is enabled via environment variable")
    void testBypassOtpFlow() {
        OtpServiceImpl otpService = new OtpServiceImpl();
        ReflectionTestUtils.setField(otpService, "otpBypassEnabled", true);

        String email = "player@example.com";
        otpService.generateOtp(email);

        assertTrue(otpService.isBypassEnabled());
        assertTrue(otpService.validateOtp(email, "123456"));
        assertTrue(otpService.validateOtp(email, "999999"));
        assertTrue(otpService.validateOtp(email, "anything"));
    }
}
