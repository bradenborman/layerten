package com.layerten.service;

import com.layerten.dto.CreateSuggestionRequest;
import com.layerten.dto.SuggestionDTO;
import com.layerten.dto.UpdateSuggestionStatusRequest;
import com.layerten.entity.Suggestion;
import com.layerten.entity.SuggestionStatus;
import com.layerten.repository.SuggestionRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for SuggestionService.
 */
@ExtendWith(MockitoExtension.class)
class SuggestionServiceTest {
    
    @Mock
    private SuggestionRepository suggestionRepository;
    
    @InjectMocks
    private SuggestionService suggestionService;
    
    private Suggestion testSuggestion;
    
    @BeforeEach
    void setUp() {
        testSuggestion = new Suggestion(
            "Top 10 Movies",
            "A list of the best movies of all time",
            "Entertainment",
            "The Godfather, Pulp Fiction, The Dark Knight",
            "John Doe",
            "john@example.com"
        );
        testSuggestion.setId(1L);
        testSuggestion.setCreatedAt(LocalDateTime.now());
        testSuggestion.setStatus(SuggestionStatus.NEW);
    }
    
    @Test
    void createSuggestion_shouldCreateSuggestionWithStatusNew() {
        // Arrange
        CreateSuggestionRequest request = new CreateSuggestionRequest(
            "Top 10 Movies",
            "A list of the best movies of all time",
            "Entertainment",
            "The Godfather, Pulp Fiction, The Dark Knight",
            "John Doe",
            "john@example.com"
        );
        
        when(suggestionRepository.save(any(Suggestion.class))).thenReturn(testSuggestion);
        
        // Act
        SuggestionDTO result = suggestionService.createSuggestion(request);
        
        // Assert
        assertNotNull(result);
        assertEquals("Top 10 Movies", result.title());
        assertEquals("A list of the best movies of all time", result.description());
        assertEquals(SuggestionStatus.NEW, result.status());
        verify(suggestionRepository).save(any(Suggestion.class));
    }
    
    @Test
    void createSuggestion_shouldHandleOptionalFields() {
        // Arrange
        CreateSuggestionRequest request = new CreateSuggestionRequest(
            "Top 10 Movies",
            "A list of the best movies of all time",
            null,
            null,
            null,
            null
        );
        
        Suggestion minimalSuggestion = new Suggestion(
            "Top 10 Movies",
            "A list of the best movies of all time",
            null,
            null,
            null,
            null
        );
        minimalSuggestion.setId(1L);
        minimalSuggestion.setCreatedAt(LocalDateTime.now());
        
        when(suggestionRepository.save(any(Suggestion.class))).thenReturn(minimalSuggestion);
        
        // Act
        SuggestionDTO result = suggestionService.createSuggestion(request);
        
        // Assert
        assertNotNull(result);
        assertEquals("Top 10 Movies", result.title());
        assertNull(result.category());
        assertNull(result.exampleEntries());
        assertNull(result.submitterName());
        assertNull(result.submitterEmail());
    }
    
    @Test
    void getAllSuggestions_shouldReturnAllSuggestionsOrderedByCreatedAtDesc() {
        // Arrange
        Suggestion suggestion1 = new Suggestion("Title 1", "Description 1", null, null, null, null);
        suggestion1.setId(1L);
        suggestion1.setCreatedAt(LocalDateTime.now().minusDays(1));
        
        Suggestion suggestion2 = new Suggestion("Title 2", "Description 2", null, null, null, null);
        suggestion2.setId(2L);
        suggestion2.setCreatedAt(LocalDateTime.now());
        
        when(suggestionRepository.findAllByOrderByCreatedAtDesc())
            .thenReturn(Arrays.asList(suggestion2, suggestion1));
        
        // Act
        List<SuggestionDTO> result = suggestionService.getAllSuggestions();
        
        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(2L, result.get(0).id());
        assertEquals(1L, result.get(1).id());
        verify(suggestionRepository).findAllByOrderByCreatedAtDesc();
    }
    
    @Test
    void getAllSuggestions_shouldReturnEmptyListWhenNoSuggestions() {
        // Arrange
        when(suggestionRepository.findAllByOrderByCreatedAtDesc())
            .thenReturn(List.of());
        
        // Act
        List<SuggestionDTO> result = suggestionService.getAllSuggestions();
        
        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }
    
    @Test
    void updateSuggestionStatus_shouldUpdateStatusToReviewing() {
        // Arrange
        UpdateSuggestionStatusRequest request = new UpdateSuggestionStatusRequest(SuggestionStatus.REVIEWING);
        
        when(suggestionRepository.findById(1L)).thenReturn(Optional.of(testSuggestion));
        when(suggestionRepository.save(any(Suggestion.class))).thenReturn(testSuggestion);
        
        // Act
        SuggestionDTO result = suggestionService.updateSuggestionStatus(1L, request);
        
        // Assert
        assertNotNull(result);
        verify(suggestionRepository).save(argThat(suggestion -> 
            suggestion.getStatus() == SuggestionStatus.REVIEWING
        ));
    }
    
    @Test
    void updateSuggestionStatus_shouldUpdateStatusToAccepted() {
        // Arrange
        UpdateSuggestionStatusRequest request = new UpdateSuggestionStatusRequest(SuggestionStatus.ACCEPTED);
        
        when(suggestionRepository.findById(1L)).thenReturn(Optional.of(testSuggestion));
        when(suggestionRepository.save(any(Suggestion.class))).thenReturn(testSuggestion);
        
        // Act
        SuggestionDTO result = suggestionService.updateSuggestionStatus(1L, request);
        
        // Assert
        assertNotNull(result);
        verify(suggestionRepository).save(argThat(suggestion -> 
            suggestion.getStatus() == SuggestionStatus.ACCEPTED
        ));
    }
    
    @Test
    void updateSuggestionStatus_shouldUpdateStatusToDeclined() {
        // Arrange
        UpdateSuggestionStatusRequest request = new UpdateSuggestionStatusRequest(SuggestionStatus.DECLINED);
        
        when(suggestionRepository.findById(1L)).thenReturn(Optional.of(testSuggestion));
        when(suggestionRepository.save(any(Suggestion.class))).thenReturn(testSuggestion);
        
        // Act
        SuggestionDTO result = suggestionService.updateSuggestionStatus(1L, request);
        
        // Assert
        assertNotNull(result);
        verify(suggestionRepository).save(argThat(suggestion -> 
            suggestion.getStatus() == SuggestionStatus.DECLINED
        ));
    }
    
    @Test
    void updateSuggestionStatus_shouldThrowExceptionWhenSuggestionNotFound() {
        // Arrange
        UpdateSuggestionStatusRequest request = new UpdateSuggestionStatusRequest(SuggestionStatus.REVIEWING);
        
        when(suggestionRepository.findById(999L)).thenReturn(Optional.empty());
        
        // Act & Assert
        assertThrows(EntityNotFoundException.class, 
            () -> suggestionService.updateSuggestionStatus(999L, request));
    }
}
