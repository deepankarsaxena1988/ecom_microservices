package com.ecom.accounts.common;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

import jakarta.servlet.http.HttpServletRequest;

@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<Map<String, Object>> handleResponseStatusException(
        ResponseStatusException exception,
        HttpServletRequest request
    ) {
        Map<String, Object> errorBody = new LinkedHashMap<>();
        errorBody.put("timestamp", Instant.now());
        errorBody.put("status", exception.getStatusCode().value());
        errorBody.put("error", exception.getReason());
        errorBody.put("path", request.getRequestURI());

        return ResponseEntity.status(exception.getStatusCode()).body(errorBody);
    }
}
