package com.layerten.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * Request DTO for creating a new suggestion.
 */
public record CreateSuggestionRequest(
    @NotBlank(message = "Title must not be blank")
    String title,
    
    @NotBlank(message = "Description must not be blank")
    String description,
    
    String category,
    
    String exampleEntries,
    
    String submitterName,
    
    @Email(message = "Email must be valid")
    String submitterEmail
) {}
