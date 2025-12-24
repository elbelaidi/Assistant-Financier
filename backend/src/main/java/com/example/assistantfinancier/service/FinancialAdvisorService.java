package com.example.assistantfinancier.service;

import com.example.assistantfinancier.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class FinancialAdvisorService {

    @Autowired
    private AIModelService aiModelService;


    public String genererConseil(User user, String requete, String language) {
        // Use the language parameter from the request (not user's preferred language)
        String effectiveLanguage = language != null ? language : "fr";
        String aiResponse = aiModelService.processNLP(requete, effectiveLanguage);
        return aiResponse;
    }

    public String analyserRisques(User user) {
        // Risk analysis logic
        return "Risque faible basé sur le profil.";
    }
}