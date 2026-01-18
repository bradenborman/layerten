package com.layerten.dto;

import com.layerten.entity.PostStatus;
import jakarta.validation.constraints.NotBlank;
import java.util.Set;

/**
 * Request DTO for creating a new blog post.
 */
public record CreatePostRequest(
    @NotBlank(message = "Title must not be blank")
    String title,
    
    @NotBlank(message = "Excerpt must not be blank")
    String excerpt,
    
    @NotBlank(message = "Body must not be blank")
    String body,
    
    Long coverImageId,
    
    Set<Long> tagIds,
    
    PostStatus status
) {}
