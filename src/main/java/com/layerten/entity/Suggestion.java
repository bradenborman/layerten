package com.layerten.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Entity representing a visitor-submitted suggestion for a future ranked list.
 */
@Entity
@Table(name = "suggestion")
public class Suggestion {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, length = 255)
    private String title;
    
    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;
    
    @Column(length = 100)
    private String category;
    
    @Column(name = "example_entries", columnDefinition = "TEXT")
    private String exampleEntries;
    
    @Column(name = "submitter_name", length = 255)
    private String submitterName;
    
    @Column(name = "submitter_email", length = 255)
    private String submitterEmail;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private SuggestionStatus status = SuggestionStatus.NEW;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
    
    // Constructors
    public Suggestion() {
    }
    
    public Suggestion(String title, String description, String category, String exampleEntries, 
                     String submitterName, String submitterEmail) {
        this.title = title;
        this.description = description;
        this.category = category;
        this.exampleEntries = exampleEntries;
        this.submitterName = submitterName;
        this.submitterEmail = submitterEmail;
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getTitle() {
        return title;
    }
    
    public void setTitle(String title) {
        this.title = title;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public String getCategory() {
        return category;
    }
    
    public void setCategory(String category) {
        this.category = category;
    }
    
    public String getExampleEntries() {
        return exampleEntries;
    }
    
    public void setExampleEntries(String exampleEntries) {
        this.exampleEntries = exampleEntries;
    }
    
    public String getSubmitterName() {
        return submitterName;
    }
    
    public void setSubmitterName(String submitterName) {
        this.submitterName = submitterName;
    }
    
    public String getSubmitterEmail() {
        return submitterEmail;
    }
    
    public void setSubmitterEmail(String submitterEmail) {
        this.submitterEmail = submitterEmail;
    }
    
    public SuggestionStatus getStatus() {
        return status;
    }
    
    public void setStatus(SuggestionStatus status) {
        this.status = status;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
