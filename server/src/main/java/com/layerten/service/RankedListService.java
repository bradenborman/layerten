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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Service for managing ranked lists.
 * Provides methods for creating, updating, deleting, and querying ranked lists.
 */
@Service
@Transactional
public class RankedListService {
    
    private final RankedListRepository rankedListRepository;
    private final RankedEntryRepository rankedEntryRepository;
    private final TagRepository tagRepository;
    private final MediaAssetRepository mediaAssetRepository;
    private final SlugService slugService;
    
    public RankedListService(
            RankedListRepository rankedListRepository,
            RankedEntryRepository rankedEntryRepository,
            TagRepository tagRepository,
            MediaAssetRepository mediaAssetRepository,
            SlugService slugService) {
        this.rankedListRepository = rankedListRepository;
        this.rankedEntryRepository = rankedEntryRepository;
        this.tagRepository = tagRepository;
        this.mediaAssetRepository = mediaAssetRepository;
        this.slugService = slugService;
    }
    
    /**
     * Create a new ranked list.
     * 
     * @param request the create list request
     * @return the created list as a DTO
     */
    public RankedListDetailDTO createList(CreateListRequest request) {
        // Generate unique slug from title
        String baseSlug = slugService.generateSlug(request.title());
        String uniqueSlug = slugService.ensureUniqueSlug(baseSlug, RankedList.class);
        
        // Create the ranked list entity
        RankedList rankedList = new RankedList(
            request.title(),
            request.subtitle(),
            uniqueSlug,
            request.intro(),
            request.outro()
        );
        
        // Set published timestamp
        rankedList.setPublishedAt(LocalDateTime.now());
        
        // Set cover image if provided
        if (request.coverImageId() != null) {
            MediaAsset coverImage = mediaAssetRepository.findById(request.coverImageId())
                .orElseThrow(() -> new EntityNotFoundException(
                    "Media asset with ID " + request.coverImageId() + " not found"));
            rankedList.setCoverImage(coverImage);
        }
        
        // Set tags if provided
        if (request.tagIds() != null && !request.tagIds().isEmpty()) {
            Set<Tag> tags = new HashSet<>();
            for (Long tagId : request.tagIds()) {
                Tag tag = tagRepository.findById(tagId)
                    .orElseThrow(() -> new EntityNotFoundException(
                        "Tag with ID " + tagId + " not found"));
                tags.add(tag);
            }
            rankedList.setTags(tags);
        }
        
        // Save and return
        RankedList saved = rankedListRepository.save(rankedList);
        return toDetailDTO(saved);
    }
    
    /**
     * Update an existing ranked list.
     * 
     * @param id the ID of the list to update
     * @param request the update list request
     * @return the updated list as a DTO
     */
    public RankedListDetailDTO updateList(Long id, UpdateListRequest request) {
        RankedList rankedList = rankedListRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException(
                "Ranked list with ID " + id + " not found"));
        
        // Update fields if provided
        if (request.title() != null && !request.title().isBlank()) {
            rankedList.setTitle(request.title());
            // Regenerate slug if title changed
            String baseSlug = slugService.generateSlug(request.title());
            String uniqueSlug = slugService.ensureUniqueSlugExcluding(baseSlug, RankedList.class, id);
            rankedList.setSlug(uniqueSlug);
        }
        
        if (request.subtitle() != null) {
            rankedList.setSubtitle(request.subtitle());
        }
        
        if (request.intro() != null && !request.intro().isBlank()) {
            rankedList.setIntro(request.intro());
        }
        
        if (request.outro() != null) {
            rankedList.setOutro(request.outro());
        }
        
        // Update cover image
        if (request.coverImageId() != null) {
            MediaAsset coverImage = mediaAssetRepository.findById(request.coverImageId())
                .orElseThrow(() -> new EntityNotFoundException(
                    "Media asset with ID " + request.coverImageId() + " not found"));
            rankedList.setCoverImage(coverImage);
        }
        
        // Update tags
        if (request.tagIds() != null) {
            Set<Tag> tags = new HashSet<>();
            for (Long tagId : request.tagIds()) {
                Tag tag = tagRepository.findById(tagId)
                    .orElseThrow(() -> new EntityNotFoundException(
                        "Tag with ID " + tagId + " not found"));
                tags.add(tag);
            }
            rankedList.setTags(tags);
        }
        
        // Save and return
        RankedList updated = rankedListRepository.save(rankedList);
        return toDetailDTO(updated);
    }
    
    /**
     * Delete a ranked list.
     * This will cascade delete all associated entries.
     * 
     * @param id the ID of the list to delete
     */
    public void deleteList(Long id) {
        if (!rankedListRepository.existsById(id)) {
            throw new EntityNotFoundException("Ranked list with ID " + id + " not found");
        }
        rankedListRepository.deleteById(id);
    }
    
    /**
     * Get a ranked list by slug with all entries.
     * 
     * @param slug the slug of the list
     * @return the list with entries as a DTO
     */
    @Transactional(readOnly = true)
    public RankedListDetailDTO getListBySlug(String slug) {
        RankedList rankedList = rankedListRepository.findBySlug(slug)
            .orElseThrow(() -> new EntityNotFoundException(
                "Ranked list with slug '" + slug + "' not found"));
        
        return toDetailDTO(rankedList);
    }
    
    /**
     * Get a ranked list by ID with all entries.
     * 
     * @param id the ID of the list
     * @return the list with entries as a DTO
     */
    @Transactional(readOnly = true)
    public RankedListDetailDTO getListById(Long id) {
        RankedList rankedList = rankedListRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException(
                "Ranked list with ID " + id + " not found"));
        
        return toDetailDTO(rankedList);
    }
    
    /**
     * Search ranked lists with pagination, search, and tag filters.
     * Only returns published lists.
     * 
     * @param search optional search term for title/intro
     * @param tag optional tag name to filter by
     * @param pageable pagination information
     * @return a page of list summaries
     */
    @Transactional(readOnly = true)
    public Page<RankedListSummaryDTO> searchLists(String search, String tag, Pageable pageable) {
        Page<RankedList> lists;
        
        if (tag != null && !tag.isBlank() && search != null && !search.isBlank()) {
            // Both tag and search filters
            lists = rankedListRepository.findPublishedByTagNameAndTitleOrIntroContaining(tag, search, pageable);
        } else if (tag != null && !tag.isBlank()) {
            // Tag filter only
            lists = rankedListRepository.findPublishedByTagName(tag, pageable);
        } else if (search != null && !search.isBlank()) {
            // Search filter only
            lists = rankedListRepository.findPublishedByTitleOrIntroContaining(search, pageable);
        } else {
            // No filters, return all published lists
            lists = rankedListRepository.findByPublishedAtIsNotNull(pageable);
        }
        
        return lists.map(this::toSummaryDTO);
    }
    
    /**
     * Reorder entries in a ranked list atomically.
     * All rank updates are performed in a single transaction.
     * 
     * @param listId the ID of the list
     * @param updates the list of entry rank updates
     */
    public void reorderEntries(Long listId, List<EntryRankUpdate> updates) {
        // Verify the list exists
        if (!rankedListRepository.existsById(listId)) {
            throw new EntityNotFoundException("Ranked list with ID " + listId + " not found");
        }
        
        // Update all entry ranks atomically
        for (EntryRankUpdate update : updates) {
            RankedEntry entry = rankedEntryRepository.findById(update.entryId())
                .orElseThrow(() -> new EntityNotFoundException(
                    "Ranked entry with ID " + update.entryId() + " not found"));
            
            // Verify the entry belongs to the specified list
            if (!entry.getRankedList().getId().equals(listId)) {
                throw new IllegalArgumentException(
                    "Entry with ID " + update.entryId() + " does not belong to list with ID " + listId);
            }
            
            entry.setRank(update.newRank());
            rankedEntryRepository.save(entry);
        }
    }
    
    /**
     * Add a new entry to a ranked list.
     * 
     * @param listId the ID of the list
     * @param request the create entry request
     * @return the created entry as a DTO
     */
    public RankedEntryDTO addEntry(Long listId, CreateEntryRequest request) {
        RankedList rankedList = rankedListRepository.findById(listId)
            .orElseThrow(() -> new EntityNotFoundException(
                "Ranked list with ID " + listId + " not found"));
        
        // Create the entry
        RankedEntry entry = new RankedEntry(
            request.rank(),
            request.title(),
            request.blurb(),
            request.commentary(),
            request.funFact(),
            request.externalLink()
        );
        
        // Set hero image if provided
        if (request.heroImageId() != null) {
            MediaAsset heroImage = mediaAssetRepository.findById(request.heroImageId())
                .orElseThrow(() -> new EntityNotFoundException(
                    "Media asset with ID " + request.heroImageId() + " not found"));
            entry.setHeroImage(heroImage);
        }
        
        // Associate with the list
        entry.setRankedList(rankedList);
        
        // Save and return
        RankedEntry saved = rankedEntryRepository.save(entry);
        return toEntryDTO(saved);
    }
    
    /**
     * Update an existing entry in a ranked list.
     * 
     * @param listId the ID of the list
     * @param entryId the ID of the entry
     * @param request the update entry request
     * @return the updated entry as a DTO
     */
    public RankedEntryDTO updateEntry(Long listId, Long entryId, CreateEntryRequest request) {
        RankedEntry entry = rankedEntryRepository.findById(entryId)
            .orElseThrow(() -> new EntityNotFoundException(
                "Entry with ID " + entryId + " not found"));
        
        // Verify the entry belongs to the specified list
        if (!entry.getRankedList().getId().equals(listId)) {
            throw new IllegalArgumentException(
                "Entry " + entryId + " does not belong to list " + listId);
        }
        
        // Update fields
        entry.setRank(request.rank());
        entry.setTitle(request.title());
        entry.setBlurb(request.blurb());
        entry.setCommentary(request.commentary());
        entry.setFunFact(request.funFact());
        entry.setExternalLink(request.externalLink());
        
        // Update hero image
        if (request.heroImageId() != null) {
            MediaAsset heroImage = mediaAssetRepository.findById(request.heroImageId())
                .orElseThrow(() -> new EntityNotFoundException(
                    "Media asset with ID " + request.heroImageId() + " not found"));
            entry.setHeroImage(heroImage);
        } else {
            entry.setHeroImage(null);
        }
        
        // Save and return
        RankedEntry saved = rankedEntryRepository.save(entry);
        return toEntryDTO(saved);
    }
    
    // Helper methods for DTO conversion
    
    private RankedListSummaryDTO toSummaryDTO(RankedList rankedList) {
        return new RankedListSummaryDTO(
            rankedList.getId(),
            rankedList.getTitle(),
            rankedList.getSubtitle(),
            rankedList.getSlug(),
            toMediaAssetDTO(rankedList.getCoverImage()),
            rankedList.getTags().stream()
                .map(this::toTagDTO)
                .collect(Collectors.toSet()),
            rankedList.getEntries().size(),
            rankedList.getPublishedAt()
        );
    }
    
    private RankedListDetailDTO toDetailDTO(RankedList rankedList) {
        // Fetch entries ordered by rank descending
        List<RankedEntry> entries = rankedEntryRepository.findByRankedListOrderByRankDesc(rankedList);
        
        return new RankedListDetailDTO(
            rankedList.getId(),
            rankedList.getTitle(),
            rankedList.getSubtitle(),
            rankedList.getSlug(),
            rankedList.getIntro(),
            rankedList.getOutro(),
            toMediaAssetDTO(rankedList.getCoverImage()),
            rankedList.getTags().stream()
                .map(this::toTagDTO)
                .collect(Collectors.toSet()),
            entries.stream()
                .map(this::toEntryDTO)
                .collect(Collectors.toList()),
            rankedList.getPublishedAt()
        );
    }
    
    private RankedEntryDTO toEntryDTO(RankedEntry entry) {
        return new RankedEntryDTO(
            entry.getId(),
            entry.getRank(),
            entry.getTitle(),
            entry.getBlurb(),
            entry.getCommentary(),
            entry.getFunFact(),
            entry.getExternalLink(),
            toMediaAssetDTO(entry.getHeroImage())
        );
    }
    
    private TagDTO toTagDTO(Tag tag) {
        return new TagDTO(
            tag.getId(),
            tag.getName(),
            tag.getSlug()
        );
    }
    
    private MediaAssetDTO toMediaAssetDTO(MediaAsset mediaAsset) {
        if (mediaAsset == null) {
            return null;
        }
        
        return new MediaAssetDTO(
            mediaAsset.getId(),
            mediaAsset.getFilename(),
            mediaAsset.getContentType(),
            mediaAsset.getFileSize(),
            mediaAsset.getAltText(),
            "/api/media/" + mediaAsset.getId()
        );
    }
}
