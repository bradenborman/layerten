package com.layerten.dto;

import com.layerten.entity.PostStatus;
import java.time.LocalDateTime;
import java.util.Set;

/**
 * DTO for blog post detail information.
 * Used in detail views with full content.
 */
public record BlogPostDetailDTO(
    Long id,
    String title,
    String slug,
    String excerpt,
    String body,
    MediaAssetDTO coverImage,
    Set<TagDTO> tags,
    PostStatus status,
    LocalDateTime publishedAt
) {}
