package com.ecom.userlogin.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ecom.userlogin.dto.UserDTO;
import com.ecom.userlogin.dto.UserLoginDTO;
import com.ecom.userlogin.dto.UserRegistrationDTO;
import com.ecom.userlogin.service.UserLoginService;

@RestController
@RequestMapping("/users")
public class UserLoginController {

    private final UserLoginService userLoginService;

    public UserLoginController(UserLoginService userLoginService) {
        this.userLoginService = userLoginService;
    }

    @PostMapping("/login")
    public ResponseEntity<UserDTO> login(@RequestBody UserLoginDTO loginRequest) {
        return ResponseEntity.ok(userLoginService.login(loginRequest));
    }

    @PostMapping("/register")
    public ResponseEntity<UserDTO> register(@RequestBody UserRegistrationDTO registrationRequest) {
        return ResponseEntity.ok(userLoginService.register(registrationRequest));
    }
}
