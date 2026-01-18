package com.layerten.repository;

import com.layerten.entity.RankedList;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository for RankedList entity.
 * Provides custom query methods for slug lookup, tag filtering, search, and status filtering.
 */
@Repository
public interface RankedListRepository extends JpaRepository<RankedList, Long> {
    
    /**
     * Find a ranked list by its slug.
     * 
     * @param slug the slug to search for
     * @return an Optional containing the ranked list if found
     */
    Optional<RankedList> findBySlug(String slug);
    
    /**
     * Find all published ranked lists (where publishedAt is not null).
     * 
     * @param pageable pagination information
     * @return a page of published ranked lists
     */
    Page<RankedList> findByPublishedAtIsNotNull(Pageable pageable);
    
    /**
     * Search published ranked lists by title or intro text (case-insensitive).
     * 
     * @param search the search term
     * @param pageable pagination information
     * @return a page of matching published ranked lists
     */
    @Query("SELECT rl FROM RankedList rl WHERE rl.publishedAt IS NOT NULL " +
           "AND (LOWER(rl.title) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "OR LOWER(rl.intro) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<RankedList> findPublishedByTitleOrIntroContaining(@Param("search") String search, Pageable pageable);
    
    /**
     * Find published ranked lists by tag name.
     * 
     * @param tagName the tag name to filter by
     * @param pageable pagination information
     * @return a page of published ranked lists with the specified tag
     */
    @Query("SELECT DISTINCT rl FROM RankedList rl JOIN rl.tags t " +
           "WHERE rl.publishedAt IS NOT NULL AND t.name = :tagName")
    Page<RankedList> findPublishedByTagName(@Param("tagName") String tagName, Pageable pageable);
    
    /**
     * Search published ranked lists by tag name and title/intro text.
     * 
     * @param tagName the tag name to filter by
     * @param search the search term
     * @param pageable pagination information
     * @return a page of matching published ranked lists
     */
    @Query("SELECT DISTINCT rl FROM RankedList rl JOIN rl.tags t " +
           "WHERE rl.publishedAt IS NOT NULL AND t.name = :tagName " +
           "AND (LOWER(rl.title) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "OR LOWER(rl.intro) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<RankedList> findPublishedByTagNameAndTitleOrIntroContaining(
        @Param("tagName") String tagName, 
        @Param("search") String search, 
        Pageable pageable
    );
    
    /**
     * Check if a slug already exists.
     * 
     * @param slug the slug to check
     * @return true if the slug exists, false otherwise
     */
    boolean existsBySlug(String slug);
}
