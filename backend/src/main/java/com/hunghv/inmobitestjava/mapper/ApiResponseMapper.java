package com.hunghv.inmobitestjava.mapper;

import com.hunghv.inmobitestjava.constant.ResponseMessage;
import com.hunghv.inmobitestjava.generated.model.ApiErrorDetail;
import com.hunghv.inmobitestjava.generated.model.AuthResponse;
import com.hunghv.inmobitestjava.generated.model.CurrentUserApiResponse;
import com.hunghv.inmobitestjava.generated.model.CurrentUserResponse;
import com.hunghv.inmobitestjava.generated.model.ErrorApiResponse;
import com.hunghv.inmobitestjava.generated.model.ForgotPasswordRequestApiResponse;
import com.hunghv.inmobitestjava.generated.model.ForgotPasswordResponse;
import com.hunghv.inmobitestjava.generated.model.GuessApiResponse;
import com.hunghv.inmobitestjava.generated.model.GuessResponse;
import com.hunghv.inmobitestjava.generated.model.LeaderboardApiResponse;
import com.hunghv.inmobitestjava.generated.model.LeaderboardResponse;
import com.hunghv.inmobitestjava.generated.model.LoginApiResponse;
import com.hunghv.inmobitestjava.generated.model.PaymentCheckoutApiResponse;
import com.hunghv.inmobitestjava.generated.model.PaymentCheckoutResponse;
import com.hunghv.inmobitestjava.generated.model.PaymentConfirmationApiResponse;
import com.hunghv.inmobitestjava.generated.model.PaymentConfirmationResponse;
import com.hunghv.inmobitestjava.generated.model.RegisterApiResponse;
import com.hunghv.inmobitestjava.generated.model.RegisterResponse;
import com.hunghv.inmobitestjava.generated.model.ResetPasswordApiResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import org.springframework.http.HttpStatus;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE, imports = {HttpStatus.class, ResponseMessage.class})
public interface ApiResponseMapper {

    @Mapping(target = "code", expression = "java(HttpStatus.CREATED.value())")
    @Mapping(target = "message", expression = "java(ResponseMessage.CREATED)")
    @Mapping(target = "data", source = "data")
    RegisterApiResponse toCreatedResponse(RegisterResponse data);

    @Mapping(target = "code", expression = "java(HttpStatus.OK.value())")
    @Mapping(target = "message", expression = "java(ResponseMessage.SUCCESS)")
    @Mapping(target = "data", source = "data")
    LoginApiResponse toSuccessResponse(AuthResponse data);

    @Mapping(target = "code", expression = "java(HttpStatus.OK.value())")
    @Mapping(target = "message", expression = "java(ResponseMessage.SUCCESS)")
    @Mapping(target = "data", source = "data")
    ForgotPasswordRequestApiResponse toForgotPasswordResponse(ForgotPasswordResponse data);

    @Mapping(target = "code", expression = "java(HttpStatus.OK.value())")
    @Mapping(target = "message", expression = "java(ResponseMessage.SUCCESS)")
    @Mapping(target = "data", source = "data")
    ResetPasswordApiResponse toResetPasswordResponse(ForgotPasswordResponse data);

    @Mapping(target = "code", expression = "java(HttpStatus.OK.value())")
    @Mapping(target = "message", expression = "java(ResponseMessage.SUCCESS)")
    @Mapping(target = "data", source = "data")
    CurrentUserApiResponse toSuccessResponse(CurrentUserResponse data);

    @Mapping(target = "code", expression = "java(HttpStatus.OK.value())")
    @Mapping(target = "message", expression = "java(ResponseMessage.SUCCESS)")
    @Mapping(target = "data", source = "data")
    GuessApiResponse toSuccessResponse(GuessResponse data);

    @Mapping(target = "code", expression = "java(HttpStatus.OK.value())")
    @Mapping(target = "message", expression = "java(ResponseMessage.SUCCESS)")
    @Mapping(target = "data", source = "data")
    PaymentCheckoutApiResponse toSuccessResponse(PaymentCheckoutResponse data);

    @Mapping(target = "code", expression = "java(HttpStatus.OK.value())")
    @Mapping(target = "message", expression = "java(ResponseMessage.SUCCESS)")
    @Mapping(target = "data", source = "data")
    PaymentConfirmationApiResponse toSuccessResponse(PaymentConfirmationResponse data);

    @Mapping(target = "code", source = "code")
    @Mapping(target = "message", source = "message")
    @Mapping(target = "data", source = "data")
    LeaderboardApiResponse toLeaderboardResponse(Integer code, String message, List<LeaderboardResponse> data);

    default LeaderboardApiResponse toLeaderboardResponse(List<LeaderboardResponse> data) {
        return toLeaderboardResponse(HttpStatus.OK.value(), ResponseMessage.SUCCESS, data);
    }

    @Mapping(target = "code", expression = "java(status.value())")
    @Mapping(target = "message", source = "message")
    @Mapping(target = "data", source = "data")
    ErrorApiResponse toErrorResponse(HttpStatus status, String message, ApiErrorDetail data);
}
