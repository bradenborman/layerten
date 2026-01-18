package com.layerten.dto;

/**
 * DTO for RankedEntry entity.
 */
public record RankedEntryDTO(
    Long id,
    Integer rank,
    String title,
    String blurb,
    String commentary,
    String funFact,
    String externalLink,
    MediaAssetDTO heroImage
) {}
