package com.layerten.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Entity representing a ranked list (countdown-style content).
 */
@Entity
@Table(name = "ranked_list")
public class RankedList {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, length = 255)
    private String title;
    
    @Column(length = 255)
    private String subtitle;
    
    @Column(nullable = false, unique = true, length = 255)
    private String slug;
    
    @Column(nullable = false, columnDefinition = "TEXT")
    private String intro;
    
    @Column(columnDefinition = "TEXT")
    private String outro;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cover_image_id")
    private MediaAsset coverImage;
    
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "ranked_list_tags",
        joinColumns = @JoinColumn(name = "ranked_list_id"),
        inverseJoinColumns = @JoinColumn(name = "tag_id")
    )
    private Set<Tag> tags = new HashSet<>();
    
    @OneToMany(mappedBy = "rankedList", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<RankedEntry> entries = new ArrayList<>();
    
    @Column(name = "published_at")
    private LocalDateTime publishedAt;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    // Constructors
    public RankedList() {
    }
    
    public RankedList(String title, String subtitle, String slug, String intro, String outro) {
        this.title = title;
        this.subtitle = subtitle;
        this.slug = slug;
        this.intro = intro;
        this.outro = outro;
    }
    
    // Helper methods for managing bidirectional relationships
    public void addEntry(RankedEntry entry) {
        entries.add(entry);
        entry.setRankedList(this);
    }
    
    public void removeEntry(RankedEntry entry) {
        entries.remove(entry);
        entry.setRankedList(null);
    }
    
    public void addTag(Tag tag) {
        tags.add(tag);
    }
    
    public void removeTag(Tag tag) {
        tags.remove(tag);
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
    
    public String getSubtitle() {
        return subtitle;
    }
    
    public void setSubtitle(String subtitle) {
        this.subtitle = subtitle;
    }
    
    public String getSlug() {
        return slug;
    }
    
    public void setSlug(String slug) {
        this.slug = slug;
    }
    
    public String getIntro() {
        return intro;
    }
    
    public void setIntro(String intro) {
        this.intro = intro;
    }
    
    public String getOutro() {
        return outro;
    }
    
    public void setOutro(String outro) {
        this.outro = outro;
    }
    
    public MediaAsset getCoverImage() {
        return coverImage;
    }
    
    public void setCoverImage(MediaAsset coverImage) {
        this.coverImage = coverImage;
    }
    
    public Set<Tag> getTags() {
        return tags;
    }
    
    public void setTags(Set<Tag> tags) {
        this.tags = tags;
    }
    
    public List<RankedEntry> getEntries() {
        return entries;
    }
    
    public void setEntries(List<RankedEntry> entries) {
        this.entries = entries;
    }
    
    public LocalDateTime getPublishedAt() {
        return publishedAt;
    }
    
    public void setPublishedAt(LocalDateTime publishedAt) {
        this.publishedAt = publishedAt;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
