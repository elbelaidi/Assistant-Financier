package com.example.assistantfinancier.controller;

import com.example.assistantfinancier.model.User;
import com.example.assistantfinancier.security.JwtUtil;
import com.example.assistantfinancier.service.ConversationService;
import com.example.assistantfinancier.service.TransactionService;
import com.example.assistantfinancier.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@CrossOrigin(origins = "*")
public class AdminController {

    @Autowired
    private UserService userService;

    @Autowired
    private TransactionService transactionService;

    @Autowired
    private ConversationService conversationService;

    @Autowired
    private JwtUtil jwtUtil;

    /**
     * Check if user is admin
     */
    private boolean isAdmin(String token) {
        try {
            String jwt = token.substring(7);
            String role = jwtUtil.getRoleFromToken(jwt);
            return "ADMIN".equals(role);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Get all users (admin only)
     */
    @GetMapping("/users")
    public ResponseEntity<?> getAllUsers(@RequestHeader("Authorization") String token) {
        try {
            if (!isAdmin(token)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("error", "Admin access required"));
            }

            Iterable<User> users = userService.getAllUsers();
            List<Map<String, Object>> userList = new ArrayList<>();

            for (User user : users) {
                Map<String, Object> userMap = new HashMap<>();
                userMap.put("id", user.getId());
                userMap.put("nom", user.getNom());
                userMap.put("email", user.getEmail());
                userMap.put("telephone", user.getTelephone());
                userMap.put("role", user.getRole());
                userMap.put("active", user.getActive());
                userMap.put("languePreferee", user.getLanguePreferee());
                userMap.put("createdAt", user.getCreatedAt());
                userMap.put("lastLoginAt", user.getLastLoginAt());
                
                // Add user statistics
                userMap.put("transactionCount", transactionService.getUserTransactionCount(user));
                userMap.put("conversationCount", conversationService.getUserConversationCount(user));
                
                userList.add(userMap);
            }

            return ResponseEntity.ok(userList);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Toggle user active status (admin only)
     */
    @PostMapping("/users/{userId}/toggle-status")
    public ResponseEntity<?> toggleUserStatus(
            @RequestHeader("Authorization") String token,
            @PathVariable Long userId) {
        
        try {
            if (!isAdmin(token)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("error", "Admin access required"));
            }

            User user = userService.toggleUserStatus(userId);
            return ResponseEntity.ok(Map.of(
                "message", "User status updated",
                "userId", user.getId(),
                "active", user.getActive()
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Get system statistics (admin only)
     */
    @GetMapping("/stats")
    public ResponseEntity<?> getSystemStats(@RequestHeader("Authorization") String token) {
        try {
            if (!isAdmin(token)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("error", "Admin access required"));
            }

            Map<String, Object> stats = new HashMap<>();
            
            // Count users
            Iterable<User> users = userService.getAllUsers();
            long totalUsers = 0;
            long activeUsers = 0;
            long totalTransactions = 0;
            long totalConversations = 0;

            for (User user : users) {
                totalUsers++;
                if (user.getActive()) activeUsers++;
                totalTransactions += transactionService.getUserTransactionCount(user);
                totalConversations += conversationService.getUserConversationCount(user);
            }

            stats.put("totalUsers", totalUsers);
            stats.put("activeUsers", activeUsers);
            stats.put("totalTransactions", totalTransactions);
            stats.put("totalConversations", totalConversations);

            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Get user details (admin only)
     */
    @GetMapping("/users/{userId}")
    public ResponseEntity<?> getUserDetails(
            @RequestHeader("Authorization") String token,
            @PathVariable Long userId) {
        
        try {
            if (!isAdmin(token)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("error", "Admin access required"));
            }

            User user = userService.findById(userId)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            Map<String, Object> userDetails = new HashMap<>();
            userDetails.put("id", user.getId());
            userDetails.put("nom", user.getNom());
            userDetails.put("email", user.getEmail());
            userDetails.put("telephone", user.getTelephone());
            userDetails.put("role", user.getRole());
            userDetails.put("active", user.getActive());
            userDetails.put("languePreferee", user.getLanguePreferee());
            userDetails.put("createdAt", user.getCreatedAt());
            userDetails.put("lastLoginAt", user.getLastLoginAt());

            // Financial summary
            Map<String, Double> financialSummary = transactionService.getUserFinancialSummary(user);
            userDetails.put("financialSummary", financialSummary);

            // Transaction and conversation counts
            userDetails.put("transactionCount", transactionService.getUserTransactionCount(user));
            userDetails.put("conversationCount", conversationService.getUserConversationCount(user));

            return ResponseEntity.ok(userDetails);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
