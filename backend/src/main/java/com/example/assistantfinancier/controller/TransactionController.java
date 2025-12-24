package com.example.assistantfinancier.controller;

import com.example.assistantfinancier.dto.TransactionDTO;
import com.example.assistantfinancier.model.Transaction;
import com.example.assistantfinancier.model.User;
import com.example.assistantfinancier.security.JwtUtil;
import com.example.assistantfinancier.service.TransactionService;
import com.example.assistantfinancier.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/transactions")
@CrossOrigin(origins = "*")
public class TransactionController {

    @Autowired
    private TransactionService transactionService;

    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtil jwtUtil;

    @GetMapping
    public ResponseEntity<?> getUserTransactions(@RequestHeader("Authorization") String token) {
        try {
            String jwt = token.substring(7);
            String email = jwtUtil.getEmailFromToken(jwt);
            User user = userService.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            List<Transaction> transactions = transactionService.getUserTransactions(user);
            List<TransactionDTO> dtos = transactions.stream()
                    .map(t -> new TransactionDTO(t.getId(), t.getType(), t.getMontant(), t.getDescription(),
                            t.getDate(), t.getCategory(), t.getStatus(), t.getPaymentMethod(), t.getReference()))
                    .collect(Collectors.toList());

            return ResponseEntity.ok(dtos);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/filter")
    public ResponseEntity<?> filterTransactions(
            @RequestHeader("Authorization") String token,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        
        try {
            String jwt = token.substring(7);
            String email = jwtUtil.getEmailFromToken(jwt);
            User user = userService.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            List<Transaction> transactions;
            
            if (type != null && !type.isEmpty()) {
                transactions = transactionService.getUserTransactionsByType(user, type);
            } else if (category != null && !category.isEmpty()) {
                transactions = transactionService.getUserTransactionsByCategory(user, category);
            } else if (startDate != null && endDate != null) {
                transactions = transactionService.getUserTransactionsByDateRange(user, startDate, endDate);
            } else {
                transactions = transactionService.getUserTransactions(user);
            }

            List<TransactionDTO> dtos = transactions.stream()
                    .map(t -> new TransactionDTO(t.getId(), t.getType(), t.getMontant(), t.getDescription(),
                            t.getDate(), t.getCategory(), t.getStatus(), t.getPaymentMethod(), t.getReference()))
                    .collect(Collectors.toList());

            return ResponseEntity.ok(dtos);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping
    public ResponseEntity<?> createTransaction(
            @RequestHeader("Authorization") String token,
            @RequestBody TransactionDTO transactionDTO) {
        
        try {
            String jwt = token.substring(7);
            String email = jwtUtil.getEmailFromToken(jwt);
            User user = userService.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            Transaction transaction = new Transaction();
            transaction.setUser(user);
            transaction.setType(transactionDTO.getType());
            transaction.setMontant(transactionDTO.getMontant());
            transaction.setDescription(transactionDTO.getDescription());
            transaction.setCategory(transactionDTO.getCategory());
            transaction.setStatus(transactionDTO.getStatus());
            transaction.setPaymentMethod(transactionDTO.getPaymentMethod());
            transaction.setDate(transactionDTO.getDate() != null ? transactionDTO.getDate() : LocalDateTime.now());

            Transaction saved = transactionService.createTransaction(transaction);
            
            TransactionDTO responseDTO = new TransactionDTO(saved.getId(), saved.getType(), saved.getMontant(),
                    saved.getDescription(), saved.getDate(), saved.getCategory(), saved.getStatus(),
                    saved.getPaymentMethod(), saved.getReference());

            return ResponseEntity.ok(responseDTO);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateTransaction(
            @RequestHeader("Authorization") String token,
            @PathVariable Long id,
            @RequestBody TransactionDTO transactionDTO) {
        
        try {
            Transaction transaction = new Transaction();
            transaction.setType(transactionDTO.getType());
            transaction.setMontant(transactionDTO.getMontant());
            transaction.setDescription(transactionDTO.getDescription());
            transaction.setCategory(transactionDTO.getCategory());
            transaction.setStatus(transactionDTO.getStatus());
            transaction.setPaymentMethod(transactionDTO.getPaymentMethod());
            transaction.setDate(transactionDTO.getDate());

            Transaction updated = transactionService.updateTransaction(id, transaction);
            
            TransactionDTO responseDTO = new TransactionDTO(updated.getId(), updated.getType(), updated.getMontant(),
                    updated.getDescription(), updated.getDate(), updated.getCategory(), updated.getStatus(),
                    updated.getPaymentMethod(), updated.getReference());

            return ResponseEntity.ok(responseDTO);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteTransaction(
            @RequestHeader("Authorization") String token,
            @PathVariable Long id) {
        
        try {
            transactionService.deleteTransaction(id);
            return ResponseEntity.ok(Map.of("message", "Transaction deleted successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/summary")
    public ResponseEntity<?> getFinancialSummary(@RequestHeader("Authorization") String token) {
        try {
            String jwt = token.substring(7);
            String email = jwtUtil.getEmailFromToken(jwt);
            User user = userService.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            Map<String, Double> summary = transactionService.getUserFinancialSummary(user);
            return ResponseEntity.ok(summary);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/expenses-by-category")
    public ResponseEntity<?> getExpensesByCategory(@RequestHeader("Authorization") String token) {
        try {
            String jwt = token.substring(7);
            String email = jwtUtil.getEmailFromToken(jwt);
            User user = userService.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            Map<String, Double> expensesByCategory = transactionService.getExpensesByCategory(user);
            return ResponseEntity.ok(expensesByCategory);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
