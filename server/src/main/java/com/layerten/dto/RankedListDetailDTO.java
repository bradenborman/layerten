package com.layerten.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

/**
 * DTO for RankedList detail (used in detail views with entries).
 */
public record RankedListDetailDTO(
    Long id,
    String title,
    String subtitle,
    String slug,
    String intro,
    String outro,
    MediaAssetDTO coverImage,
    Set<TagDTO> tags,
    List<RankedEntryDTO> entries,
    LocalDateTime publishedAt
) {}
