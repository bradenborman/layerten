package com.layerten.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

/**
 * Request DTO for creating a new ranked entry.
 */
public record CreateEntryRequest(
    @NotNull(message = "Rank must not be null")
    @Positive(message = "Rank must be a positive integer")
    Integer rank,
    
    @NotBlank(message = "Title must not be blank")
    String title,
    
    String blurb,
    
    String commentary,
    
    String funFact,
    
    String externalLink,
    
    Long heroImageId
) {}
