package com.example.assistantfinancier.dto;

public class UpdateProfileRequest {
    private String nom;
    private String telephone;
    private String languePreferee;

    public UpdateProfileRequest() {}

    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }

    public String getTelephone() { return telephone; }
    public void setTelephone(String telephone) { this.telephone = telephone; }

    public String getLanguePreferee() { return languePreferee; }
    public void setLanguePreferee(String languePreferee) { this.languePreferee = languePreferee; }
}
