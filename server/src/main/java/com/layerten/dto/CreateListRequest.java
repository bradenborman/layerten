package com.layerten.dto;

import jakarta.validation.constraints.NotBlank;
import java.util.Set;

/**
 * Request DTO for creating a new ranked list.
 */
public record CreateListRequest(
    @NotBlank(message = "Title must not be blank")
    String title,
    
    String subtitle,
    
    @NotBlank(message = "Intro must not be blank")
    String intro,
    
    String outro,
    
    Long coverImageId,
    
    Set<Long> tagIds
) {}
