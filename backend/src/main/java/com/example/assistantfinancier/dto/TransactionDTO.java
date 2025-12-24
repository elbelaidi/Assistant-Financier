package com.example.assistantfinancier.dto;

import java.time.LocalDateTime;

public class TransactionDTO {
    private Long id;
    private String type;
    private Double montant;
    private String description;
    private LocalDateTime date;
    private String category;
    private String status;
    private String paymentMethod;
    private String reference;

    public TransactionDTO() {}

    public TransactionDTO(Long id, String type, Double montant, String description, LocalDateTime date, 
                          String category, String status, String paymentMethod, String reference) {
        this.id = id;
        this.type = type;
        this.montant = montant;
        this.description = description;
        this.date = date;
        this.category = category;
        this.status = status;
        this.paymentMethod = paymentMethod;
        this.reference = reference;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public Double getMontant() { return montant; }
    public void setMontant(Double montant) { this.montant = montant; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public LocalDateTime getDate() { return date; }
    public void setDate(LocalDateTime date) { this.date = date; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }

    public String getReference() { return reference; }
    public void setReference(String reference) { this.reference = reference; }
}
