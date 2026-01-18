package com.layerten.dto;

/**
 * DTO for MediaAsset entity.
 */
public record MediaAssetDTO(
    Long id,
    String filename,
    String contentType,
    Long fileSize,
    String altText,
    String url // Computed as /api/media/{id}
) {}
