package com.hunghv.inmobitestjava.controller;

import com.hunghv.inmobitestjava.generated.api.AuthApi;
import com.hunghv.inmobitestjava.generated.model.*;
import com.hunghv.inmobitestjava.mapper.ApiResponseMapper;
import com.hunghv.inmobitestjava.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
public class AuthController implements AuthApi {

    private final AuthService authService;
    private final ApiResponseMapper apiResponseMapper;

    @Override
    public ResponseEntity<RegisterApiResponse> register(RegisterRequest request) {
        log.info("Attempting to register user: email={}", request.getEmail());
        RegisterResponse response = authService.register(request);
        log.info("Successfully registered user: userId={}, email={}", response.getId(), response.getEmail());
        return ResponseEntity.status(HttpStatus.CREATED).body(apiResponseMapper.toCreatedResponse(response));
    }

    @Override
    public ResponseEntity<LoginApiResponse> login(LoginRequest request) {
        log.info("Attempting to login user: email={}", request.getEmail());
        AuthResponse response = authService.login(request);
        log.info("Successfully logged in user: email={}", request.getEmail());
        return ResponseEntity.ok(apiResponseMapper.toSuccessResponse(response));
    }

    @Override
    public ResponseEntity<TokenRefreshApiResponse> refresh(TokenRefreshRequest request) {
        log.info("Attempting to refresh access token");
        AuthResponse response = authService.refresh(request);
        log.info("Successfully refreshed access token");
        return ResponseEntity.ok(apiResponseMapper.toRefreshResponse(response));
    }

    @Override
    public ResponseEntity<ForgotPasswordRequestApiResponse> requestForgotPasswordOtp(ForgotPasswordRequest request) {
        log.info("Attempting forgot password OTP request: email={}", request.getEmail());
        ForgotPasswordResponse response = authService.requestForgotPasswordOtp(request);
        log.info("Successfully processed forgot password OTP request: email={}", request.getEmail());
        return ResponseEntity.ok(apiResponseMapper.toForgotPasswordResponse(response));
    }

    @Override
    public ResponseEntity<ResetPasswordApiResponse> resetPasswordWithOtp(ResetPasswordRequest request) {
        log.info("Attempting password reset with OTP: email={}", request.getEmail());
        ForgotPasswordResponse response = authService.resetPasswordWithOtp(request);
        log.info("Successfully reset password with OTP: email={}", request.getEmail());
        return ResponseEntity.ok(apiResponseMapper.toResetPasswordResponse(response));
    }
}
