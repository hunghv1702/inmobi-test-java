package com.hunghv.inmobitestjava.service;

import com.hunghv.inmobitestjava.generated.model.AuthResponse;
import com.hunghv.inmobitestjava.generated.model.ForgotPasswordRequest;
import com.hunghv.inmobitestjava.generated.model.ForgotPasswordResponse;
import com.hunghv.inmobitestjava.generated.model.LoginRequest;
import com.hunghv.inmobitestjava.generated.model.RegisterRequest;
import com.hunghv.inmobitestjava.generated.model.RegisterResponse;
import com.hunghv.inmobitestjava.generated.model.ResetPasswordRequest;
import com.hunghv.inmobitestjava.generated.model.TokenRefreshRequest;

public interface AuthService {

    RegisterResponse register(RegisterRequest request);

    AuthResponse login(LoginRequest request);

    AuthResponse refresh(TokenRefreshRequest request);

    ForgotPasswordResponse requestForgotPasswordOtp(ForgotPasswordRequest request);

    ForgotPasswordResponse resetPasswordWithOtp(ResetPasswordRequest request);
}
