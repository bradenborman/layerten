package com.layerten.repository;

import com.layerten.entity.RankedEntry;
import com.layerten.entity.RankedList;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for RankedEntry entity.
 * Provides methods for finding entries by list with proper ordering.
 */
@Repository
public interface RankedEntryRepository extends JpaRepository<RankedEntry, Long> {
    
    /**
     * Find all entries for a ranked list, ordered by rank in descending order (highest rank first).
     * This supports the countdown-style display where the highest-ranked item is shown first.
     * 
     * @param rankedList the ranked list to find entries for
     * @return a list of entries ordered by rank descending
     */
    List<RankedEntry> findByRankedListOrderByRankDesc(RankedList rankedList);
    
    /**
     * Find all entries for a ranked list by list ID, ordered by rank in descending order.
     * 
     * @param rankedListId the ID of the ranked list
     * @return a list of entries ordered by rank descending
     */
    List<RankedEntry> findByRankedListIdOrderByRankDesc(Long rankedListId);
}
