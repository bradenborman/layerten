package com.layerten.entity;

import jakarta.persistence.*;

/**
 * Entity representing a single entry within a ranked list.
 */
@Entity
@Table(
    name = "ranked_entry",
    uniqueConstraints = @UniqueConstraint(columnNames = {"ranked_list_id", "rank"})
)
public class RankedEntry {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ranked_list_id", nullable = false)
    private RankedList rankedList;
    
    @Column(nullable = false)
    private Integer rank;
    
    @Column(nullable = false, length = 255)
    private String title;
    
    @Column(columnDefinition = "TEXT")
    private String blurb;
    
    @Column(columnDefinition = "TEXT")
    private String commentary;
    
    @Column(name = "fun_fact", columnDefinition = "TEXT")
    private String funFact;
    
    @Column(name = "external_link", length = 500)
    private String externalLink;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hero_image_id")
    private MediaAsset heroImage;
    
    // Constructors
    public RankedEntry() {
    }
    
    public RankedEntry(Integer rank, String title, String blurb, String commentary, String funFact, String externalLink) {
        this.rank = rank;
        this.title = title;
        this.blurb = blurb;
        this.commentary = commentary;
        this.funFact = funFact;
        this.externalLink = externalLink;
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public RankedList getRankedList() {
        return rankedList;
    }
    
    public void setRankedList(RankedList rankedList) {
        this.rankedList = rankedList;
    }
    
    public Integer getRank() {
        return rank;
    }
    
    public void setRank(Integer rank) {
        this.rank = rank;
    }
    
    public String getTitle() {
        return title;
    }
    
    public void setTitle(String title) {
        this.title = title;
    }
    
    public String getBlurb() {
        return blurb;
    }
    
    public void setBlurb(String blurb) {
        this.blurb = blurb;
    }
    
    public String getCommentary() {
        return commentary;
    }
    
    public void setCommentary(String commentary) {
        this.commentary = commentary;
    }
    
    public String getFunFact() {
        return funFact;
    }
    
    public void setFunFact(String funFact) {
        this.funFact = funFact;
    }
    
    public String getExternalLink() {
        return externalLink;
    }
    
    public void setExternalLink(String externalLink) {
        this.externalLink = externalLink;
    }
    
    public MediaAsset getHeroImage() {
        return heroImage;
    }
    
    public void setHeroImage(MediaAsset heroImage) {
        this.heroImage = heroImage;
    }
}
