package com.layerten.service;

import com.layerten.entity.RankedList;
import com.layerten.repository.BlogPostRepository;
import com.layerten.repository.RankedListRepository;
import com.layerten.repository.TagRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for SlugService.
 */
@ExtendWith(MockitoExtension.class)
class SlugServiceTest {
    
    @Mock
    private RankedListRepository rankedListRepository;
    
    @Mock
    private BlogPostRepository blogPostRepository;
    
    @Mock
    private TagRepository tagRepository;
    
    @InjectMocks
    private SlugService slugService;
    
    @Test
    void generateSlug_shouldConvertTitleToSlug() {
        // Act
        String slug = slugService.generateSlug("Top 10 Movies of 2024");
        
        // Assert
        assertEquals("top-10-movies-of-2024", slug);
    }
    
    @Test
    void generateSlug_shouldHandleSpecialCharacters() {
        // Act
        String slug = slugService.generateSlug("Best Movies! (2024) & TV Shows");
        
        // Assert
        assertEquals("best-movies-2024-tv-shows", slug);
    }
    
    @Test
    void generateSlug_shouldHandleMultipleSpaces() {
        // Act
        String slug = slugService.generateSlug("Top   10   Movies");
        
        // Assert
        assertEquals("top-10-movies", slug);
    }
    
    @Test
    void generateSlug_shouldHandleLeadingAndTrailingSpaces() {
        // Act
        String slug = slugService.generateSlug("  Top 10 Movies  ");
        
        // Assert
        assertEquals("top-10-movies", slug);
    }
    
    @Test
    void generateSlug_shouldHandleAccentedCharacters() {
        // Act
        String slug = slugService.generateSlug("Café Résumé");
        
        // Assert
        assertEquals("cafe-resume", slug);
    }
    
    @Test
    void generateSlug_shouldReturnEmptyStringForNullTitle() {
        // Act
        String slug = slugService.generateSlug(null);
        
        // Assert
        assertEquals("", slug);
    }
    
    @Test
    void generateSlug_shouldReturnEmptyStringForBlankTitle() {
        // Act
        String slug = slugService.generateSlug("   ");
        
        // Assert
        assertEquals("", slug);
    }
    
    @Test
    void ensureUniqueSlug_shouldReturnBaseSlugWhenUnique() {
        // Arrange
        when(rankedListRepository.existsBySlug("top-10-movies")).thenReturn(false);
        
        // Act
        String slug = slugService.ensureUniqueSlug("top-10-movies", RankedList.class);
        
        // Assert
        assertEquals("top-10-movies", slug);
    }
    
    @Test
    void ensureUniqueSlug_shouldAppendNumberWhenSlugExists() {
        // Arrange
        when(rankedListRepository.existsBySlug("top-10-movies")).thenReturn(true);
        when(rankedListRepository.existsBySlug("top-10-movies-2")).thenReturn(false);
        
        // Act
        String slug = slugService.ensureUniqueSlug("top-10-movies", RankedList.class);
        
        // Assert
        assertEquals("top-10-movies-2", slug);
    }
    
    @Test
    void ensureUniqueSlug_shouldIncrementNumberUntilUnique() {
        // Arrange
        when(rankedListRepository.existsBySlug("top-10-movies")).thenReturn(true);
        when(rankedListRepository.existsBySlug("top-10-movies-2")).thenReturn(true);
        when(rankedListRepository.existsBySlug("top-10-movies-3")).thenReturn(false);
        
        // Act
        String slug = slugService.ensureUniqueSlug("top-10-movies", RankedList.class);
        
        // Assert
        assertEquals("top-10-movies-3", slug);
    }
}
