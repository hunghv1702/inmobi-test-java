package com.hunghv.inmobitestjava.service;

public interface IOtpService {

    String generateOtp(String email);

    boolean validateOtp(String email, String otpCode);

    void clearOtp(String email);

    boolean isBypassEnabled();
}
