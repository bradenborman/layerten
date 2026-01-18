package com.layerten.controller;

import com.layerten.dto.*;
import com.layerten.service.RankedListService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Admin API controller for managing ranked lists.
 * Requires authentication.
 */
@RestController
@RequestMapping("/api/admin/lists")
public class AdminListController {
    
    private final RankedListService rankedListService;
    
    public AdminListController(RankedListService rankedListService) {
        this.rankedListService = rankedListService;
    }
    
    /**
     * Create a new ranked list.
     * 
     * @param request the create list request
     * @return the created list
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public RankedListDetailDTO createList(@Valid @RequestBody CreateListRequest request) {
        return rankedListService.createList(request);
    }
    
    /**
     * Update an existing ranked list.
     * 
     * @param id the list ID
     * @param request the update list request
     * @return the updated list
     */
    @PutMapping("/{id}")
    public RankedListDetailDTO updateList(
        @PathVariable Long id,
        @Valid @RequestBody UpdateListRequest request
    ) {
        return rankedListService.updateList(id, request);
    }
    
    /**
     * Delete a ranked list.
     * 
     * @param id the list ID
     */
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteList(@PathVariable Long id) {
        rankedListService.deleteList(id);
    }
    
    /**
     * Add a new entry to a ranked list.
     * 
     * @param id the list ID
     * @param request the create entry request
     * @return the created entry
     */
    @PostMapping("/{id}/entries")
    @ResponseStatus(HttpStatus.CREATED)
    public RankedEntryDTO addEntry(
        @PathVariable Long id,
        @Valid @RequestBody CreateEntryRequest request
    ) {
        return rankedListService.addEntry(id, request);
    }
    
    /**
     * Reorder entries in a ranked list.
     * 
     * @param id the list ID
     * @param updates the list of entry rank updates
     */
    @PutMapping("/{id}/entries/reorder")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void reorderEntries(
        @PathVariable Long id,
        @Valid @RequestBody List<EntryRankUpdate> updates
    ) {
        rankedListService.reorderEntries(id, updates);
    }
}
