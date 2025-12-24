package com.example.assistantfinancier.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "transactions")
public class Transaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String type; // "income" or "expense"
    private Double montant;
    private String description;
    private LocalDateTime date;
    
    @Column(length = 50)
    private String category; // e.g., "salary", "food", "transport", "entertainment"
    
    @Column(length = 20)
    private String status; // "pending", "completed", "cancelled"
    
    @Column(length = 50)
    private String paymentMethod; // "cash", "card", "transfer", "mobile"
    
    @Column
    private String reference; // Transaction reference number

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    // Constructors
    public Transaction() {
        this.date = LocalDateTime.now();
        this.status = "completed";
    }

    public Transaction(String type, Double montant, String description, User user) {
        this.type = type;
        this.montant = montant;
        this.description = description;
        this.user = user;
        this.date = LocalDateTime.now();
        this.status = "completed";
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

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }

    public String getReference() { return reference; }
    public void setReference(String reference) { this.reference = reference; }
}