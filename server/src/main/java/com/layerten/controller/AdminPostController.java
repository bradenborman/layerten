package com.layerten.controller;

import com.layerten.dto.BlogPostDetailDTO;
import com.layerten.dto.CreatePostRequest;
import com.layerten.dto.UpdatePostRequest;
import com.layerten.service.BlogPostService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

/**
 * Admin API controller for managing blog posts.
 * Requires authentication.
 */
@RestController
@RequestMapping("/api/admin/posts")
public class AdminPostController {
    
    private final BlogPostService blogPostService;
    
    public AdminPostController(BlogPostService blogPostService) {
        this.blogPostService = blogPostService;
    }
    
    /**
     * Create a new blog post.
     * 
     * @param request the create post request
     * @return the created post
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public BlogPostDetailDTO createPost(@Valid @RequestBody CreatePostRequest request) {
        return blogPostService.createPost(request);
    }
    
    /**
     * Update an existing blog post.
     * 
     * @param id the post ID
     * @param request the update post request
     * @return the updated post
     */
    @PutMapping("/{id}")
    public BlogPostDetailDTO updatePost(
        @PathVariable Long id,
        @Valid @RequestBody UpdatePostRequest request
    ) {
        return blogPostService.updatePost(id, request);
    }
    
    /**
     * Delete a blog post.
     * 
     * @param id the post ID
     */
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deletePost(@PathVariable Long id) {
        blogPostService.deletePost(id);
    }
}
