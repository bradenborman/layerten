package com.layerten.controller;

import com.layerten.dto.CreateSuggestionRequest;
import com.layerten.dto.SuggestionDTO;
import com.layerten.service.SuggestionService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

/**
 * Public API controller for suggestions.
 * Allows visitors to submit suggestions without authentication.
 */
@RestController
@RequestMapping("/api/suggestions")
public class SuggestionController {
    
    private final SuggestionService suggestionService;
    
    public SuggestionController(SuggestionService suggestionService) {
        this.suggestionService = suggestionService;
    }
    
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public SuggestionDTO createSuggestion(@Valid @RequestBody CreateSuggestionRequest request) {
        return suggestionService.createSuggestion(request);
    }
}
