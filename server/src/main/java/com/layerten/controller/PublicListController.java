package com.layerten.controller;

import com.layerten.dto.RankedEntryDTO;
import com.layerten.dto.RankedListDetailDTO;
import com.layerten.dto.RankedListSummaryDTO;
import com.layerten.service.RankedListService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Public API controller for ranked lists.
 * Provides endpoints for browsing and viewing ranked lists.
 */
@RestController
@RequestMapping("/api/lists")
public class PublicListController {
    
    private final RankedListService rankedListService;
    
    public PublicListController(RankedListService rankedListService) {
        this.rankedListService = rankedListService;
    }
    
    /**
     * Get paginated ranked lists with optional search and tag filters.
     * 
     * @param search optional search term for title/intro
     * @param tag optional tag name to filter by
     * @param pageable pagination parameters
     * @return page of ranked list summaries
     */
    @GetMapping
    public Page<RankedListSummaryDTO> getLists(
        @RequestParam(required = false) String search,
        @RequestParam(required = false) String tag,
        Pageable pageable
    ) {
        return rankedListService.searchLists(search, tag, pageable);
    }
    
    /**
     * Get a specific ranked list by slug with all entries.
     * 
     * @param slug the list slug
     * @return the ranked list with entries
     */
    @GetMapping("/{slug}")
    public RankedListDetailDTO getListBySlug(@PathVariable String slug) {
        return rankedListService.getListBySlug(slug);
    }
    
    /**
     * Get all entries for a specific ranked list.
     * 
     * @param slug the list slug
     * @return list of ranked entries ordered by rank descending
     */
    @GetMapping("/{slug}/entries")
    public List<RankedEntryDTO> getListEntries(@PathVariable String slug) {
        RankedListDetailDTO list = rankedListService.getListBySlug(slug);
        return list.entries();
    }
}
