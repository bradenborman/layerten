package com.layerten.dto;

import com.layerten.entity.PostStatus;
import java.time.LocalDateTime;
import java.util.Set;

/**
 * DTO for blog post summary information.
 * Used in list views and search results.
 */
public record BlogPostSummaryDTO(
    Long id,
    String title,
    String slug,
    String excerpt,
    MediaAssetDTO coverImage,
    Set<TagDTO> tags,
    PostStatus status,
    LocalDateTime publishedAt
) {}
