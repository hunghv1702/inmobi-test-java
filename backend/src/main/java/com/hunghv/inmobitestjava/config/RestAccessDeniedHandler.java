package com.hunghv.inmobitestjava.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hunghv.inmobitestjava.constant.ResponseMessage;
import com.hunghv.inmobitestjava.mapper.ApiResponseMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class RestAccessDeniedHandler implements AccessDeniedHandler {

    private final ObjectMapper objectMapper;
    private final ApiResponseMapper apiResponseMapper;

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException)
        throws IOException {

        response.setStatus(HttpStatus.FORBIDDEN.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        objectMapper.writeValue(
            response.getOutputStream(),
            apiResponseMapper.toErrorResponse(HttpStatus.FORBIDDEN, ResponseMessage.ACCESS_DENIED, null)
        );
    }
}
