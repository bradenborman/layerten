package com.layerten.dto;

import com.layerten.entity.SuggestionStatus;
import jakarta.validation.constraints.NotNull;

/**
 * Request DTO for updating a suggestion's status.
 */
public record UpdateSuggestionStatusRequest(
    @NotNull(message = "Status must not be null")
    SuggestionStatus status
) {}
