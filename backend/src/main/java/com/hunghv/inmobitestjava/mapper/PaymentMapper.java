package com.hunghv.inmobitestjava.mapper;

import com.hunghv.inmobitestjava.entity.PaymentTransaction;
import com.hunghv.inmobitestjava.generated.model.PaymentCheckoutResponse;
import com.hunghv.inmobitestjava.generated.model.PaymentConfirmationResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.net.URI;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface PaymentMapper {

    @Mapping(target = "paymentId", source = "id")
    @Mapping(target = "checkoutSessionId", source = "providerSessionId")
    @Mapping(target = "status", expression = "java(payment.getStatus().name())")
    PaymentCheckoutResponse toCheckoutResponse(PaymentTransaction payment);

    @Mapping(target = "paymentId", source = "id")
    @Mapping(target = "checkoutSessionId", source = "providerSessionId")
    @Mapping(target = "status", expression = "java(payment.getStatus().name())")
    @Mapping(target = "email", source = "user.email")
    PaymentConfirmationResponse toConfirmationResponse(PaymentTransaction payment);
    default URI toUri(String value) {
        return value == null ? null : URI.create(value);
    }
}
