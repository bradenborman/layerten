package com.layerten.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

/**
 * DTO for updating entry ranks during reordering.
 */
public record EntryRankUpdate(
    @NotNull(message = "Entry ID must not be null")
    Long entryId,
    
    @NotNull(message = "New rank must not be null")
    @Positive(message = "New rank must be a positive integer")
    Integer newRank
) {}
