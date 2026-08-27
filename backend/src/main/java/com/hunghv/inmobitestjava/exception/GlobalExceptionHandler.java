package com.hunghv.inmobitestjava.exception;

import com.hunghv.inmobitestjava.constant.ResponseMessage;
import com.hunghv.inmobitestjava.generated.model.ApiErrorDetail;
import com.hunghv.inmobitestjava.generated.model.ErrorApiResponse;
import com.hunghv.inmobitestjava.mapper.ApiResponseMapper;
import lombok.RequiredArgsConstructor;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;

@Slf4j
@RestControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler {

    private final ApiResponseMapper apiResponseMapper;

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ErrorApiResponse> handleApiException(ApiException ex, HttpServletRequest request) {
        log.error("API exception occurred: status={}, uri={}, message={}", ex.getStatus().value(), request.getRequestURI(), ex.getMessage());
        return ResponseEntity.status(ex.getStatus())
            .body(apiResponseMapper.toErrorResponse(ex.getStatus(), ex.getMessage(), null));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorApiResponse> handleValidation(MethodArgumentNotValidException ex, HttpServletRequest request) {
        List<String> violations = ex.getBindingResult().getFieldErrors().stream()
            .map(error -> error.getField() + ": " + error.getDefaultMessage())
            .toList();
        log.error("Validation exception occurred: uri={}, violations={}", request.getRequestURI(), violations);

        ApiErrorDetail detail = new ApiErrorDetail().violations(violations);

        return ResponseEntity.badRequest()
            .body(apiResponseMapper.toErrorResponse(HttpStatus.BAD_REQUEST, ResponseMessage.REQUEST_VALIDATION_FAILED, detail));
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorApiResponse> handleConstraintViolation(ConstraintViolationException ex, HttpServletRequest request) {
        List<String> violations = ex.getConstraintViolations().stream()
            .map(violation -> violation.getPropertyPath() + ": " + violation.getMessage())
            .toList();
        log.error("Constraint violation occurred: uri={}, violations={}", request.getRequestURI(), violations);

        ApiErrorDetail detail = new ApiErrorDetail().violations(violations);

        return ResponseEntity.badRequest()
            .body(apiResponseMapper.toErrorResponse(HttpStatus.BAD_REQUEST, ResponseMessage.REQUEST_VALIDATION_FAILED, detail));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorApiResponse> handleUnexpected(Exception ex, HttpServletRequest request) {
        log.error("Unhandled error occurred: uri={}", request.getRequestURI(), ex);
        return ResponseEntity.internalServerError()
            .body(apiResponseMapper.toErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, ResponseMessage.UNEXPECTED_ERROR, null));
    }
}
