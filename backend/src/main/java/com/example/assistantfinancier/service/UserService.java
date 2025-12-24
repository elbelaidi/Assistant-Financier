package com.example.assistantfinancier.service;

import com.example.assistantfinancier.model.User;
import com.example.assistantfinancier.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class UserService {
    
    @Autowired
    private UserRepository userRepository;
    
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    
    /**
     * Register a new user
     */
    public User registerUser(String nom, String email, String password, String telephone, String languePreferee) {
        // Check if email already exists
        if (userRepository.findByEmail(email).isPresent()) {
            throw new RuntimeException("Email already exists");
        }
        
        // Create new user
        User user = new User();
        user.setNom(nom);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));
        user.setTelephone(telephone);
        user.setLanguePreferee(languePreferee != null ? languePreferee : "fr");
        user.setRole("USER");
        user.setActive(true);
        user.setCreatedAt(LocalDateTime.now());
        
        return userRepository.save(user);
    }
    
    /**
     * Authenticate user (login)
     */
    public Optional<User> authenticateUser(String email, String password) {
        Optional<User> userOpt = userRepository.findByEmail(email);
        
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            
            // Check if account is active
            if (!user.getActive()) {
                throw new RuntimeException("Account is deactivated");
            }
            
            // Verify password
            if (passwordEncoder.matches(password, user.getPassword())) {
                // Update last login
                user.setLastLoginAt(LocalDateTime.now());
                userRepository.save(user);
                return Optional.of(user);
            }
        }
        
        return Optional.empty();
    }
    
    /**
     * Find user by email
     */
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }
    
    /**
     * Find user by ID
     */
    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }
    
    /**
     * Update user profile
     */
    public User updateProfile(Long userId, String nom, String telephone, String languePreferee) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        if (nom != null) user.setNom(nom);
        if (telephone != null) user.setTelephone(telephone);
        if (languePreferee != null) user.setLanguePreferee(languePreferee);
        
        return userRepository.save(user);
    }
    
    /**
     * Change user password
     */
    public void changePassword(Long userId, String currentPassword, String newPassword) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        // Verify current password
        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            throw new RuntimeException("Current password is incorrect");
        }
        
        // Validate new password
        if (newPassword == null || newPassword.length() < 6) {
            throw new RuntimeException("New password must be at least 6 characters");
        }
        
        // Update password
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }
    
    /**
     * Get all users (admin only)
     */
    public Iterable<User> getAllUsers() {
        return userRepository.findAll();
    }
    
    /**
     * Toggle user active status (admin only)
     */
    public User toggleUserStatus(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        user.setActive(!user.getActive());
        return userRepository.save(user);
    }
}
