package com.hunghv.inmobitestjava.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hunghv.inmobitestjava.constant.ResponseMessage;
import com.hunghv.inmobitestjava.mapper.ApiResponseMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class RestAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper;
    private final ApiResponseMapper apiResponseMapper;

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException)
        throws IOException {

        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        objectMapper.writeValue(
            response.getOutputStream(),
            apiResponseMapper.toErrorResponse(HttpStatus.UNAUTHORIZED, ResponseMessage.AUTHENTICATION_REQUIRED, null)
        );
    }
}
