package com.layerten.dto;

import com.layerten.entity.PostStatus;
import java.util.Set;

/**
 * Request DTO for updating an existing blog post.
 * All fields are optional - only provided fields will be updated.
 */
public record UpdatePostRequest(
    String title,
    String excerpt,
    String body,
    Long coverImageId,
    Set<Long> tagIds,
    PostStatus status
) {}
