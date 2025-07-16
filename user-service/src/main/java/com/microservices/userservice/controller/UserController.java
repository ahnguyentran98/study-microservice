package com.microservices.userservice.controller;

import com.microservices.userservice.model.User;
import com.microservices.userservice.security.UserPrincipal;
import com.microservices.userservice.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
// PreAuthorize removed - authorization handled by API Gateway
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "*", maxAge = 3600)
public class UserController {
    
    private static final Logger logger = LoggerFactory.getLogger(UserController.class);
    
    @Autowired
    private UserService userService;
    
    @GetMapping("/profile")
    public ResponseEntity<?> getUserProfile(
            @RequestHeader("X-User-Email") String userEmail,
            @RequestHeader("X-User-Id") String userId,
            @RequestHeader("X-User-Role") String role) {
        
        log.info("Getting profile for user: {} (ID: {}, Role: {})", userEmail, userId, role);
        
        Optional<User> user = userService.findByEmail(userEmail);
        
        if (user.isPresent()) {
            User userProfile = user.get();
            userProfile.setPassword(null); // Don't expose password
            return ResponseEntity.ok(userProfile);
        } else {
            return ResponseEntity.notFound().build();
        }
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<?> getUserById(
            @PathVariable Long id,
            @RequestHeader("X-User-Email") String userEmail,
            @RequestHeader("X-User-Role") String role) {
        
        log.info("Getting user by ID: {} requested by: {} (Role: {})", id, userEmail, role);
        
        Optional<User> user = userService.findById(id);
        
        if (user.isPresent()) {
            User userProfile = user.get();
            userProfile.setPassword(null); // Don't expose password
            return ResponseEntity.ok(userProfile);
        } else {
            return ResponseEntity.notFound().build();
        }
    }
    
    @PutMapping("/profile")
    public ResponseEntity<?> updateUserProfile(
            @RequestBody User userDetails,
            @RequestHeader("X-User-Email") String userEmail,
            @RequestHeader("X-User-Id") String userId,
            @RequestHeader("X-User-Role") String role) {
        
        log.info("Updating profile for user: {} (ID: {})", userEmail, userId);
        
        try {
            User updatedUser = userService.updateUser(Long.parseLong(userId), userDetails);
            updatedUser.setPassword(null); // Don't expose password
            
            logger.info("User profile updated for ID: {}", userId);
            return ResponseEntity.ok(updatedUser);
        } catch (Exception e) {
            logger.error("Failed to update user profile: {}", e.getMessage());
            return ResponseEntity.badRequest().body(new AuthController.MessageResponse("Error: " + e.getMessage()));
        }
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteUser(
            @PathVariable Long id,
            @RequestHeader("X-User-Email") String userEmail,
            @RequestHeader("X-User-Role") String role) {
        
        log.info("Deleting user ID: {} requested by: {} (Role: {})", id, userEmail, role);
        
        try {
            userService.deleteUser(id);
            return ResponseEntity.ok(new AuthController.MessageResponse("User deleted successfully!"));
        } catch (Exception e) {
            logger.error("Failed to delete user: {}", e.getMessage());
            return ResponseEntity.badRequest().body(new AuthController.MessageResponse("Error: " + e.getMessage()));
        }
    }
}