package com.layerten.dto;

import com.layerten.entity.SuggestionStatus;
import java.time.LocalDateTime;

/**
 * DTO for suggestion information.
 * Used for displaying suggestions to admin.
 */
public record SuggestionDTO(
    Long id,
    String title,
    String description,
    String category,
    String exampleEntries,
    String submitterName,
    String submitterEmail,
    SuggestionStatus status,
    LocalDateTime createdAt
) {}
