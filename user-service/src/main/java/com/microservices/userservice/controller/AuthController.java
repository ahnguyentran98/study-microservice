package com.microservices.userservice.controller;

import com.microservices.userservice.dto.JwtResponse;
import com.microservices.userservice.dto.LoginRequest;
import com.microservices.userservice.dto.UserRegistrationRequest;
import com.microservices.userservice.model.User;
import com.microservices.userservice.security.JwtUtils;
import com.microservices.userservice.security.UserPrincipal;
import com.microservices.userservice.service.UserService;
import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "*", maxAge = 3600)
public class AuthController {
    
    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);
    
    @Autowired
    private AuthenticationManager authenticationManager;
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private JwtUtils jwtUtils;
    
    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@Valid @RequestBody UserRegistrationRequest registrationRequest) {
        logger.info("Registration request received for email: {}", registrationRequest.getEmail());
        
        try {
            User user = userService.registerUser(registrationRequest);
            
            return ResponseEntity.ok(new MessageResponse("User registered successfully!"));
        } catch (Exception e) {
            logger.error("Registration failed: {}", e.getMessage());
            return ResponseEntity.badRequest().body(new MessageResponse("Error: " + e.getMessage()));
        }
    }
    
    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
        logger.info("Login request received for email: {}", loginRequest.getEmail());
        
        try {
            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword())
            );
            
            SecurityContextHolder.getContext().setAuthentication(authentication);
            String jwt = jwtUtils.generateJwtToken(authentication);
            
            UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
            
            User user = userService.findByEmail(userPrincipal.getEmail())
                    .orElseThrow(() -> new RuntimeException("User not found"));
            
            JwtResponse jwtResponse = new JwtResponse(
                jwt,
                userPrincipal.getId(),
                userPrincipal.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                user.getRole().name()
            );
            
            logger.info("User logged in successfully: {}", loginRequest.getEmail());
            return ResponseEntity.ok(jwtResponse);
            
        } catch (Exception e) {
            logger.error("Login failed for email {}: {}", loginRequest.getEmail(), e.getMessage());
            return ResponseEntity.badRequest().body(new MessageResponse("Error: Invalid email or password!"));
        }
    }
    
    @Setter
    @Getter
    public static class MessageResponse {
        private String message;
        public MessageResponse(String message) {
            this.message = message;
        }
    }
}