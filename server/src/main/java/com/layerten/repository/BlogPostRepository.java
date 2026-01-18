package com.layerten.repository;

import com.layerten.entity.BlogPost;
import com.layerten.entity.PostStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository for BlogPost entity.
 * Provides custom query methods for slug lookup, tag filtering, search, and status filtering.
 */
@Repository
public interface BlogPostRepository extends JpaRepository<BlogPost, Long> {
    
    /**
     * Find a blog post by its slug.
     * 
     * @param slug the slug to search for
     * @return an Optional containing the blog post if found
     */
    Optional<BlogPost> findBySlug(String slug);
    
    /**
     * Find all blog posts with a specific status.
     * 
     * @param status the post status to filter by
     * @param pageable pagination information
     * @return a page of blog posts with the specified status
     */
    Page<BlogPost> findByStatus(PostStatus status, Pageable pageable);
    
    /**
     * Search blog posts by title or excerpt (case-insensitive) with status filter.
     * 
     * @param search the search term
     * @param status the post status to filter by
     * @param pageable pagination information
     * @return a page of matching blog posts
     */
    @Query("SELECT bp FROM BlogPost bp WHERE bp.status = :status " +
           "AND (LOWER(bp.title) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "OR LOWER(bp.excerpt) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<BlogPost> findByStatusAndTitleOrExcerptContaining(
        @Param("search") String search,
        @Param("status") PostStatus status,
        Pageable pageable
    );
    
    /**
     * Find blog posts by tag name with status filter.
     * 
     * @param tagName the tag name to filter by
     * @param status the post status to filter by
     * @param pageable pagination information
     * @return a page of blog posts with the specified tag and status
     */
    @Query("SELECT DISTINCT bp FROM BlogPost bp JOIN bp.tags t " +
           "WHERE bp.status = :status AND t.name = :tagName")
    Page<BlogPost> findByStatusAndTagName(
        @Param("tagName") String tagName,
        @Param("status") PostStatus status,
        Pageable pageable
    );
    
    /**
     * Search blog posts by tag name and title/excerpt with status filter.
     * 
     * @param tagName the tag name to filter by
     * @param search the search term
     * @param status the post status to filter by
     * @param pageable pagination information
     * @return a page of matching blog posts
     */
    @Query("SELECT DISTINCT bp FROM BlogPost bp JOIN bp.tags t " +
           "WHERE bp.status = :status AND t.name = :tagName " +
           "AND (LOWER(bp.title) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "OR LOWER(bp.excerpt) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<BlogPost> findByStatusAndTagNameAndTitleOrExcerptContaining(
        @Param("tagName") String tagName,
        @Param("search") String search,
        @Param("status") PostStatus status,
        Pageable pageable
    );
    
    /**
     * Check if a slug already exists.
     * 
     * @param slug the slug to check
     * @return true if the slug exists, false otherwise
     */
    boolean existsBySlug(String slug);
    
    /**
     * Search blog posts by title or excerpt (case-insensitive) without status filter.
     * Used for admin queries.
     * 
     * @param search the search term
     * @param pageable pagination information
     * @return a page of matching blog posts
     */
    @Query("SELECT bp FROM BlogPost bp WHERE " +
           "LOWER(bp.title) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "OR LOWER(bp.excerpt) LIKE LOWER(CONCAT('%', :search, '%'))")
    Page<BlogPost> findByTitleOrExcerptContaining(
        @Param("search") String search,
        Pageable pageable
    );
    
    /**
     * Find blog posts by tag name without status filter.
     * Used for admin queries.
     * 
     * @param tagName the tag name to filter by
     * @param pageable pagination information
     * @return a page of blog posts with the specified tag
     */
    @Query("SELECT DISTINCT bp FROM BlogPost bp JOIN bp.tags t WHERE t.name = :tagName")
    Page<BlogPost> findByTagName(
        @Param("tagName") String tagName,
        Pageable pageable
    );
    
    /**
     * Search blog posts by tag name and title/excerpt without status filter.
     * Used for admin queries.
     * 
     * @param tagName the tag name to filter by
     * @param search the search term
     * @param pageable pagination information
     * @return a page of matching blog posts
     */
    @Query("SELECT DISTINCT bp FROM BlogPost bp JOIN bp.tags t " +
           "WHERE t.name = :tagName " +
           "AND (LOWER(bp.title) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "OR LOWER(bp.excerpt) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<BlogPost> findByTagNameAndTitleOrExcerptContaining(
        @Param("tagName") String tagName,
        @Param("search") String search,
        Pageable pageable
    );
}
