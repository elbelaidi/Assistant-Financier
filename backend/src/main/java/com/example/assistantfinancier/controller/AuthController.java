package com.example.assistantfinancier.controller;

import com.example.assistantfinancier.dto.*;
import com.example.assistantfinancier.model.User;
import com.example.assistantfinancier.security.JwtUtil;
import com.example.assistantfinancier.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private JwtUtil jwtUtil;
    
    /**
     * Register a new user
     */
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request) {
        try {
            // Validate input
            if (request.getEmail() == null || request.getEmail().trim().isEmpty()) {
                return ResponseEntity.badRequest().body(createErrorResponse("Email is required"));
            }
            if (request.getPassword() == null || request.getPassword().length() < 6) {
                return ResponseEntity.badRequest().body(createErrorResponse("Password must be at least 6 characters"));
            }
            if (request.getNom() == null || request.getNom().trim().isEmpty()) {
                return ResponseEntity.badRequest().body(createErrorResponse("Name is required"));
            }
            
            // Register user
            User user = userService.registerUser(
                request.getNom(),
                request.getEmail(),
                request.getPassword(),
                request.getTelephone(),
                request.getLanguePreferee()
            );
            
            // Generate token
            String token = jwtUtil.generateToken(user.getEmail(), user.getRole());
            
            // Create response
            AuthResponse response = new AuthResponse(
                token,
                user.getEmail(),
                user.getNom(),
                user.getRole(),
                user.getLanguePreferee()
            );
            
            return ResponseEntity.ok(response);
            
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(createErrorResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("An error occurred during registration"));
        }
    }
    
    /**
     * Login user
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        try {
            // Validate input
            if (request.getEmail() == null || request.getEmail().trim().isEmpty()) {
                return ResponseEntity.badRequest().body(createErrorResponse("Email is required"));
            }
            if (request.getPassword() == null || request.getPassword().trim().isEmpty()) {
                return ResponseEntity.badRequest().body(createErrorResponse("Password is required"));
            }
            
            // Authenticate user
            Optional<User> userOpt = userService.authenticateUser(request.getEmail(), request.getPassword());
            
            if (userOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(createErrorResponse("Invalid email or password"));
            }
            
            User user = userOpt.get();
            
            // Generate token
            String token = jwtUtil.generateToken(user.getEmail(), user.getRole());
            
            // Create response
            AuthResponse response = new AuthResponse(
                token,
                user.getEmail(),
                user.getNom(),
                user.getRole(),
                user.getLanguePreferee()
            );
            
            return ResponseEntity.ok(response);
            
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(createErrorResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("An error occurred during login"));
        }
    }
    
    /**
     * Get current user info from token
     */
    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(@RequestHeader("Authorization") String authHeader) {
        try {
            // Extract token
            String token = authHeader.replace("Bearer ", "");
            
            // Validate token
            if (!jwtUtil.validateToken(token)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(createErrorResponse("Invalid or expired token"));
            }
            
            // Get email from token
            String email = jwtUtil.getEmailFromToken(token);
            
            // Find user
            Optional<User> userOpt = userService.findByEmail(email);
            if (userOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(createErrorResponse("User not found"));
            }
            
            User user = userOpt.get();
            
            // Create response (without token)
            Map<String, Object> response = new HashMap<>();
            response.put("email", user.getEmail());
            response.put("nom", user.getNom());
            response.put("role", user.getRole());
            response.put("languePreferee", user.getLanguePreferee());
            response.put("telephone", user.getTelephone());
            response.put("active", user.getActive());
            response.put("createdAt", user.getCreatedAt());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("An error occurred"));
        }
    }
    
    /**
     * Update user profile
     */
    @PutMapping("/profile")
    public ResponseEntity<?> updateProfile(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody UpdateProfileRequest request) {
        try {
            String token = authHeader.replace("Bearer ", "");
            String email = jwtUtil.getEmailFromToken(token);
            
            User user = userService.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            
            User updatedUser = userService.updateProfile(
                user.getId(),
                request.getNom(),
                request.getTelephone(),
                request.getLanguePreferee()
            );
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Profile updated successfully");
            response.put("nom", updatedUser.getNom());
            response.put("telephone", updatedUser.getTelephone());
            response.put("languePreferee", updatedUser.getLanguePreferee());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(createErrorResponse(e.getMessage()));
        }
    }
    
    /**
     * Change password
     */
    @PostMapping("/change-password")
    public ResponseEntity<?> changePassword(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody ChangePasswordRequest request) {
        try {
            String token = authHeader.replace("Bearer ", "");
            String email = jwtUtil.getEmailFromToken(token);
            
            User user = userService.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            
            userService.changePassword(
                user.getId(),
                request.getCurrentPassword(),
                request.getNewPassword()
            );
            
            return ResponseEntity.ok(Map.of("message", "Password changed successfully"));
            
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(createErrorResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("An error occurred while changing password"));
        }
    }
    
    /**
     * Helper method to create error response
     */
    private Map<String, String> createErrorResponse(String message) {
        Map<String, String> error = new HashMap<>();
        error.put("error", message);
        return error;
    }
}
