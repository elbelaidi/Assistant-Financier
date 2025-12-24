package com.example.assistantfinancier.dto;

public class RegisterRequest {
    private String nom;
    private String email;
    private String password;
    private String telephone;
    private String languePreferee;
    
    public RegisterRequest() {}
    
    public RegisterRequest(String nom, String email, String password, String telephone, String languePreferee) {
        this.nom = nom;
        this.email = email;
        this.password = password;
        this.telephone = telephone;
        this.languePreferee = languePreferee;
    }
    
    public String getNom() {
        return nom;
    }
    
    public void setNom(String nom) {
        this.nom = nom;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public String getPassword() {
        return password;
    }
    
    public void setPassword(String password) {
        this.password = password;
    }
    
    public String getTelephone() {
        return telephone;
    }
    
    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }
    
    public String getLanguePreferee() {
        return languePreferee;
    }
    
    public void setLanguePreferee(String languePreferee) {
        this.languePreferee = languePreferee;
    }
}
