package com.twitter.api.controller;

import com.twitter.api.dto.JwtResponse;
import com.twitter.api.dto.LoginRequest;
import com.twitter.api.dto.MessageResponse;
import com.twitter.api.dto.SignupRequest;
import com.twitter.api.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/")
public class AuthController {
    
    @Autowired
    private AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
        JwtResponse jwtResponse = authService.authenticateUser(loginRequest);
        return ResponseEntity.ok(jwtResponse);
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@Valid @RequestBody SignupRequest signUpRequest) {
        MessageResponse messageResponse = authService.registerUser(signUpRequest);
        
        if (messageResponse.getMessage().startsWith("Error")) {
            return ResponseEntity.badRequest().body(messageResponse);
        }
        
        return ResponseEntity.ok(messageResponse);
    }
} 