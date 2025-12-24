package com.example.assistantfinancier.repository;

import com.example.assistantfinancier.model.Conversation;
import com.example.assistantfinancier.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ConversationRepository extends JpaRepository<Conversation, Long> {
    List<Conversation> findByUserOrderByTimestampDesc(User user);
    
    List<Conversation> findByUserAndLanguageOrderByTimestampDesc(User user, String language);
    
    List<Conversation> findByUserAndCategoryOrderByTimestampDesc(User user, String category);
    
    List<Conversation> findByUserAndTimestampBetweenOrderByTimestampDesc(User user, LocalDateTime start, LocalDateTime end);
    
    Long countByUser(User user);
}
