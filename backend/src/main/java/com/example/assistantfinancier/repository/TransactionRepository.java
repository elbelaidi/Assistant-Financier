package com.example.assistantfinancier.repository;

import com.example.assistantfinancier.model.Transaction;
import com.example.assistantfinancier.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    List<Transaction> findByUserOrderByDateDesc(User user);
    
    List<Transaction> findByUserAndTypeOrderByDateDesc(User user, String type);
    
    List<Transaction> findByUserAndCategoryOrderByDateDesc(User user, String category);
    
    List<Transaction> findByUserAndDateBetweenOrderByDateDesc(User user, LocalDateTime start, LocalDateTime end);
    
    @Query("SELECT SUM(t.montant) FROM Transaction t WHERE t.user = ?1 AND t.type = ?2 AND t.status = 'completed'")
    Double sumByUserAndType(User user, String type);
    
    @Query("SELECT t.category, SUM(t.montant) FROM Transaction t WHERE t.user = ?1 AND t.type = 'expense' AND t.status = 'completed' GROUP BY t.category")
    List<Object[]> getExpensesByCategory(User user);
    
    Long countByUser(User user);
}
