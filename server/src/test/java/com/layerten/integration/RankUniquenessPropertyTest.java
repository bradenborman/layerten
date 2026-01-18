package com.layerten.integration;

import com.layerten.entity.RankedEntry;
import com.layerten.entity.RankedList;
import com.layerten.repository.RankedEntryRepository;
import com.layerten.repository.RankedListRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.TestPropertySource;

import org.springframework.dao.DataIntegrityViolationException;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Property-based test for rank uniqueness within a ranked list.
 * 
 * Feature: layerten, Property 3: Rank uniqueness within list
 * Validates: Requirements 1.3
 * 
 * This test verifies that attempting to add two entries with the same rank number
 * to a ranked list results in a constraint violation error.
 */
@DataJpaTest
@TestPropertySource(properties = {
    "spring.datasource.url=jdbc:h2:mem:testdb",
    "spring.datasource.driver-class-name=org.h2.Driver",
    "spring.jpa.hibernate.ddl-auto=create-drop",
    "spring.flyway.enabled=false"
})
public class RankUniquenessPropertyTest {
    
    @Autowired
    private RankedListRepository rankedListRepository;
    
    @Autowired
    private RankedEntryRepository rankedEntryRepository;
    
    @Autowired
    private TestEntityManager entityManager;
    
    /**
     * Property test: For any ranked list, attempting to add two entries with the same rank
     * should result in a constraint violation error.
     * 
     * This test runs 100 iterations with random rank values to verify the property holds
     * across a wide range of inputs.
     */
    @Test
    public void rankUniquenessWithinList() {
        // Feature: layerten, Property 3: Rank uniqueness within list
        
        Random random = new Random();
        int trials = 100;
        
        for (int i = 0; i < trials; i++) {
            // Generate a random positive rank (1 to 1000)
            int rank = random.nextInt(1000) + 1;
            
            // Create a ranked list with unique slug for each iteration
            RankedList list = new RankedList(
                "Test List " + i,
                "Subtitle",
                "test-list-" + System.nanoTime() + "-" + i,
                "Introduction text",
                "Conclusion text"
            );
            RankedList savedList = rankedListRepository.saveAndFlush(list);
            
            // Create first entry with the rank
            RankedEntry entry1 = new RankedEntry(
                rank,
                "Entry 1",
                "Blurb 1",
                "Commentary 1",
                "Fun fact 1",
                "https://example.com/1"
            );
            entry1.setRankedList(savedList);
            rankedEntryRepository.saveAndFlush(entry1);
            
            // Create second entry with the SAME rank
            RankedEntry entry2 = new RankedEntry(
                rank,
                "Entry 2",
                "Blurb 2",
                "Commentary 2",
                "Fun fact 2",
                "https://example.com/2"
            );
            entry2.setRankedList(savedList);
            
            // Attempting to save the second entry with the same rank should fail
            final int currentRank = rank;
            assertThrows(
                DataIntegrityViolationException.class,
                () -> {
                    rankedEntryRepository.saveAndFlush(entry2);
                },
                "Expected DataIntegrityViolationException when adding duplicate rank " + currentRank + " to the same list (iteration " + i + ")"
            );
            
            // Clear the entity manager to reset the session after the exception
            entityManager.clear();
            
            // Clean up for next iteration
            rankedEntryRepository.deleteAll();
            rankedListRepository.deleteAll();
        }
    }
    
    /**
     * Complementary test: Verify that different ranks within the same list are allowed.
     * This ensures the uniqueness constraint only applies to duplicate ranks, not all entries.
     */
    @Test
    public void differentRanksInSameListAreAllowed() {
        Random random = new Random();
        
        // Create a ranked list
        RankedList list = new RankedList(
            "Test List",
            "Subtitle",
            "test-list-different-ranks",
            "Introduction text",
            "Conclusion text"
        );
        RankedList savedList = rankedListRepository.saveAndFlush(list);
        
        // Add multiple entries with different ranks - should all succeed
        for (int i = 1; i <= 10; i++) {
            RankedEntry entry = new RankedEntry(
                i,
                "Entry " + i,
                "Blurb " + i,
                "Commentary " + i,
                "Fun fact " + i,
                "https://example.com/" + i
            );
            entry.setRankedList(savedList);
            rankedEntryRepository.saveAndFlush(entry);
        }
        
        // Verify all entries were saved
        assertEquals(10, rankedEntryRepository.findByRankedListOrderByRankDesc(savedList).size());
        
        // Clean up
        rankedEntryRepository.deleteAll();
        rankedListRepository.deleteAll();
    }
    
    /**
     * Complementary test: Verify that the same rank can be used in different lists.
     * The uniqueness constraint is per-list, not global.
     */
    @Test
    public void sameRankInDifferentListsIsAllowed() {
        int rank = 5;
        
        // Create first list with an entry at rank 5
        RankedList list1 = new RankedList(
            "Test List 1",
            "Subtitle",
            "test-list-1-same-rank",
            "Introduction text",
            "Conclusion text"
        );
        RankedList savedList1 = rankedListRepository.saveAndFlush(list1);
        
        RankedEntry entry1 = new RankedEntry(
            rank,
            "Entry 1",
            "Blurb 1",
            "Commentary 1",
            "Fun fact 1",
            "https://example.com/1"
        );
        entry1.setRankedList(savedList1);
        rankedEntryRepository.saveAndFlush(entry1);
        
        // Create second list with an entry at the SAME rank 5 - should succeed
        RankedList list2 = new RankedList(
            "Test List 2",
            "Subtitle",
            "test-list-2-same-rank",
            "Introduction text",
            "Conclusion text"
        );
        RankedList savedList2 = rankedListRepository.saveAndFlush(list2);
        
        RankedEntry entry2 = new RankedEntry(
            rank,
            "Entry 2",
            "Blurb 2",
            "Commentary 2",
            "Fun fact 2",
            "https://example.com/2"
        );
        entry2.setRankedList(savedList2);
        
        // This should NOT throw an exception because it's a different list
        assertDoesNotThrow(() -> {
            rankedEntryRepository.saveAndFlush(entry2);
        });
        
        // Verify both entries exist
        assertEquals(1, rankedEntryRepository.findByRankedListOrderByRankDesc(savedList1).size());
        assertEquals(1, rankedEntryRepository.findByRankedListOrderByRankDesc(savedList2).size());
        
        // Clean up
        rankedEntryRepository.deleteAll();
        rankedListRepository.deleteAll();
    }
}
