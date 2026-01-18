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

import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Property-based test for cascade deletion of entries when a ranked list is deleted.
 * 
 * Feature: layerten, Property 5: Cascade deletion of entries
 * Validates: Requirements 1.5
 * 
 * This test verifies that when a ranked list is deleted, all associated entries
 * are automatically deleted from the database due to the cascade delete configuration.
 */
@DataJpaTest
@TestPropertySource(properties = {
    "spring.datasource.url=jdbc:h2:mem:testdb",
    "spring.datasource.driver-class-name=org.h2.Driver",
    "spring.jpa.hibernate.ddl-auto=create-drop",
    "spring.flyway.enabled=false"
})
public class CascadeDeletionPropertyTest {
    
    @Autowired
    private RankedListRepository rankedListRepository;
    
    @Autowired
    private RankedEntryRepository rankedEntryRepository;
    
    @Autowired
    private TestEntityManager entityManager;
    
    /**
     * Property test: For any ranked list with entries, when the list is deleted,
     * all associated entries should also be deleted from the database.
     * 
     * This test runs 100 iterations with random numbers of entries to verify
     * the cascade deletion property holds across various scenarios.
     */
    @Test
    public void cascadeDeletionOfEntries() {
        // Feature: layerten, Property 5: Cascade deletion of entries
        
        Random random = new Random();
        int trials = 100;
        
        for (int i = 0; i < trials; i++) {
            // Generate a random number of entries (1 to 20)
            int entryCount = random.nextInt(20) + 1;
            
            // Create a ranked list with unique slug for each iteration
            RankedList list = new RankedList(
                "Test List " + i,
                "Subtitle " + i,
                "test-list-" + System.nanoTime() + "-" + i,
                "Introduction text for list " + i,
                "Conclusion text for list " + i
            );
            RankedList savedList = rankedListRepository.saveAndFlush(list);
            Long listId = savedList.getId();
            
            // Create multiple entries for this list
            for (int j = 1; j <= entryCount; j++) {
                RankedEntry entry = new RankedEntry(
                    j,
                    "Entry " + j + " of List " + i,
                    "Blurb " + j,
                    "Commentary " + j,
                    "Fun fact " + j,
                    "https://example.com/" + i + "/" + j
                );
                entry.setRankedList(savedList);
                rankedEntryRepository.saveAndFlush(entry);
            }
            
            // Verify all entries were created
            List<RankedEntry> entriesBeforeDeletion = rankedEntryRepository.findByRankedListOrderByRankDesc(savedList);
            assertEquals(entryCount, entriesBeforeDeletion.size(), 
                "Expected " + entryCount + " entries to be created for list " + i);
            
            // Store entry IDs for verification after deletion
            List<Long> entryIds = entriesBeforeDeletion.stream()
                .map(RankedEntry::getId)
                .toList();
            
            // Clear the entity manager to ensure we're testing actual database state
            entityManager.clear();
            
            // Delete the ranked list
            rankedListRepository.deleteById(listId);
            rankedListRepository.flush();
            
            // Clear the entity manager again to force fresh database queries
            entityManager.clear();
            
            // Verify the list is deleted
            assertFalse(rankedListRepository.existsById(listId),
                "Ranked list " + listId + " should be deleted (iteration " + i + ")");
            
            // Verify all associated entries are also deleted (cascade deletion)
            for (Long entryId : entryIds) {
                assertFalse(rankedEntryRepository.existsById(entryId),
                    "Entry " + entryId + " should be cascade deleted when list " + listId + " is deleted (iteration " + i + ")");
            }
            
            // Double-check: query for entries by the deleted list should return empty
            // Note: We can't use findByRankedListOrderByRankDesc with the deleted list object,
            // so we verify by checking that no entries exist with those IDs
            long remainingEntriesCount = rankedEntryRepository.count();
            assertEquals(0, remainingEntriesCount,
                "No entries should remain in the database after cascade deletion (iteration " + i + ")");
        }
    }
    
    /**
     * Complementary test: Verify that deleting one list does not affect entries in other lists.
     * This ensures cascade deletion is scoped to the specific list being deleted.
     */
    @Test
    public void cascadeDeletionDoesNotAffectOtherLists() {
        // Create first list with entries
        RankedList list1 = new RankedList(
            "Test List 1",
            "Subtitle 1",
            "test-list-1-cascade",
            "Introduction text 1",
            "Conclusion text 1"
        );
        RankedList savedList1 = rankedListRepository.saveAndFlush(list1);
        
        RankedEntry entry1 = new RankedEntry(
            1,
            "Entry 1 of List 1",
            "Blurb 1",
            "Commentary 1",
            "Fun fact 1",
            "https://example.com/1"
        );
        entry1.setRankedList(savedList1);
        rankedEntryRepository.saveAndFlush(entry1);
        
        RankedEntry entry2 = new RankedEntry(
            2,
            "Entry 2 of List 1",
            "Blurb 2",
            "Commentary 2",
            "Fun fact 2",
            "https://example.com/2"
        );
        entry2.setRankedList(savedList1);
        rankedEntryRepository.saveAndFlush(entry2);
        
        // Create second list with entries
        RankedList list2 = new RankedList(
            "Test List 2",
            "Subtitle 2",
            "test-list-2-cascade",
            "Introduction text 2",
            "Conclusion text 2"
        );
        RankedList savedList2 = rankedListRepository.saveAndFlush(list2);
        
        RankedEntry entry3 = new RankedEntry(
            1,
            "Entry 1 of List 2",
            "Blurb 3",
            "Commentary 3",
            "Fun fact 3",
            "https://example.com/3"
        );
        entry3.setRankedList(savedList2);
        rankedEntryRepository.saveAndFlush(entry3);
        
        Long list1Id = savedList1.getId();
        Long list2Id = savedList2.getId();
        Long entry1Id = entry1.getId();
        Long entry2Id = entry2.getId();
        Long entry3Id = entry3.getId();
        
        // Clear entity manager
        entityManager.clear();
        
        // Delete the first list
        rankedListRepository.deleteById(list1Id);
        rankedListRepository.flush();
        entityManager.clear();
        
        // Verify list 1 and its entries are deleted
        assertFalse(rankedListRepository.existsById(list1Id), "List 1 should be deleted");
        assertFalse(rankedEntryRepository.existsById(entry1Id), "Entry 1 should be cascade deleted");
        assertFalse(rankedEntryRepository.existsById(entry2Id), "Entry 2 should be cascade deleted");
        
        // Verify list 2 and its entries still exist
        assertTrue(rankedListRepository.existsById(list2Id), "List 2 should still exist");
        assertTrue(rankedEntryRepository.existsById(entry3Id), "Entry 3 should still exist");
        
        // Verify list 2 still has its entry
        RankedList retrievedList2 = rankedListRepository.findById(list2Id).orElseThrow();
        List<RankedEntry> list2Entries = rankedEntryRepository.findByRankedListOrderByRankDesc(retrievedList2);
        assertEquals(1, list2Entries.size(), "List 2 should still have 1 entry");
        
        // Clean up
        rankedEntryRepository.deleteAll();
        rankedListRepository.deleteAll();
    }
    
    /**
     * Complementary test: Verify cascade deletion works with empty lists.
     * A list with no entries should be deletable without errors.
     */
    @Test
    public void cascadeDeletionWorksWithEmptyList() {
        // Create a list with no entries
        RankedList list = new RankedList(
            "Empty Test List",
            "Subtitle",
            "empty-test-list-cascade",
            "Introduction text",
            "Conclusion text"
        );
        RankedList savedList = rankedListRepository.saveAndFlush(list);
        Long listId = savedList.getId();
        
        // Verify the list exists and has no entries
        assertTrue(rankedListRepository.existsById(listId), "List should exist");
        List<RankedEntry> entries = rankedEntryRepository.findByRankedListOrderByRankDesc(savedList);
        assertEquals(0, entries.size(), "List should have no entries");
        
        entityManager.clear();
        
        // Delete the list - should succeed without errors
        assertDoesNotThrow(() -> {
            rankedListRepository.deleteById(listId);
            rankedListRepository.flush();
        }, "Deleting an empty list should not throw an exception");
        
        entityManager.clear();
        
        // Verify the list is deleted
        assertFalse(rankedListRepository.existsById(listId), "List should be deleted");
    }
    
    /**
     * Complementary test: Verify cascade deletion works with a large number of entries.
     * This tests the performance and correctness of cascade deletion at scale.
     */
    @Test
    public void cascadeDeletionWorksWithManyEntries() {
        int largeEntryCount = 100;
        
        // Create a list with many entries
        RankedList list = new RankedList(
            "Large Test List",
            "Subtitle",
            "large-test-list-cascade",
            "Introduction text",
            "Conclusion text"
        );
        RankedList savedList = rankedListRepository.saveAndFlush(list);
        Long listId = savedList.getId();
        
        // Create many entries
        for (int i = 1; i <= largeEntryCount; i++) {
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
        
        // Verify all entries were created
        List<RankedEntry> entriesBeforeDeletion = rankedEntryRepository.findByRankedListOrderByRankDesc(savedList);
        assertEquals(largeEntryCount, entriesBeforeDeletion.size(), 
            "Expected " + largeEntryCount + " entries to be created");
        
        entityManager.clear();
        
        // Delete the list
        rankedListRepository.deleteById(listId);
        rankedListRepository.flush();
        entityManager.clear();
        
        // Verify the list is deleted
        assertFalse(rankedListRepository.existsById(listId), "List should be deleted");
        
        // Verify all entries are deleted
        long remainingEntriesCount = rankedEntryRepository.count();
        assertEquals(0, remainingEntriesCount, 
            "All " + largeEntryCount + " entries should be cascade deleted");
    }
}
