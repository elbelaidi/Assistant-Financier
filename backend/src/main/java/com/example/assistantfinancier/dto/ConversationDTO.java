package com.example.assistantfinancier.dto;

import java.time.LocalDateTime;

public class ConversationDTO {
    private Long id;
    private String query;
    private String response;
    private String language;
    private String category;
    private LocalDateTime timestamp;
    private Integer rating;

    public ConversationDTO() {}

    public ConversationDTO(Long id, String query, String response, String language, String category, LocalDateTime timestamp, Integer rating) {
        this.id = id;
        this.query = query;
        this.response = response;
        this.language = language;
        this.category = category;
        this.timestamp = timestamp;
        this.rating = rating;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public String getResponse() {
        return response;
    }

    public void setResponse(String response) {
        this.response = response;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public Integer getRating() {
        return rating;
    }

    public void setRating(Integer rating) {
        this.rating = rating;
    }
}
