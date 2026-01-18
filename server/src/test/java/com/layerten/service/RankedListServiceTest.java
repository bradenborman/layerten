package com.layerten.service;

import com.layerten.dto.*;
import com.layerten.entity.MediaAsset;
import com.layerten.entity.RankedEntry;
import com.layerten.entity.RankedList;
import com.layerten.entity.Tag;
import com.layerten.repository.MediaAssetRepository;
import com.layerten.repository.RankedEntryRepository;
import com.layerten.repository.RankedListRepository;
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
 * Unit tests for RankedListService.
 */
@ExtendWith(MockitoExtension.class)
class RankedListServiceTest {
    
    @Mock
    private RankedListRepository rankedListRepository;
    
    @Mock
    private RankedEntryRepository rankedEntryRepository;
    
    @Mock
    private TagRepository tagRepository;
    
    @Mock
    private MediaAssetRepository mediaAssetRepository;
    
    @Mock
    private SlugService slugService;
    
    @InjectMocks
    private RankedListService rankedListService;
    
    private RankedList testList;
    private Tag testTag;
    private MediaAsset testMedia;
    
    @BeforeEach
    void setUp() {
        testTag = new Tag("Technology", "technology");
        testTag.setId(1L);
        
        testMedia = new MediaAsset("test.jpg", "image/jpeg", 1024L, "Test image", "/media/test.jpg");
        testMedia.setId(1L);
        
        testList = new RankedList("Top 10 Movies", "Best of 2024", "top-10-movies", "Introduction", "Conclusion");
        testList.setId(1L);
        testList.setPublishedAt(LocalDateTime.now());
        testList.setCoverImage(testMedia);
        testList.addTag(testTag);
    }
    
    @Test
    void createList_shouldCreateListWithGeneratedSlug() {
        // Arrange
        CreateListRequest request = new CreateListRequest(
            "Top 10 Movies",
            "Best of 2024",
            "Introduction",
            "Conclusion",
            1L,
            Set.of(1L)
        );
        
        when(slugService.generateSlug("Top 10 Movies")).thenReturn("top-10-movies");
        when(slugService.ensureUniqueSlug("top-10-movies", RankedList.class)).thenReturn("top-10-movies");
        when(mediaAssetRepository.findById(1L)).thenReturn(Optional.of(testMedia));
        when(tagRepository.findById(1L)).thenReturn(Optional.of(testTag));
        when(rankedListRepository.save(any(RankedList.class))).thenReturn(testList);
        when(rankedEntryRepository.findByRankedListOrderByRankDesc(any(RankedList.class))).thenReturn(Collections.emptyList());
        
        // Act
        RankedListDetailDTO result = rankedListService.createList(request);
        
        // Assert
        assertNotNull(result);
        assertEquals("Top 10 Movies", result.title());
        assertEquals("top-10-movies", result.slug());
        verify(rankedListRepository).save(any(RankedList.class));
    }
    
    @Test
    void createList_shouldThrowExceptionWhenMediaNotFound() {
        // Arrange
        CreateListRequest request = new CreateListRequest(
            "Top 10 Movies",
            null,
            "Introduction",
            null,
            999L,
            null
        );
        
        when(slugService.generateSlug("Top 10 Movies")).thenReturn("top-10-movies");
        when(slugService.ensureUniqueSlug("top-10-movies", RankedList.class)).thenReturn("top-10-movies");
        when(mediaAssetRepository.findById(999L)).thenReturn(Optional.empty());
        
        // Act & Assert
        assertThrows(EntityNotFoundException.class, () -> rankedListService.createList(request));
    }
    
    @Test
    void createList_shouldThrowExceptionWhenTagNotFound() {
        // Arrange
        CreateListRequest request = new CreateListRequest(
            "Top 10 Movies",
            null,
            "Introduction",
            null,
            null,
            Set.of(999L)
        );
        
        when(slugService.generateSlug("Top 10 Movies")).thenReturn("top-10-movies");
        when(slugService.ensureUniqueSlug("top-10-movies", RankedList.class)).thenReturn("top-10-movies");
        when(tagRepository.findById(999L)).thenReturn(Optional.empty());
        
        // Act & Assert
        assertThrows(EntityNotFoundException.class, () -> rankedListService.createList(request));
    }
    
    @Test
    void updateList_shouldUpdateListFields() {
        // Arrange
        UpdateListRequest request = new UpdateListRequest(
            "Updated Title",
            "Updated Subtitle",
            "Updated Intro",
            "Updated Outro",
            null,
            null
        );
        
        when(rankedListRepository.findById(1L)).thenReturn(Optional.of(testList));
        when(slugService.generateSlug("Updated Title")).thenReturn("updated-title");
        when(slugService.ensureUniqueSlugExcluding("updated-title", RankedList.class, 1L)).thenReturn("updated-title");
        when(rankedListRepository.save(any(RankedList.class))).thenReturn(testList);
        when(rankedEntryRepository.findByRankedListOrderByRankDesc(any(RankedList.class))).thenReturn(Collections.emptyList());
        
        // Act
        RankedListDetailDTO result = rankedListService.updateList(1L, request);
        
        // Assert
        assertNotNull(result);
        verify(rankedListRepository).save(any(RankedList.class));
    }
    
    @Test
    void updateList_shouldThrowExceptionWhenListNotFound() {
        // Arrange
        UpdateListRequest request = new UpdateListRequest(
            "Updated Title",
            null,
            null,
            null,
            null,
            null
        );
        
        when(rankedListRepository.findById(999L)).thenReturn(Optional.empty());
        
        // Act & Assert
        assertThrows(EntityNotFoundException.class, () -> rankedListService.updateList(999L, request));
    }
    
    @Test
    void deleteList_shouldDeleteList() {
        // Arrange
        when(rankedListRepository.existsById(1L)).thenReturn(true);
        
        // Act
        rankedListService.deleteList(1L);
        
        // Assert
        verify(rankedListRepository).deleteById(1L);
    }
    
    @Test
    void deleteList_shouldThrowExceptionWhenListNotFound() {
        // Arrange
        when(rankedListRepository.existsById(999L)).thenReturn(false);
        
        // Act & Assert
        assertThrows(EntityNotFoundException.class, () -> rankedListService.deleteList(999L));
    }
    
    @Test
    void getListBySlug_shouldReturnListWithEntries() {
        // Arrange
        RankedEntry entry1 = new RankedEntry(10, "Entry 10", "Blurb", "Commentary", null, null);
        entry1.setId(1L);
        RankedEntry entry2 = new RankedEntry(9, "Entry 9", "Blurb", "Commentary", null, null);
        entry2.setId(2L);
        
        when(rankedListRepository.findBySlug("top-10-movies")).thenReturn(Optional.of(testList));
        when(rankedEntryRepository.findByRankedListOrderByRankDesc(testList)).thenReturn(Arrays.asList(entry1, entry2));
        
        // Act
        RankedListDetailDTO result = rankedListService.getListBySlug("top-10-movies");
        
        // Assert
        assertNotNull(result);
        assertEquals("top-10-movies", result.slug());
        assertEquals(2, result.entries().size());
        assertEquals(10, result.entries().get(0).rank());
        assertEquals(9, result.entries().get(1).rank());
    }
    
    @Test
    void getListBySlug_shouldThrowExceptionWhenSlugNotFound() {
        // Arrange
        when(rankedListRepository.findBySlug("nonexistent")).thenReturn(Optional.empty());
        
        // Act & Assert
        assertThrows(EntityNotFoundException.class, () -> rankedListService.getListBySlug("nonexistent"));
    }
    
    @Test
    void searchLists_shouldReturnAllPublishedListsWhenNoFilters() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        Page<RankedList> page = new PageImpl<>(Collections.singletonList(testList));
        
        when(rankedListRepository.findByPublishedAtIsNotNull(pageable)).thenReturn(page);
        
        // Act
        Page<RankedListSummaryDTO> result = rankedListService.searchLists(null, null, pageable);
        
        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(rankedListRepository).findByPublishedAtIsNotNull(pageable);
    }
    
    @Test
    void searchLists_shouldFilterBySearchTerm() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        Page<RankedList> page = new PageImpl<>(Collections.singletonList(testList));
        
        when(rankedListRepository.findPublishedByTitleOrIntroContaining("movies", pageable)).thenReturn(page);
        
        // Act
        Page<RankedListSummaryDTO> result = rankedListService.searchLists("movies", null, pageable);
        
        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(rankedListRepository).findPublishedByTitleOrIntroContaining("movies", pageable);
    }
    
    @Test
    void searchLists_shouldFilterByTag() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        Page<RankedList> page = new PageImpl<>(Collections.singletonList(testList));
        
        when(rankedListRepository.findPublishedByTagName("Technology", pageable)).thenReturn(page);
        
        // Act
        Page<RankedListSummaryDTO> result = rankedListService.searchLists(null, "Technology", pageable);
        
        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(rankedListRepository).findPublishedByTagName("Technology", pageable);
    }
    
    @Test
    void searchLists_shouldFilterByBothSearchAndTag() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        Page<RankedList> page = new PageImpl<>(Collections.singletonList(testList));
        
        when(rankedListRepository.findPublishedByTagNameAndTitleOrIntroContaining("Technology", "movies", pageable))
            .thenReturn(page);
        
        // Act
        Page<RankedListSummaryDTO> result = rankedListService.searchLists("movies", "Technology", pageable);
        
        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(rankedListRepository).findPublishedByTagNameAndTitleOrIntroContaining("Technology", "movies", pageable);
    }
    
    @Test
    void reorderEntries_shouldUpdateEntryRanks() {
        // Arrange
        RankedEntry entry1 = new RankedEntry(10, "Entry 10", "Blurb", "Commentary", null, null);
        entry1.setId(1L);
        entry1.setRankedList(testList);
        
        RankedEntry entry2 = new RankedEntry(9, "Entry 9", "Blurb", "Commentary", null, null);
        entry2.setId(2L);
        entry2.setRankedList(testList);
        
        List<EntryRankUpdate> updates = Arrays.asList(
            new EntryRankUpdate(1L, 5),
            new EntryRankUpdate(2L, 6)
        );
        
        when(rankedListRepository.existsById(1L)).thenReturn(true);
        when(rankedEntryRepository.findById(1L)).thenReturn(Optional.of(entry1));
        when(rankedEntryRepository.findById(2L)).thenReturn(Optional.of(entry2));
        when(rankedEntryRepository.save(any(RankedEntry.class))).thenAnswer(i -> i.getArgument(0));
        
        // Act
        rankedListService.reorderEntries(1L, updates);
        
        // Assert
        verify(rankedEntryRepository, times(2)).save(any(RankedEntry.class));
        assertEquals(5, entry1.getRank());
        assertEquals(6, entry2.getRank());
    }
    
    @Test
    void reorderEntries_shouldThrowExceptionWhenListNotFound() {
        // Arrange
        List<EntryRankUpdate> updates = Collections.singletonList(new EntryRankUpdate(1L, 5));
        
        when(rankedListRepository.existsById(999L)).thenReturn(false);
        
        // Act & Assert
        assertThrows(EntityNotFoundException.class, () -> rankedListService.reorderEntries(999L, updates));
    }
    
    @Test
    void reorderEntries_shouldThrowExceptionWhenEntryNotFound() {
        // Arrange
        List<EntryRankUpdate> updates = Collections.singletonList(new EntryRankUpdate(999L, 5));
        
        when(rankedListRepository.existsById(1L)).thenReturn(true);
        when(rankedEntryRepository.findById(999L)).thenReturn(Optional.empty());
        
        // Act & Assert
        assertThrows(EntityNotFoundException.class, () -> rankedListService.reorderEntries(1L, updates));
    }
    
    @Test
    void reorderEntries_shouldThrowExceptionWhenEntryBelongsToDifferentList() {
        // Arrange
        RankedList otherList = new RankedList("Other List", null, "other-list", "Intro", null);
        otherList.setId(2L);
        
        RankedEntry entry = new RankedEntry(10, "Entry 10", "Blurb", "Commentary", null, null);
        entry.setId(1L);
        entry.setRankedList(otherList);
        
        List<EntryRankUpdate> updates = Collections.singletonList(new EntryRankUpdate(1L, 5));
        
        when(rankedListRepository.existsById(1L)).thenReturn(true);
        when(rankedEntryRepository.findById(1L)).thenReturn(Optional.of(entry));
        
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> rankedListService.reorderEntries(1L, updates));
    }
}
