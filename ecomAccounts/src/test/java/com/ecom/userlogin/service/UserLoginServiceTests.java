package com.ecom.userlogin.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;

import com.ecom.userlogin.dto.UserDTO;
import com.ecom.userlogin.dto.UserLoginDTO;
import com.ecom.userlogin.dto.UserRegistrationDTO;
import com.ecom.userlogin.entity.User;
import com.ecom.userlogin.repository.UserRepository;

class UserLoginServiceTests {

    private final UserRepository repository = mock(UserRepository.class);
    private final UserLoginService service = new UserLoginService(repository);

    @Test
    void returnsUserDetailsForValidCredentials() {
        User user = new User();
        user.setId(1L);
        user.setUserName("john_doe");
        user.setEmail("john@example.com");
        user.setPassword("password123");
        when(repository.findByEmailIgnoreCase("john@example.com")).thenReturn(Optional.of(user));

        UserLoginDTO request = new UserLoginDTO();
        request.setEmail(" john@example.com ");
        request.setPassword("password123");

        UserDTO result = service.login(request);

        assertEquals(1L, result.getId());
        assertEquals("john_doe", result.getUserName());
        assertEquals("john@example.com", result.getEmail());
    }

    @Test
    void rejectsInvalidPassword() {
        User user = new User();
        user.setEmail("john@example.com");
        user.setPassword("password123");
        when(repository.findByEmailIgnoreCase("john@example.com")).thenReturn(Optional.of(user));

        UserLoginDTO request = new UserLoginDTO();
        request.setEmail("john@example.com");
        request.setPassword("wrong-password");

        ResponseStatusException exception = assertThrows(
            ResponseStatusException.class,
            () -> service.login(request)
        );

        assertEquals(401, exception.getStatusCode().value());
    }

    @Test
    void rejectsMissingCredentials() {
        UserLoginDTO request = new UserLoginDTO();

        ResponseStatusException exception = assertThrows(
            ResponseStatusException.class,
            () -> service.login(request)
        );

        assertEquals(400, exception.getStatusCode().value());
    }

    @Test
    void registersNewUser() {
        when(repository.findByEmailIgnoreCase("new@example.com")).thenReturn(Optional.empty());
        when(repository.save(org.mockito.ArgumentMatchers.any(User.class)))
            .thenAnswer(invocation -> {
                User savedUser = invocation.getArgument(0);
                savedUser.setId(11L);
                return savedUser;
            });

        UserRegistrationDTO request = new UserRegistrationDTO();
        request.setUserName("new_user");
        request.setEmail(" new@example.com ");
        request.setPassword("password123");

        UserDTO result = service.register(request);

        assertEquals(11L, result.getId());
        assertEquals("new_user", result.getUserName());
        assertEquals("new@example.com", result.getEmail());
    }

    @Test
    void rejectsDuplicateEmailOnRegister() {
        User existingUser = new User();
        existingUser.setEmail("john@example.com");
        when(repository.findByEmailIgnoreCase("john@example.com")).thenReturn(Optional.of(existingUser));

        UserRegistrationDTO request = new UserRegistrationDTO();
        request.setUserName("john_doe");
        request.setEmail("john@example.com");
        request.setPassword("password123");

        ResponseStatusException exception = assertThrows(
            ResponseStatusException.class,
            () -> service.register(request)
        );

        assertEquals(409, exception.getStatusCode().value());
    }
}
