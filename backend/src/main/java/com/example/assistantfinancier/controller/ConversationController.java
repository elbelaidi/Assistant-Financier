
package com.example.assistantfinancier.controller;

import com.example.assistantfinancier.dto.ConversationDTO;
import com.example.assistantfinancier.model.Conversation;
import com.example.assistantfinancier.model.User;
import com.example.assistantfinancier.security.JwtUtil;
import com.example.assistantfinancier.service.ConversationService;
import com.example.assistantfinancier.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@RestController
@RequestMapping("/api/conversations")
@CrossOrigin(origins = "*")
public class ConversationController {
    
    private static final Logger logger = LoggerFactory.getLogger(ConversationController.class);

    @Autowired
    private ConversationService conversationService;

    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtil jwtUtil;


    @GetMapping
    public ResponseEntity<?> getUserConversations(@RequestHeader("Authorization") String token) {
        logger.info("HISTORY_REQUEST_START: Endpoint=/api/conversations");
        
        try {
            String jwt = token.substring(7);
            String email = jwtUtil.getEmailFromToken(jwt);
            
            logger.info("AUTH_PROCESSING: Extracted email={} from token", email);
            
            User user = userService.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            
            logger.info("USER_FOUND: UserID={}, Email={}", user.getId(), email);

            logger.info("HISTORY_RETRIEVE_START: UserID={}, Method=getUserConversations", user.getId());
            List<Conversation> conversations = conversationService.getUserConversations(user);
            
            logger.info("HISTORY_RETRIEVE_SUCCESS: UserID={}, RetrievedCount={}", user.getId(), conversations.size());
            
            List<ConversationDTO> dtos = conversations.stream()
                    .map(c -> new ConversationDTO(c.getId(), c.getQuery(), c.getResponse(), 
                            c.getLanguage(), c.getCategory(), c.getTimestamp(), c.getRating()))
                    .collect(Collectors.toList());
            
            logger.info("HISTORY_DTO_CONVERSION_SUCCESS: UserID={}, DTOsCreated={}", user.getId(), dtos.size());

            return ResponseEntity.ok(dtos);
        } catch (Exception e) {
            logger.error("HISTORY_REQUEST_ERROR: Endpoint=/api/conversations, Error={}, StackTrace={}", 
                        e.getMessage(), e.getStackTrace());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }


    @GetMapping("/filter")
    public ResponseEntity<?> filterConversations(
            @RequestHeader("Authorization") String token,
            @RequestParam(required = false) String language,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        
        logger.info("HISTORY_FILTER_START: Language={}, Category={}, StartDate={}, EndDate={}", 
                   language, category, startDate, endDate);
        
        try {
            String jwt = token.substring(7);
            String email = jwtUtil.getEmailFromToken(jwt);
            
            logger.info("AUTH_PROCESSING_FILTER: Extracted email={} from token", email);
            
            User user = userService.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            
            logger.info("USER_FOUND_FILTER: UserID={}, Email={}", user.getId(), email);

            List<Conversation> conversations;
            String filterMethod = "getUserConversations";
            
            if (language != null && !language.isEmpty()) {
                logger.info("HISTORY_FILTER_PROCESS: UserID={}, FilterByLanguage={}", user.getId(), language);
                conversations = conversationService.getUserConversationsByLanguage(user, language);
                filterMethod = "getUserConversationsByLanguage";
            } else if (category != null && !category.isEmpty()) {
                logger.info("HISTORY_FILTER_PROCESS: UserID={}, FilterByCategory={}", user.getId(), category);
                conversations = conversationService.getUserConversationsByCategory(user, category);
                filterMethod = "getUserConversationsByCategory";
            } else if (startDate != null && endDate != null) {
                logger.info("HISTORY_FILTER_PROCESS: UserID={}, FilterByDateRange: {} to {}", user.getId(), startDate, endDate);
                conversations = conversationService.getUserConversationsByDateRange(user, startDate, endDate);
                filterMethod = "getUserConversationsByDateRange";
            } else {
                logger.info("HISTORY_FILTER_PROCESS: UserID={}, No filters applied, using default", user.getId());
                conversations = conversationService.getUserConversations(user);
            }

            logger.info("HISTORY_FILTER_SUCCESS: UserID={}, Method={}, RetrievedCount={}", 
                       user.getId(), filterMethod, conversations.size());
            
            List<ConversationDTO> dtos = conversations.stream()
                    .map(c -> new ConversationDTO(c.getId(), c.getQuery(), c.getResponse(), 
                            c.getLanguage(), c.getCategory(), c.getTimestamp(), c.getRating()))
                    .collect(Collectors.toList());

            logger.info("HISTORY_FILTER_DTO_SUCCESS: UserID={}, DTOsCreated={}", user.getId(), dtos.size());

            return ResponseEntity.ok(dtos);
        } catch (Exception e) {
            logger.error("HISTORY_FILTER_ERROR: Error={}, StackTrace={}", 
                        e.getMessage(), e.getStackTrace());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }


    @PostMapping("/{id}/rate")
    public ResponseEntity<?> rateConversation(
            @RequestHeader("Authorization") String token,
            @PathVariable Long id,
            @RequestBody Map<String, Integer> request) {
        
        logger.info("HISTORY_RATE_START: ConversationID={}, RequestBody={}", id, request);
        
        try {
            Integer rating = request.get("rating");
            
            logger.info("HISTORY_RATE_VALIDATION: ConversationID={}, Rating={}", id, rating);
            
            if (rating == null || rating < 1 || rating > 5) {
                logger.warn("HISTORY_RATE_INVALID: ConversationID={}, InvalidRating={}", id, rating);
                return ResponseEntity.badRequest().body(Map.of("error", "Rating must be between 1 and 5"));
            }

            logger.info("HISTORY_RATE_PROCESS: ConversationID={}, Calling conversationService.rateConversation", id);
            Conversation conversation = conversationService.rateConversation(id, rating);
            
            logger.info("HISTORY_RATE_SUCCESS: ConversationID={}, NewRating={}", id, conversation.getRating());
            
            return ResponseEntity.ok(Map.of("message", "Rating saved", "rating", conversation.getRating()));
        } catch (Exception e) {
            logger.error("HISTORY_RATE_ERROR: ConversationID={}, Error={}, StackTrace={}", 
                        id, e.getMessage(), e.getStackTrace());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteConversation(
            @RequestHeader("Authorization") String token,
            @PathVariable Long id) {
        
        logger.info("HISTORY_DELETE_START: ConversationID={}", id);
        
        try {
            logger.info("HISTORY_DELETE_PROCESS: ConversationID={}, Calling conversationService.deleteConversation", id);
            conversationService.deleteConversation(id);
            
            logger.info("HISTORY_DELETE_SUCCESS: ConversationID={}", id);
            
            return ResponseEntity.ok(Map.of("message", "Conversation deleted"));
        } catch (Exception e) {
            logger.error("HISTORY_DELETE_ERROR: ConversationID={}, Error={}, StackTrace={}", 
                        id, e.getMessage(), e.getStackTrace());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/create")
    public ResponseEntity<?> createConversation(
            @RequestHeader("Authorization") String token,
            @RequestBody CreateConversationRequest request) {

        logger.info("HISTORY_CREATE_START: QueryLength={}, Language={}, Category={}",
                   request.getQuery().length(), request.getLanguage(), request.getCategory());

        try {
            String jwt = token.substring(7);
            String email = jwtUtil.getEmailFromToken(jwt);

            logger.info("AUTH_PROCESSING_CREATE: Extracted email={} from token", email);

            User user = userService.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            logger.info("USER_FOUND_CREATE: UserID={}, Email={}", user.getId(), email);

            logger.info("HISTORY_CREATE_PROCESS: UserID={}, Calling conversationService.saveConversation", user.getId());

            Conversation conversation = conversationService.saveConversation(
                user,
                request.getQuery(),
                request.getResponse(),
                request.getLanguage(),
                request.getCategory()
            );

            logger.info("HISTORY_CREATE_SUCCESS: ConversationID={}, UserID={}", conversation.getId(), user.getId());

            return ResponseEntity.ok(Map.of("message", "Conversation created", "id", conversation.getId()));
        } catch (Exception e) {
            logger.error("HISTORY_CREATE_ERROR: Error={}, StackTrace={}",
                        e.getMessage(), e.getStackTrace());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/stats")
    public ResponseEntity<?> getConversationStats(@RequestHeader("Authorization") String token) {
        logger.info("HISTORY_STATS_START: Endpoint=/api/conversations/stats");

        try {
            String jwt = token.substring(7);
            String email = jwtUtil.getEmailFromToken(jwt);

            logger.info("AUTH_PROCESSING_STATS: Extracted email={} from token", email);

            User user = userService.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            logger.info("USER_FOUND_STATS: UserID={}, Email={}", user.getId(), email);

            logger.info("HISTORY_STATS_PROCESS: UserID={}, Calling conversationService.getUserConversationCount", user.getId());
            Long totalCount = conversationService.getUserConversationCount(user);

            logger.info("HISTORY_STATS_SUCCESS: UserID={}, TotalCount={}", user.getId(), totalCount);

            Map<String, Object> stats = new HashMap<>();
            stats.put("totalConversations", totalCount);

            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            logger.error("HISTORY_STATS_ERROR: Endpoint=/api/conversations/stats, Error={}, StackTrace={}",
                        e.getMessage(), e.getStackTrace());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    public static class CreateConversationRequest {
        private String query;
        private String response;
        private String language;
        private String category;

        public String getQuery() { return query; }
        public void setQuery(String query) { this.query = query; }
        public String getResponse() { return response; }
        public void setResponse(String response) { this.response = response; }
        public String getLanguage() { return language; }
        public void setLanguage(String language) { this.language = language; }
        public String getCategory() { return category; }
        public void setCategory(String category) { this.category = category; }
    }
}
