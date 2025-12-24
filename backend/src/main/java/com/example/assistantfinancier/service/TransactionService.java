package com.example.assistantfinancier.service;

import com.example.assistantfinancier.model.Transaction;
import com.example.assistantfinancier.model.User;
import com.example.assistantfinancier.repository.TransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class TransactionService {
    
    @Autowired
    private TransactionRepository transactionRepository;

    public Transaction createTransaction(Transaction transaction) {
        if (transaction.getDate() == null) {
            transaction.setDate(LocalDateTime.now());
        }
        if (transaction.getStatus() == null) {
            transaction.setStatus("completed");
        }
        return transactionRepository.save(transaction);
    }

    public Transaction updateTransaction(Long id, Transaction updatedTransaction) {
        Transaction transaction = transactionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Transaction not found"));
        
        transaction.setType(updatedTransaction.getType());
        transaction.setMontant(updatedTransaction.getMontant());
        transaction.setDescription(updatedTransaction.getDescription());
        transaction.setCategory(updatedTransaction.getCategory());
        transaction.setStatus(updatedTransaction.getStatus());
        transaction.setPaymentMethod(updatedTransaction.getPaymentMethod());
        transaction.setDate(updatedTransaction.getDate());
        
        return transactionRepository.save(transaction);
    }

    public void deleteTransaction(Long id) {
        transactionRepository.deleteById(id);
    }

    public List<Transaction> getUserTransactions(User user) {
        return transactionRepository.findByUserOrderByDateDesc(user);
    }

    public List<Transaction> getUserTransactionsByType(User user, String type) {
        return transactionRepository.findByUserAndTypeOrderByDateDesc(user, type);
    }

    public List<Transaction> getUserTransactionsByCategory(User user, String category) {
        return transactionRepository.findByUserAndCategoryOrderByDateDesc(user, category);
    }

    public List<Transaction> getUserTransactionsByDateRange(User user, LocalDateTime start, LocalDateTime end) {
        return transactionRepository.findByUserAndDateBetweenOrderByDateDesc(user, start, end);
    }

    public Map<String, Double> getUserFinancialSummary(User user) {
        Map<String, Double> summary = new HashMap<>();
        
        Double totalIncome = transactionRepository.sumByUserAndType(user, "income");
        Double totalExpenses = transactionRepository.sumByUserAndType(user, "expense");
        
        summary.put("totalIncome", totalIncome != null ? totalIncome : 0.0);
        summary.put("totalExpenses", totalExpenses != null ? totalExpenses : 0.0);
        summary.put("balance", (totalIncome != null ? totalIncome : 0.0) - (totalExpenses != null ? totalExpenses : 0.0));
        
        return summary;
    }

    public Map<String, Double> getExpensesByCategory(User user) {
        Map<String, Double> expensesByCategory = new HashMap<>();
        List<Object[]> results = transactionRepository.getExpensesByCategory(user);
        
        for (Object[] result : results) {
            String category = (String) result[0];
            Double amount = (Double) result[1];
            expensesByCategory.put(category != null ? category : "uncategorized", amount);
        }
        
        return expensesByCategory;
    }

    public Long getUserTransactionCount(User user) {
        return transactionRepository.countByUser(user);
    }
}
