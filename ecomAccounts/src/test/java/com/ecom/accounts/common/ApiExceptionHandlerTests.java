package com.ecom.accounts.common;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.server.ResponseStatusException;

class ApiExceptionHandlerTests {

    private final ApiExceptionHandler handler = new ApiExceptionHandler();

    @Test
    void returnsJsonBodyForResponseStatusException() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/users/register");

        ResponseEntity<Map<String, Object>> response = handler.handleResponseStatusException(
            new ResponseStatusException(HttpStatus.CONFLICT, "Email already registered"),
            request
        );

        assertEquals(409, response.getStatusCode().value());
        assertEquals("Email already registered", response.getBody().get("error"));
        assertEquals("/users/register", response.getBody().get("path"));
    }
}
