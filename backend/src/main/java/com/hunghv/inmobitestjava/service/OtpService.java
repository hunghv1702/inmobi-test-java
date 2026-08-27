package com.hunghv.inmobitestjava.service;

public interface OtpService {

    String generateOtp(String email);

    boolean validateOtp(String email, String otpCode);

    void clearOtp(String email);

    boolean isBypassEnabled();
}
