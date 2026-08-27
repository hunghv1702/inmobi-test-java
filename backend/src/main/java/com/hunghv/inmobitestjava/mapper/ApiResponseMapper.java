package com.hunghv.inmobitestjava.mapper;

import com.hunghv.inmobitestjava.generated.model.*;
import com.hunghv.inmobitestjava.util.ApiResponseUtils;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import java.util.List;

@Component
public class ApiResponseMapper {

    public RegisterApiResponse toCreatedResponse(RegisterResponse data) {
        return ApiResponseUtils.created(RegisterApiResponse.class, data);
    }

    public LoginApiResponse toSuccessResponse(AuthResponse data) {
        return ApiResponseUtils.success(LoginApiResponse.class, data);
    }

    public TokenRefreshApiResponse toRefreshResponse(AuthResponse data) {
        return ApiResponseUtils.success(TokenRefreshApiResponse.class, data);
    }

    public ForgotPasswordRequestApiResponse toForgotPasswordResponse(ForgotPasswordResponse data) {
        return ApiResponseUtils.success(ForgotPasswordRequestApiResponse.class, data);
    }

    public ResetPasswordApiResponse toResetPasswordResponse(ForgotPasswordResponse data) {
        return ApiResponseUtils.success(ResetPasswordApiResponse.class, data);
    }

    public CurrentUserApiResponse toSuccessResponse(CurrentUserResponse data) {
        return ApiResponseUtils.success(CurrentUserApiResponse.class, data);
    }

    public GuessApiResponse toSuccessResponse(GuessResponse data) {
        return ApiResponseUtils.success(GuessApiResponse.class, data);
    }

    public PaymentCheckoutApiResponse toSuccessResponse(PaymentCheckoutResponse data) {
        return ApiResponseUtils.success(PaymentCheckoutApiResponse.class, data);
    }

    public PaymentConfirmationApiResponse toSuccessResponse(PaymentConfirmationResponse data) {
        return ApiResponseUtils.success(PaymentConfirmationApiResponse.class, data);
    }

    public LeaderboardApiResponse toLeaderboardResponse(List<LeaderboardResponse> data) {
        return ApiResponseUtils.success(LeaderboardApiResponse.class, data);
    }

    public ErrorApiResponse toErrorResponse(HttpStatus status, String message, ApiErrorDetail data) {
        return ApiResponseUtils.createResponse(ErrorApiResponse.class, status, message, data);
    }
}
