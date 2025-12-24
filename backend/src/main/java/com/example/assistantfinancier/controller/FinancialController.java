
package com.example.assistantfinancier.controller;

import com.example.assistantfinancier.model.User;
import com.example.assistantfinancier.security.JwtUtil;
import com.example.assistantfinancier.service.AIModelService;
import com.example.assistantfinancier.service.ConversationService;
import com.example.assistantfinancier.service.FinancialAdvisorService;
import com.example.assistantfinancier.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api")
public class FinancialController {
    
    private static final Logger logger = LoggerFactory.getLogger(FinancialController.class);

    @Autowired
    private AIModelService aiModelService;

    @Autowired
    private FinancialAdvisorService financialAdvisorService;

    @Autowired
    private ConversationService conversationService;

    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtil jwtUtil;


    @PostMapping("/conseil")
    public ResponseEntity<String> handleRequeteFinanciere(
            @RequestBody AdviceRequest request,
            @RequestHeader(value = "Authorization", required = false) String token) {
        
        logger.info("CONSEIL_REQUEST_START: QueryLength={}, Language={}, HasToken={}", 
                   request.getQuery().length(), 
                   request.getLanguage() != null ? request.getLanguage() : "fr",
                   token != null && token.startsWith("Bearer "));
        
        String language = request.getLanguage() != null ? request.getLanguage() : "fr";
        
        User user = null;
        boolean isAuthenticated = false;
        
        // Try to get authenticated user
        if (token != null && token.startsWith("Bearer ")) {
            try {
                String jwt = token.substring(7);
                String email = jwtUtil.getEmailFromToken(jwt);
                user = userService.findByEmail(email).orElse(null);
                isAuthenticated = (user != null);
                
                if (user != null) {
                    logger.info("AUTH_SUCCESS: UserID={}, Email={}", user.getId(), email);
                } else {
                    logger.warn("AUTH_FAILED: Email={} not found in database", email);
                }
            } catch (Exception e) {
                logger.warn("AUTH_TOKEN_INVALID: Error={}", e.getMessage());
            }
        }
        
        // Create temporary user for non-authenticated requests (for backward compatibility)
        if (user == null) {
            user = new User();
            user.setNom("Guest");
            user.setLanguePreferee(language);
            logger.info("GUEST_MODE: Using temporary user for query processing");
        }
        
        logger.info("AI_PROCESSING_START: UserID={}, UserType={}, QueryPreview={}", 
                   user.getId(), isAuthenticated ? "AUTHENTICATED" : "GUEST",
                   request.getQuery().substring(0, Math.min(request.getQuery().length(), 50)));
        

        String conseil = financialAdvisorService.genererConseil(user, request.getQuery(), language);
        
        logger.info("AI_PROCESSING_SUCCESS: ResponseLength={}", conseil.length());
        
        // Save conversation if user is authenticated
        if (user.getId() != null && isAuthenticated) {
            try {
                String category = categorizeQuery(request.getQuery());
                
                logger.info("CONVERSATION_SAVE_ATTEMPT: UserID={}, Category={}, QueryLength={}", 
                           user.getId(), category, request.getQuery().length());
                
                conversationService.saveConversation(user, request.getQuery(), conseil, language, category);
                
                logger.info("CONVERSATION_SAVE_SUCCESS: UserID={}, Conversation saved successfully", user.getId());
                
            } catch (Exception e) {
                logger.error("CONVERSATION_SAVE_ERROR: UserID={}, Error={}, StackTrace={}", 
                            user.getId(), e.getMessage(), e.getStackTrace());
            }
        } else {
            logger.info("CONVERSATION_SAVE_SKIPPED: UserID={}, Authenticated={}", 
                       user.getId(), isAuthenticated);
        }
        
        logger.info("CONSEIL_REQUEST_COMPLETE: Success=true, ResponseLength={}", conseil.length());
        
        return ResponseEntity.ok(conseil);
    }
    
    private String categorizeQuery(String query) {
        String lowerQuery = query.toLowerCase();
        if (lowerQuery.contains("épargn") || lowerQuery.contains("epargn") || lowerQuery.contains("saving")) {
            return "savings";
        } else if (lowerQuery.contains("prêt") || lowerQuery.contains("pret") || lowerQuery.contains("loan") || lowerQuery.contains("crédit") || lowerQuery.contains("credit")) {
            return "loan";
        } else if (lowerQuery.contains("budget")) {
            return "budget";
        } else if (lowerQuery.contains("invest")) {
            return "investment";
        } else if (lowerQuery.contains("compte") || lowerQuery.contains("account")) {
            return "account";
        } else {
            return "general";
        }
    }

    @PostMapping("/voix")
    public ResponseEntity<String> handleRequeteVocale(@RequestBody VoiceRequest request) {
        // Process voice, for now just return text
        return ResponseEntity.ok("Réponse vocale : " + request.getAudioText());
    }

    @PostMapping("/tts")
    public ResponseEntity<String> generateTTS(@RequestBody TTSRequest request) {
        System.out.println("=== DEBUG TTS: Received TTS request for text: " + request.getText() + ", language: " + request.getLanguage());

        String base64Audio = aiModelService.generateSpeech(request.getText(), request.getLanguage());

        if (base64Audio != null) {
            return ResponseEntity.ok(base64Audio);
        } else {
            return ResponseEntity.status(500).body("Failed to generate speech");
        }
    }

    // Inner classes for requests
    public static class AdviceRequest {
        private String query;
        private String language;

        public String getQuery() { return query; }
        public void setQuery(String query) { this.query = query; }
        public String getLanguage() { return language; }
        public void setLanguage(String language) { this.language = language; }
    }

    public static class VoiceRequest {
        private String audioText;

        public String getAudioText() { return audioText; }
        public void setAudioText(String audioText) { this.audioText = audioText; }
    }

    public static class TTSRequest {
        private String text;
        private String language;

        public String getText() { return text; }
        public void setText(String text) { this.text = text; }
        public String getLanguage() { return language; }
        public void setLanguage(String language) { this.language = language; }
    }
}