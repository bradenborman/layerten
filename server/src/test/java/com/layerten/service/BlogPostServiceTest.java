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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for BlogPostService.
 */
@ExtendWith(MockitoExtension.class)
class BlogPostServiceTest {
    
    @Mock
    private BlogPostRepository blogPostRepository;
    
    @Mock
    private TagRepository tagRepository;
    
    @Mock
    private MediaAssetRepository mediaAssetRepository;
    
    @Mock
    private SlugService slugService;
    
    @InjectMocks
    private BlogPostService blogPostService;
    
    private BlogPost testPost;
    private Tag testTag;
    private MediaAsset testMedia;
    
    @BeforeEach
    void setUp() {
        testTag = new Tag("Technology", "technology");
        testTag.setId(1L);
        
        testMedia = new MediaAsset("test.jpg", "image/jpeg", 1024L, "Test image", "/media/test.jpg");
        testMedia.setId(1L);
        
        testPost = new BlogPost("Test Post", "test-post", "Test excerpt", "Test body", PostStatus.PUBLISHED);
        testPost.setId(1L);
        testPost.setPublishedAt(LocalDateTime.now());
        testPost.setCoverImage(testMedia);
        testPost.addTag(testTag);
    }
    
    @Test
    void createPost_shouldCreatePostWithGeneratedSlug() {
        // Arrange
        CreatePostRequest request = new CreatePostRequest(
            "Test Post",
            "Test excerpt",
            "Test body",
            1L,
            Set.of(1L),
            PostStatus.DRAFT
        );
        
        when(slugService.generateSlug("Test Post")).thenReturn("test-post");
        when(slugService.ensureUniqueSlug("test-post", BlogPost.class)).thenReturn("test-post");
        when(mediaAssetRepository.findById(1L)).thenReturn(Optional.of(testMedia));
        when(tagRepository.findById(1L)).thenReturn(Optional.of(testTag));
        when(blogPostRepository.save(any(BlogPost.class))).thenReturn(testPost);
        
        // Act
        BlogPostDetailDTO result = blogPostService.createPost(request);
        
        // Assert
        assertNotNull(result);
        assertEquals("Test Post", result.title());
        assertEquals("test-post", result.slug());
        verify(blogPostRepository).save(any(BlogPost.class));
    }
    
    @Test
    void createPost_shouldDefaultToDraftWhenStatusNotProvided() {
        // Arrange
        CreatePostRequest request = new CreatePostRequest(
            "Test Post",
            "Test excerpt",
            "Test body",
            null,
            null,
            null
        );
        
        BlogPost draftPost = new BlogPost("Test Post", "test-post", "Test excerpt", "Test body", PostStatus.DRAFT);
        draftPost.setId(1L);
        
        when(slugService.generateSlug("Test Post")).thenReturn("test-post");
        when(slugService.ensureUniqueSlug("test-post", BlogPost.class)).thenReturn("test-post");
        when(blogPostRepository.save(any(BlogPost.class))).thenReturn(draftPost);
        
        // Act
        BlogPostDetailDTO result = blogPostService.createPost(request);
        
        // Assert
        assertNotNull(result);
        assertEquals(PostStatus.DRAFT, result.status());
        verify(blogPostRepository).save(argThat(post -> post.getStatus() == PostStatus.DRAFT));
    }
    
    @Test
    void createPost_shouldSetPublishedAtWhenStatusIsPublished() {
        // Arrange
        CreatePostRequest request = new CreatePostRequest(
            "Test Post",
            "Test excerpt",
            "Test body",
            null,
            null,
            PostStatus.PUBLISHED
        );
        
        when(slugService.generateSlug("Test Post")).thenReturn("test-post");
        when(slugService.ensureUniqueSlug("test-post", BlogPost.class)).thenReturn("test-post");
        when(blogPostRepository.save(any(BlogPost.class))).thenReturn(testPost);
        
        // Act
        BlogPostDetailDTO result = blogPostService.createPost(request);
        
        // Assert
        assertNotNull(result);
        verify(blogPostRepository).save(argThat(post -> 
            post.getStatus() == PostStatus.PUBLISHED && post.getPublishedAt() != null
        ));
    }
    
    @Test
    void createPost_shouldThrowExceptionWhenMediaNotFound() {
        // Arrange
        CreatePostRequest request = new CreatePostRequest(
            "Test Post",
            "Test excerpt",
            "Test body",
            999L,
            null,
            null
        );
        
        when(slugService.generateSlug("Test Post")).thenReturn("test-post");
        when(slugService.ensureUniqueSlug("test-post", BlogPost.class)).thenReturn("test-post");
        when(mediaAssetRepository.findById(999L)).thenReturn(Optional.empty());
        
        // Act & Assert
        assertThrows(EntityNotFoundException.class, () -> blogPostService.createPost(request));
    }
    
    @Test
    void createPost_shouldThrowExceptionWhenTagNotFound() {
        // Arrange
        CreatePostRequest request = new CreatePostRequest(
            "Test Post",
            "Test excerpt",
            "Test body",
            null,
            Set.of(999L),
            null
        );
        
        when(slugService.generateSlug("Test Post")).thenReturn("test-post");
        when(slugService.ensureUniqueSlug("test-post", BlogPost.class)).thenReturn("test-post");
        when(tagRepository.findById(999L)).thenReturn(Optional.empty());
        
        // Act & Assert
        assertThrows(EntityNotFoundException.class, () -> blogPostService.createPost(request));
    }
    
    @Test
    void updatePost_shouldUpdatePostFields() {
        // Arrange
        UpdatePostRequest request = new UpdatePostRequest(
            "Updated Title",
            "Updated excerpt",
            "Updated body",
            null,
            null,
            null
        );
        
        when(blogPostRepository.findById(1L)).thenReturn(Optional.of(testPost));
        when(slugService.generateSlug("Updated Title")).thenReturn("updated-title");
        when(slugService.ensureUniqueSlugExcluding("updated-title", BlogPost.class, 1L)).thenReturn("updated-title");
        when(blogPostRepository.save(any(BlogPost.class))).thenReturn(testPost);
        
        // Act
        BlogPostDetailDTO result = blogPostService.updatePost(1L, request);
        
        // Assert
        assertNotNull(result);
        verify(blogPostRepository).save(any(BlogPost.class));
    }
    
    @Test
    void updatePost_shouldSetPublishedAtWhenTransitioningToPublished() {
        // Arrange
        BlogPost draftPost = new BlogPost("Test Post", "test-post", "Test excerpt", "Test body", PostStatus.DRAFT);
        draftPost.setId(1L);
        
        UpdatePostRequest request = new UpdatePostRequest(
            null,
            null,
            null,
            null,
            null,
            PostStatus.PUBLISHED
        );
        
        when(blogPostRepository.findById(1L)).thenReturn(Optional.of(draftPost));
        when(blogPostRepository.save(any(BlogPost.class))).thenReturn(draftPost);
        
        // Act
        BlogPostDetailDTO result = blogPostService.updatePost(1L, request);
        
        // Assert
        assertNotNull(result);
        verify(blogPostRepository).save(argThat(post -> 
            post.getStatus() == PostStatus.PUBLISHED && post.getPublishedAt() != null
        ));
    }
    
    @Test
    void updatePost_shouldThrowExceptionWhenPostNotFound() {
        // Arrange
        UpdatePostRequest request = new UpdatePostRequest(
            "Updated Title",
            null,
            null,
            null,
            null,
            null
        );
        
        when(blogPostRepository.findById(999L)).thenReturn(Optional.empty());
        
        // Act & Assert
        assertThrows(EntityNotFoundException.class, () -> blogPostService.updatePost(999L, request));
    }
    
    @Test
    void deletePost_shouldDeletePost() {
        // Arrange
        when(blogPostRepository.existsById(1L)).thenReturn(true);
        
        // Act
        blogPostService.deletePost(1L);
        
        // Assert
        verify(blogPostRepository).deleteById(1L);
    }
    
    @Test
    void deletePost_shouldThrowExceptionWhenPostNotFound() {
        // Arrange
        when(blogPostRepository.existsById(999L)).thenReturn(false);
        
        // Act & Assert
        assertThrows(EntityNotFoundException.class, () -> blogPostService.deletePost(999L));
    }
    
    @Test
    void getPostBySlug_shouldReturnPost() {
        // Arrange
        when(blogPostRepository.findBySlug("test-post")).thenReturn(Optional.of(testPost));
        
        // Act
        BlogPostDetailDTO result = blogPostService.getPostBySlug("test-post");
        
        // Assert
        assertNotNull(result);
        assertEquals("test-post", result.slug());
        assertEquals("Test Post", result.title());
    }
    
    @Test
    void getPostBySlug_shouldThrowExceptionWhenSlugNotFound() {
        // Arrange
        when(blogPostRepository.findBySlug("nonexistent")).thenReturn(Optional.empty());
        
        // Act & Assert
        assertThrows(EntityNotFoundException.class, () -> blogPostService.getPostBySlug("nonexistent"));
    }
    
    @Test
    void searchPosts_shouldReturnAllPublishedPostsWhenNoFilters() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        Page<BlogPost> page = new PageImpl<>(Collections.singletonList(testPost));
        
        when(blogPostRepository.findByStatus(PostStatus.PUBLISHED, pageable)).thenReturn(page);
        
        // Act
        Page<BlogPostSummaryDTO> result = blogPostService.searchPosts(null, null, PostStatus.PUBLISHED, pageable);
        
        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(blogPostRepository).findByStatus(PostStatus.PUBLISHED, pageable);
    }
    
    @Test
    void searchPosts_shouldReturnAllPostsWhenStatusIsNull() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        Page<BlogPost> page = new PageImpl<>(Collections.singletonList(testPost));
        
        when(blogPostRepository.findAll(pageable)).thenReturn(page);
        
        // Act
        Page<BlogPostSummaryDTO> result = blogPostService.searchPosts(null, null, null, pageable);
        
        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(blogPostRepository).findAll(pageable);
    }
    
    @Test
    void searchPosts_shouldFilterBySearchTermWithoutStatus() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        Page<BlogPost> page = new PageImpl<>(Collections.singletonList(testPost));
        
        when(blogPostRepository.findByTitleOrExcerptContaining("test", pageable))
            .thenReturn(page);
        
        // Act
        Page<BlogPostSummaryDTO> result = blogPostService.searchPosts("test", null, null, pageable);
        
        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(blogPostRepository).findByTitleOrExcerptContaining("test", pageable);
    }
    
    @Test
    void searchPosts_shouldFilterByTagWithoutStatus() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        Page<BlogPost> page = new PageImpl<>(Collections.singletonList(testPost));
        
        when(blogPostRepository.findByTagName("Technology", pageable))
            .thenReturn(page);
        
        // Act
        Page<BlogPostSummaryDTO> result = blogPostService.searchPosts(null, "Technology", null, pageable);
        
        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(blogPostRepository).findByTagName("Technology", pageable);
    }
    
    @Test
    void searchPosts_shouldFilterByBothSearchAndTagWithoutStatus() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        Page<BlogPost> page = new PageImpl<>(Collections.singletonList(testPost));
        
        when(blogPostRepository.findByTagNameAndTitleOrExcerptContaining("Technology", "test", pageable))
            .thenReturn(page);
        
        // Act
        Page<BlogPostSummaryDTO> result = blogPostService.searchPosts("test", "Technology", null, pageable);
        
        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(blogPostRepository).findByTagNameAndTitleOrExcerptContaining("Technology", "test", pageable);
    }
    
    @Test
    void searchPosts_shouldFilterBySearchTerm() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        Page<BlogPost> page = new PageImpl<>(Collections.singletonList(testPost));
        
        when(blogPostRepository.findByStatusAndTitleOrExcerptContaining("test", PostStatus.PUBLISHED, pageable))
            .thenReturn(page);
        
        // Act
        Page<BlogPostSummaryDTO> result = blogPostService.searchPosts("test", null, PostStatus.PUBLISHED, pageable);
        
        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(blogPostRepository).findByStatusAndTitleOrExcerptContaining("test", PostStatus.PUBLISHED, pageable);
    }
    
    @Test
    void searchPosts_shouldFilterByTag() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        Page<BlogPost> page = new PageImpl<>(Collections.singletonList(testPost));
        
        when(blogPostRepository.findByStatusAndTagName("Technology", PostStatus.PUBLISHED, pageable))
            .thenReturn(page);
        
        // Act
        Page<BlogPostSummaryDTO> result = blogPostService.searchPosts(null, "Technology", PostStatus.PUBLISHED, pageable);
        
        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(blogPostRepository).findByStatusAndTagName("Technology", PostStatus.PUBLISHED, pageable);
    }
    
    @Test
    void searchPosts_shouldFilterByBothSearchAndTag() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        Page<BlogPost> page = new PageImpl<>(Collections.singletonList(testPost));
        
        when(blogPostRepository.findByStatusAndTagNameAndTitleOrExcerptContaining("Technology", "test", PostStatus.PUBLISHED, pageable))
            .thenReturn(page);
        
        // Act
        Page<BlogPostSummaryDTO> result = blogPostService.searchPosts("test", "Technology", PostStatus.PUBLISHED, pageable);
        
        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(blogPostRepository).findByStatusAndTagNameAndTitleOrExcerptContaining("Technology", "test", PostStatus.PUBLISHED, pageable);
    }
    
    @Test
    void publishPost_shouldSetStatusToPublishedAndRecordTimestamp() {
        // Arrange
        BlogPost draftPost = new BlogPost("Test Post", "test-post", "Test excerpt", "Test body", PostStatus.DRAFT);
        draftPost.setId(1L);
        
        when(blogPostRepository.findById(1L)).thenReturn(Optional.of(draftPost));
        when(blogPostRepository.save(any(BlogPost.class))).thenReturn(draftPost);
        
        // Act
        BlogPostDetailDTO result = blogPostService.publishPost(1L);
        
        // Assert
        assertNotNull(result);
        verify(blogPostRepository).save(argThat(post -> 
            post.getStatus() == PostStatus.PUBLISHED && post.getPublishedAt() != null
        ));
    }
    
    @Test
    void publishPost_shouldNotOverwriteExistingPublishedAt() {
        // Arrange
        LocalDateTime originalPublishedAt = LocalDateTime.of(2024, 1, 1, 12, 0);
        testPost.setPublishedAt(originalPublishedAt);
        
        when(blogPostRepository.findById(1L)).thenReturn(Optional.of(testPost));
        when(blogPostRepository.save(any(BlogPost.class))).thenReturn(testPost);
        
        // Act
        BlogPostDetailDTO result = blogPostService.publishPost(1L);
        
        // Assert
        assertNotNull(result);
        verify(blogPostRepository).save(argThat(post -> 
            post.getPublishedAt().equals(originalPublishedAt)
        ));
    }
    
    @Test
    void publishPost_shouldThrowExceptionWhenPostNotFound() {
        // Arrange
        when(blogPostRepository.findById(999L)).thenReturn(Optional.empty());
        
        // Act & Assert
        assertThrows(EntityNotFoundException.class, () -> blogPostService.publishPost(999L));
    }
}
