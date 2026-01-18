package com.layerten.service;

import com.layerten.dto.*;
import com.layerten.entity.BlogPost;
import com.layerten.entity.MediaAsset;
import com.layerten.entity.PostStatus;
import com.layerten.entity.Tag;
import com.layerten.repository.BlogPostRepository;
import com.layerten.repository.MediaAssetRepository;
import com.layerten.repository.TagRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Service for managing blog posts.
 * Provides methods for creating, updating, deleting, and querying blog posts.
 */
@Service
@Transactional
public class BlogPostService {
    
    private final BlogPostRepository blogPostRepository;
    private final TagRepository tagRepository;
    private final MediaAssetRepository mediaAssetRepository;
    private final SlugService slugService;
    
    public BlogPostService(
            BlogPostRepository blogPostRepository,
            TagRepository tagRepository,
            MediaAssetRepository mediaAssetRepository,
            SlugService slugService) {
        this.blogPostRepository = blogPostRepository;
        this.tagRepository = tagRepository;
        this.mediaAssetRepository = mediaAssetRepository;
        this.slugService = slugService;
    }
    
    /**
     * Create a new blog post.
     * 
     * @param request the create post request
     * @return the created post as a DTO
     */
    public BlogPostDetailDTO createPost(CreatePostRequest request) {
        // Generate unique slug from title
        String baseSlug = slugService.generateSlug(request.title());
        String uniqueSlug = slugService.ensureUniqueSlug(baseSlug, BlogPost.class);
        
        // Determine status (default to DRAFT if not provided)
        PostStatus status = request.status() != null ? request.status() : PostStatus.DRAFT;
        
        // Create the blog post entity
        BlogPost blogPost = new BlogPost(
            request.title(),
            uniqueSlug,
            request.excerpt(),
            request.body(),
            status
        );
        
        // Set cover image if provided
        if (request.coverImageId() != null) {
            MediaAsset coverImage = mediaAssetRepository.findById(request.coverImageId())
                .orElseThrow(() -> new EntityNotFoundException(
                    "Media asset with ID " + request.coverImageId() + " not found"));
            blogPost.setCoverImage(coverImage);
        }
        
        // Set tags if provided
        if (request.tagIds() != null && !request.tagIds().isEmpty()) {
            Set<Tag> tags = new HashSet<>();
            for (Long tagId : request.tagIds()) {
                Tag tag = tagRepository.findById(tagId)
                    .orElseThrow(() -> new EntityNotFoundException(
                        "Tag with ID " + tagId + " not found"));
                tags.add(tag);
            }
            blogPost.setTags(tags);
        }
        
        // Set published timestamp if status is PUBLISHED
        if (status == PostStatus.PUBLISHED) {
            blogPost.setPublishedAt(LocalDateTime.now());
        }
        
        // Save and return
        BlogPost saved = blogPostRepository.save(blogPost);
        return toDetailDTO(saved);
    }
    
    /**
     * Update an existing blog post.
     * 
     * @param id the ID of the post to update
     * @param request the update post request
     * @return the updated post as a DTO
     */
    public BlogPostDetailDTO updatePost(Long id, UpdatePostRequest request) {
        BlogPost blogPost = blogPostRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException(
                "Blog post with ID " + id + " not found"));
        
        // Update fields if provided
        if (request.title() != null && !request.title().isBlank()) {
            blogPost.setTitle(request.title());
            // Regenerate slug if title changed
            String baseSlug = slugService.generateSlug(request.title());
            String uniqueSlug = slugService.ensureUniqueSlugExcluding(baseSlug, BlogPost.class, id);
            blogPost.setSlug(uniqueSlug);
        }
        
        if (request.excerpt() != null && !request.excerpt().isBlank()) {
            blogPost.setExcerpt(request.excerpt());
        }
        
        if (request.body() != null && !request.body().isBlank()) {
            blogPost.setBody(request.body());
        }
        
        // Update cover image
        if (request.coverImageId() != null) {
            MediaAsset coverImage = mediaAssetRepository.findById(request.coverImageId())
                .orElseThrow(() -> new EntityNotFoundException(
                    "Media asset with ID " + request.coverImageId() + " not found"));
            blogPost.setCoverImage(coverImage);
        }
        
        // Update tags
        if (request.tagIds() != null) {
            Set<Tag> tags = new HashSet<>();
            for (Long tagId : request.tagIds()) {
                Tag tag = tagRepository.findById(tagId)
                    .orElseThrow(() -> new EntityNotFoundException(
                        "Tag with ID " + tagId + " not found"));
                tags.add(tag);
            }
            blogPost.setTags(tags);
        }
        
        // Update status and set published timestamp if transitioning to PUBLISHED
        if (request.status() != null) {
            PostStatus oldStatus = blogPost.getStatus();
            blogPost.setStatus(request.status());
            
            // Set published timestamp if transitioning from DRAFT to PUBLISHED
            if (oldStatus != PostStatus.PUBLISHED && request.status() == PostStatus.PUBLISHED) {
                blogPost.setPublishedAt(LocalDateTime.now());
            }
        }
        
        // Save and return
        BlogPost updated = blogPostRepository.save(blogPost);
        return toDetailDTO(updated);
    }
    
    /**
     * Delete a blog post.
     * 
     * @param id the ID of the post to delete
     */
    public void deletePost(Long id) {
        if (!blogPostRepository.existsById(id)) {
            throw new EntityNotFoundException("Blog post with ID " + id + " not found");
        }
        blogPostRepository.deleteById(id);
    }
    
    /**
     * Get a blog post by slug.
     * 
     * @param slug the slug of the post
     * @return the post as a DTO
     */
    @Transactional(readOnly = true)
    public BlogPostDetailDTO getPostBySlug(String slug) {
        BlogPost blogPost = blogPostRepository.findBySlug(slug)
            .orElseThrow(() -> new EntityNotFoundException(
                "Blog post with slug '" + slug + "' not found"));
        
        return toDetailDTO(blogPost);
    }
    
    /**
     * Search blog posts with pagination, search, tag filters, and status filtering.
     * If status is null, returns all posts (for admin use).
     * If status is PUBLISHED, returns only published posts (for public use).
     * 
     * @param search optional search term for title/excerpt
     * @param tag optional tag name to filter by
     * @param status optional status to filter by (null for all posts)
     * @param pageable pagination information
     * @return a page of post summaries
     */
    @Transactional(readOnly = true)
    public Page<BlogPostSummaryDTO> searchPosts(String search, String tag, PostStatus status, Pageable pageable) {
        Page<BlogPost> posts;
        
        if (status != null) {
            // Filter by status (for public queries)
            if (tag != null && !tag.isBlank() && search != null && !search.isBlank()) {
                // Both tag and search filters with status
                posts = blogPostRepository.findByStatusAndTagNameAndTitleOrExcerptContaining(tag, search, status, pageable);
            } else if (tag != null && !tag.isBlank()) {
                // Tag filter only with status
                posts = blogPostRepository.findByStatusAndTagName(tag, status, pageable);
            } else if (search != null && !search.isBlank()) {
                // Search filter only with status
                posts = blogPostRepository.findByStatusAndTitleOrExcerptContaining(search, status, pageable);
            } else {
                // No filters, return all posts with status
                posts = blogPostRepository.findByStatus(status, pageable);
            }
        } else {
            // No status filter (for admin queries)
            if (tag != null && !tag.isBlank() && search != null && !search.isBlank()) {
                // Both tag and search filters without status
                posts = blogPostRepository.findByTagNameAndTitleOrExcerptContaining(tag, search, pageable);
            } else if (tag != null && !tag.isBlank()) {
                // Tag filter only without status
                posts = blogPostRepository.findByTagName(tag, pageable);
            } else if (search != null && !search.isBlank()) {
                // Search filter only without status
                posts = blogPostRepository.findByTitleOrExcerptContaining(search, pageable);
            } else {
                // No filters, return all posts
                posts = blogPostRepository.findAll(pageable);
            }
        }
        
        return posts.map(this::toSummaryDTO);
    }
    
    /**
     * Publish a blog post by setting its status to PUBLISHED and recording the timestamp.
     * 
     * @param id the ID of the post to publish
     * @return the published post as a DTO
     */
    public BlogPostDetailDTO publishPost(Long id) {
        BlogPost blogPost = blogPostRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException(
                "Blog post with ID " + id + " not found"));
        
        // Set status to PUBLISHED and record timestamp
        blogPost.setStatus(PostStatus.PUBLISHED);
        if (blogPost.getPublishedAt() == null) {
            blogPost.setPublishedAt(LocalDateTime.now());
        }
        
        // Save and return
        BlogPost published = blogPostRepository.save(blogPost);
        return toDetailDTO(published);
    }
    
    // Helper methods for DTO conversion
    
    private BlogPostSummaryDTO toSummaryDTO(BlogPost blogPost) {
        return new BlogPostSummaryDTO(
            blogPost.getId(),
            blogPost.getTitle(),
            blogPost.getSlug(),
            blogPost.getExcerpt(),
            toMediaAssetDTO(blogPost.getCoverImage()),
            blogPost.getTags().stream()
                .map(this::toTagDTO)
                .collect(Collectors.toSet()),
            blogPost.getStatus(),
            blogPost.getPublishedAt()
        );
    }
    
    private BlogPostDetailDTO toDetailDTO(BlogPost blogPost) {
        return new BlogPostDetailDTO(
            blogPost.getId(),
            blogPost.getTitle(),
            blogPost.getSlug(),
            blogPost.getExcerpt(),
            blogPost.getBody(),
            toMediaAssetDTO(blogPost.getCoverImage()),
            blogPost.getTags().stream()
                .map(this::toTagDTO)
                .collect(Collectors.toSet()),
            blogPost.getStatus(),
            blogPost.getPublishedAt()
        );
    }
    
    private TagDTO toTagDTO(Tag tag) {
        return new TagDTO(
            tag.getId(),
            tag.getName(),
            tag.getSlug()
        );
    }
    
    private MediaAssetDTO toMediaAssetDTO(MediaAsset mediaAsset) {
        if (mediaAsset == null) {
            return null;
        }
        
        return new MediaAssetDTO(
            mediaAsset.getId(),
            mediaAsset.getFilename(),
            mediaAsset.getContentType(),
            mediaAsset.getFileSize(),
            mediaAsset.getAltText(),
            "/api/media/" + mediaAsset.getId()
        );
    }
}
