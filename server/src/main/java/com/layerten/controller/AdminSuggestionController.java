package com.layerten.controller;

import com.layerten.dto.SuggestionDTO;
import com.layerten.dto.UpdateSuggestionStatusRequest;
import com.layerten.service.SuggestionService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Admin API controller for managing suggestions.
 * Requires authentication.
 */
@RestController
@RequestMapping("/api/admin/suggestions")
public class AdminSuggestionController {
    
    private final SuggestionService suggestionService;
    
    public AdminSuggestionController(SuggestionService suggestionService) {
        this.suggestionService = suggestionService;
    }
    
    @GetMapping
    public List<SuggestionDTO> getAllSuggestions() {
        return suggestionService.getAllSuggestions();
    }
    
    @PutMapping("/{id}")
    public SuggestionDTO updateSuggestionStatus(
        @PathVariable Long id,
        @Valid @RequestBody UpdateSuggestionStatusRequest request
    ) {
        return suggestionService.updateSuggestionStatus(id, request);
    }
}
