package com.example.assistantfinancier.dto;

public class AuthResponse {
    private String token;
    private String email;
    private String nom;
    private String role;
    private String languePreferee;
    
    public AuthResponse() {}
    
    public AuthResponse(String token, String email, String nom, String role, String languePreferee) {
        this.token = token;
        this.email = email;
        this.nom = nom;
        this.role = role;
        this.languePreferee = languePreferee;
    }
    
    public String getToken() {
        return token;
    }
    
    public void setToken(String token) {
        this.token = token;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public String getNom() {
        return nom;
    }
    
    public void setNom(String nom) {
        this.nom = nom;
    }
    
    public String getRole() {
        return role;
    }
    
    public void setRole(String role) {
        this.role = role;
    }
    
    public String getLanguePreferee() {
        return languePreferee;
    }
    
    public void setLanguePreferee(String languePreferee) {
        this.languePreferee = languePreferee;
    }
}
