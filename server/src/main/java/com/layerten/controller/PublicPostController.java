package com.layerten.controller;

import com.layerten.dto.BlogPostDetailDTO;
import com.layerten.dto.BlogPostSummaryDTO;
import com.layerten.entity.PostStatus;
import com.layerten.service.BlogPostService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

/**
 * Public API controller for blog posts.
 * Provides endpoints for browsing and viewing published blog posts.
 */
@RestController
@RequestMapping("/api/posts")
public class PublicPostController {
    
    private final BlogPostService blogPostService;
    
    public PublicPostController(BlogPostService blogPostService) {
        this.blogPostService = blogPostService;
    }
    
    /**
     * Get paginated blog posts with optional search and tag filters.
     * Only returns published posts.
     * 
     * @param search optional search term for title/excerpt
     * @param tag optional tag name to filter by
     * @param pageable pagination parameters
     * @return page of blog post summaries
     */
    @GetMapping
    public Page<BlogPostSummaryDTO> getPosts(
        @RequestParam(required = false) String search,
        @RequestParam(required = false) String tag,
        Pageable pageable
    ) {
        // Public endpoint only returns published posts
        return blogPostService.searchPosts(search, tag, PostStatus.PUBLISHED, pageable);
    }
    
    /**
     * Get a specific blog post by slug.
     * 
     * @param slug the post slug
     * @return the blog post with full content
     */
    @GetMapping("/{slug}")
    public BlogPostDetailDTO getPostBySlug(@PathVariable String slug) {
        return blogPostService.getPostBySlug(slug);
    }
}
