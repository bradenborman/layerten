package com.layerten.repository;

import com.layerten.entity.Suggestion;
import com.layerten.entity.SuggestionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for Suggestion entity.
 * Provides methods for filtering suggestions by status.
 */
@Repository
public interface SuggestionRepository extends JpaRepository<Suggestion, Long> {
    
    /**
     * Find all suggestions with a specific status, ordered by creation date descending (newest first).
     * 
     * @param status the suggestion status to filter by
     * @return a list of suggestions with the specified status
     */
    List<Suggestion> findByStatusOrderByCreatedAtDesc(SuggestionStatus status);
    
    /**
     * Find all suggestions ordered by creation date descending (newest first).
     * 
     * @return a list of all suggestions
     */
    List<Suggestion> findAllByOrderByCreatedAtDesc();
}
