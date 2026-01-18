package com.layerten.service;

import com.layerten.dto.CreateSuggestionRequest;
import com.layerten.dto.SuggestionDTO;
import com.layerten.dto.UpdateSuggestionStatusRequest;
import com.layerten.entity.Suggestion;
import com.layerten.entity.SuggestionStatus;
import com.layerten.repository.SuggestionRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for managing visitor suggestions.
 * Handles creation, retrieval, and status updates for suggestions.
 */
@Service
public class SuggestionService {
    
    private final SuggestionRepository suggestionRepository;
    
    public SuggestionService(SuggestionRepository suggestionRepository) {
        this.suggestionRepository = suggestionRepository;
    }
    
    /**
     * Create a new suggestion with initial status NEW.
     * 
     * @param request the suggestion creation request
     * @return the created suggestion as a DTO
     */
    @Transactional
    public SuggestionDTO createSuggestion(CreateSuggestionRequest request) {
        Suggestion suggestion = new Suggestion(
            request.title(),
            request.description(),
            request.category(),
            request.exampleEntries(),
            request.submitterName(),
            request.submitterEmail()
        );
        
        // Status is automatically set to NEW by the entity default
        Suggestion saved = suggestionRepository.save(suggestion);
        return toDTO(saved);
    }
    
    /**
     * Get all suggestions for admin review.
     * Returns suggestions ordered by creation date (newest first).
     * 
     * @return list of all suggestions
     */
    @Transactional(readOnly = true)
    public List<SuggestionDTO> getAllSuggestions() {
        return suggestionRepository.findAllByOrderByCreatedAtDesc()
            .stream()
            .map(this::toDTO)
            .collect(Collectors.toList());
    }
    
    /**
     * Update the status of a suggestion.
     * Validates that the status is one of the allowed values.
     * 
     * @param id the suggestion ID
     * @param request the status update request
     * @return the updated suggestion as a DTO
     * @throws EntityNotFoundException if suggestion not found
     */
    @Transactional
    public SuggestionDTO updateSuggestionStatus(Long id, UpdateSuggestionStatusRequest request) {
        Suggestion suggestion = suggestionRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Suggestion not found with id: " + id));
        
        // Validation is handled by the enum type - only valid SuggestionStatus values can be passed
        suggestion.setStatus(request.status());
        
        Suggestion updated = suggestionRepository.save(suggestion);
        return toDTO(updated);
    }
    
    /**
     * Convert a Suggestion entity to a SuggestionDTO.
     * 
     * @param suggestion the entity to convert
     * @return the DTO representation
     */
    private SuggestionDTO toDTO(Suggestion suggestion) {
        return new SuggestionDTO(
            suggestion.getId(),
            suggestion.getTitle(),
            suggestion.getDescription(),
            suggestion.getCategory(),
            suggestion.getExampleEntries(),
            suggestion.getSubmitterName(),
            suggestion.getSubmitterEmail(),
            suggestion.getStatus(),
            suggestion.getCreatedAt()
        );
    }
}
