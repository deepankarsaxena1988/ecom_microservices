package com.ecom.userlogin.service;

import java.util.Objects;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.ecom.userlogin.dto.UserDTO;
import com.ecom.userlogin.dto.UserLoginDTO;
import com.ecom.userlogin.dto.UserRegistrationDTO;
import com.ecom.userlogin.entity.User;
import com.ecom.userlogin.repository.UserRepository;

@Service
public class UserLoginService {

    private final UserRepository userRepository;

    public UserLoginService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public UserDTO login(UserLoginDTO loginRequest) {
        if (loginRequest.getEmail() == null || loginRequest.getEmail().isBlank()
            || loginRequest.getPassword() == null || loginRequest.getPassword().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email and password are required");
        }

        User user = userRepository.findByEmailIgnoreCase(loginRequest.getEmail().trim())
            .filter(foundUser -> Objects.equals(foundUser.getPassword(), loginRequest.getPassword()))
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.UNAUTHORIZED,
                "Invalid email or password"
            ));

        return toDTO(user);
    }

    public UserDTO register(UserRegistrationDTO registrationRequest) {
        if (registrationRequest.getUserName() == null || registrationRequest.getUserName().isBlank()
            || registrationRequest.getEmail() == null || registrationRequest.getEmail().isBlank()
            || registrationRequest.getPassword() == null || registrationRequest.getPassword().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "User name, email and password are required");
        }

        String email = registrationRequest.getEmail().trim();
        if (userRepository.findByEmailIgnoreCase(email).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already registered");
        }

        User user = new User();
        user.setUserName(registrationRequest.getUserName().trim());
        user.setEmail(email);
        user.setPassword(registrationRequest.getPassword());

        return toDTO(userRepository.save(user));
    }

    private UserDTO toDTO(User user) {
        UserDTO userDTO = new UserDTO();
        userDTO.setId(user.getId());
        userDTO.setUserName(user.getUserName());
        userDTO.setEmail(user.getEmail());
        return userDTO;
    }
}
