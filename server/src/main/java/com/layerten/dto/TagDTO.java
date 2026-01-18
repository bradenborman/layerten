package com.layerten.dto;

/**
 * DTO for Tag entity.
 */
public record TagDTO(
    Long id,
    String name,
    String slug
) {}
