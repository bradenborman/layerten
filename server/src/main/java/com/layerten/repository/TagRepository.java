package com.layerten.repository;

import com.layerten.entity.Tag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository for Tag entity.
 * Provides methods for tag lookup by slug and name.
 */
@Repository
public interface TagRepository extends JpaRepository<Tag, Long> {
    
    /**
     * Find a tag by its slug.
     * 
     * @param slug the slug to search for
     * @return an Optional containing the tag if found
     */
    Optional<Tag> findBySlug(String slug);
    
    /**
     * Find a tag by its name.
     * 
     * @param name the name to search for
     * @return an Optional containing the tag if found
     */
    Optional<Tag> findByName(String name);
    
    /**
     * Check if a slug already exists.
     * 
     * @param slug the slug to check
     * @return true if the slug exists, false otherwise
     */
    boolean existsBySlug(String slug);
    
    /**
     * Check if a name already exists.
     * 
     * @param name the name to check
     * @return true if the name exists, false otherwise
     */
    boolean existsByName(String name);
}
