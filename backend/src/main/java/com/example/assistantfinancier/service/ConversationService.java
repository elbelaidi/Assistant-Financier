

package com.example.assistantfinancier.service;

import com.example.assistantfinancier.model.Conversation;
import com.example.assistantfinancier.model.User;
import com.example.assistantfinancier.repository.ConversationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ConversationService {
    
    private static final Logger logger = LoggerFactory.getLogger(ConversationService.class);
    
    @Autowired
    private ConversationRepository conversationRepository;

    public Conversation saveConversation(User user, String query, String response, String language, String category) {
        logger.info("CONVERSATION_SAVE_START: UserID={}, Language={}, Category={}, QueryLength={}", 
                   user.getId(), language, category, query.length());
        
        try {
            Conversation conversation = new Conversation(user, query, response, language);
            conversation.setCategory(category);
            
            Conversation savedConversation = conversationRepository.save(conversation);
            
            logger.info("CONVERSATION_SAVE_SUCCESS: ConversationID={}, UserID={}, Timestamp={}", 
                       savedConversation.getId(), user.getId(), savedConversation.getTimestamp());
            
            return savedConversation;
        } catch (Exception e) {
            logger.error("CONVERSATION_SAVE_FAILED: UserID={}, Error={}, Query={}", 
                        user.getId(), e.getMessage(), query.substring(0, Math.min(query.length(), 100)));
            throw e;
        }
    }


    public List<Conversation> getUserConversations(User user) {
        logger.info("CONVERSATION_RETRIEVE_START: UserID={}, Method=findByUserOrderByTimestampDesc", user.getId());
        
        try {
            List<Conversation> conversations = conversationRepository.findByUserOrderByTimestampDesc(user);
            
            logger.info("CONVERSATION_RETRIEVE_SUCCESS: UserID={}, Count={}, Method=findByUserOrderByTimestampDesc", 
                       user.getId(), conversations.size());
            
            return conversations;
        } catch (Exception e) {
            logger.error("CONVERSATION_RETRIEVE_FAILED: UserID={}, Error={}, Method=findByUserOrderByTimestampDesc", 
                        user.getId(), e.getMessage());
            throw e;
        }
    }

    public List<Conversation> getUserConversationsByLanguage(User user, String language) {
        logger.info("CONVERSATION_RETRIEVE_START: UserID={}, Language={}, Method=findByUserAndLanguageOrderByTimestampDesc", 
                   user.getId(), language);
        
        try {
            List<Conversation> conversations = conversationRepository.findByUserAndLanguageOrderByTimestampDesc(user, language);
            
            logger.info("CONVERSATION_RETRIEVE_SUCCESS: UserID={}, Language={}, Count={}, Method=findByUserAndLanguageOrderByTimestampDesc", 
                       user.getId(), language, conversations.size());
            
            return conversations;
        } catch (Exception e) {
            logger.error("CONVERSATION_RETRIEVE_FAILED: UserID={}, Language={}, Error={}, Method=findByUserAndLanguageOrderByTimestampDesc", 
                        user.getId(), language, e.getMessage());
            throw e;
        }
    }

    public List<Conversation> getUserConversationsByCategory(User user, String category) {
        logger.info("CONVERSATION_RETRIEVE_START: UserID={}, Category={}, Method=findByUserAndCategoryOrderByTimestampDesc", 
                   user.getId(), category);
        
        try {
            List<Conversation> conversations = conversationRepository.findByUserAndCategoryOrderByTimestampDesc(user, category);
            
            logger.info("CONVERSATION_RETRIEVE_SUCCESS: UserID={}, Category={}, Count={}, Method=findByUserAndCategoryOrderByTimestampDesc", 
                       user.getId(), category, conversations.size());
            
            return conversations;
        } catch (Exception e) {
            logger.error("CONVERSATION_RETRIEVE_FAILED: UserID={}, Category={}, Error={}, Method=findByUserAndCategoryOrderByTimestampDesc", 
                        user.getId(), category, e.getMessage());
            throw e;
        }
    }

    public List<Conversation> getUserConversationsByDateRange(User user, LocalDateTime start, LocalDateTime end) {
        logger.info("CONVERSATION_RETRIEVE_START: UserID={}, StartDate={}, EndDate={}, Method=findByUserAndTimestampBetweenOrderByTimestampDesc", 
                   user.getId(), start, end);
        
        try {
            List<Conversation> conversations = conversationRepository.findByUserAndTimestampBetweenOrderByTimestampDesc(user, start, end);
            
            logger.info("CONVERSATION_RETRIEVE_SUCCESS: UserID={}, StartDate={}, EndDate={}, Count={}, Method=findByUserAndTimestampBetweenOrderByTimestampDesc", 
                       user.getId(), start, end, conversations.size());
            
            return conversations;
        } catch (Exception e) {
            logger.error("CONVERSATION_RETRIEVE_FAILED: UserID={}, StartDate={}, EndDate={}, Error={}, Method=findByUserAndTimestampBetweenOrderByTimestampDesc", 
                        user.getId(), start, end, e.getMessage());
            throw e;
        }
    }

    public Long getUserConversationCount(User user) {
        logger.info("CONVERSATION_COUNT_START: UserID={}", user.getId());
        
        try {
            Long count = conversationRepository.countByUser(user);
            
            logger.info("CONVERSATION_COUNT_SUCCESS: UserID={}, Count={}", user.getId(), count);
            
            return count;
        } catch (Exception e) {
            logger.error("CONVERSATION_COUNT_FAILED: UserID={}, Error={}", user.getId(), e.getMessage());
            throw e;
        }
    }

    public Conversation rateConversation(Long conversationId, Integer rating) {
        logger.info("CONVERSATION_RATE_START: ConversationID={}, Rating={}", conversationId, rating);
        
        try {
            Conversation conversation = conversationRepository.findById(conversationId)
                    .orElseThrow(() -> new RuntimeException("Conversation not found"));
            
            conversation.setRating(rating);
            Conversation savedConversation = conversationRepository.save(conversation);
            
            logger.info("CONVERSATION_RATE_SUCCESS: ConversationID={}, NewRating={}", 
                       conversationId, savedConversation.getRating());
            
            return savedConversation;
        } catch (Exception e) {
            logger.error("CONVERSATION_RATE_FAILED: ConversationID={}, Rating={}, Error={}", 
                        conversationId, rating, e.getMessage());
            throw e;
        }
    }

    public void deleteConversation(Long conversationId) {
        logger.info("CONVERSATION_DELETE_START: ConversationID={}", conversationId);
        
        try {
            conversationRepository.deleteById(conversationId);
            
            logger.info("CONVERSATION_DELETE_SUCCESS: ConversationID={}", conversationId);
        } catch (Exception e) {
            logger.error("CONVERSATION_DELETE_FAILED: ConversationID={}, Error={}", conversationId, e.getMessage());
            throw e;
        }
    }
}
