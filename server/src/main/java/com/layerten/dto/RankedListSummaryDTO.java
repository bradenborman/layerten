package com.layerten.dto;

import java.time.LocalDateTime;
import java.util.Set;

/**
 * DTO for RankedList summary (used in list views).
 */
public record RankedListSummaryDTO(
    Long id,
    String title,
    String subtitle,
    String slug,
    MediaAssetDTO coverImage,
    Set<TagDTO> tags,
    Integer entryCount,
    LocalDateTime publishedAt
) {}
