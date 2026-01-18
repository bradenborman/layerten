package com.layerten.service;

import com.layerten.entity.BlogPost;
import com.layerten.entity.RankedList;
import com.layerten.entity.Tag;
import com.layerten.repository.BlogPostRepository;
import com.layerten.repository.RankedListRepository;
import com.layerten.repository.TagRepository;
import org.springframework.stereotype.Service;

import java.text.Normalizer;
import java.util.Locale;
import java.util.regex.Pattern;

/**
 * Service for generating URL-safe slugs from titles.
 * Ensures slug uniqueness across entities.
 */
@Service
public class SlugService {
    
    private static final Pattern NON_LATIN = Pattern.compile("[^\\w-]");
    private static final Pattern WHITESPACE = Pattern.compile("[\\s]");
    private static final Pattern MULTIPLE_HYPHENS = Pattern.compile("-+");
    
    private final RankedListRepository rankedListRepository;
    private final BlogPostRepository blogPostRepository;
    private final TagRepository tagRepository;
    
    public SlugService(
            RankedListRepository rankedListRepository,
            BlogPostRepository blogPostRepository,
            TagRepository tagRepository) {
        this.rankedListRepository = rankedListRepository;
        this.blogPostRepository = blogPostRepository;
        this.tagRepository = tagRepository;
    }
    
    /**
     * Generate a URL-safe slug from a title.
     * Converts to lowercase, replaces spaces with hyphens, removes special characters.
     * 
     * @param title the title to convert
     * @return the generated slug
     */
    public String generateSlug(String title) {
        if (title == null || title.isBlank()) {
            return "";
        }
        
        // Normalize to decomposed form and remove accents
        String normalized = Normalizer.normalize(title, Normalizer.Form.NFD);
        
        // Convert to lowercase
        String slug = normalized.toLowerCase(Locale.ENGLISH);
        
        // Replace whitespace with hyphens
        slug = WHITESPACE.matcher(slug).replaceAll("-");
        
        // Remove non-latin characters (keep alphanumeric and hyphens)
        slug = NON_LATIN.matcher(slug).replaceAll("");
        
        // Replace multiple consecutive hyphens with single hyphen
        slug = MULTIPLE_HYPHENS.matcher(slug).replaceAll("-");
        
        // Remove leading and trailing hyphens
        slug = slug.replaceAll("^-+|-+$", "");
        
        return slug;
    }
    
    /**
     * Ensure a slug is unique by appending a number if necessary.
     * 
     * @param baseSlug the base slug to check
     * @param entityClass the entity class to check uniqueness for
     * @return a unique slug
     */
    public String ensureUniqueSlug(String baseSlug, Class<?> entityClass) {
        return ensureUniqueSlugExcluding(baseSlug, entityClass, null);
    }
    
    /**
     * Ensure a slug is unique by appending a number if necessary.
     * Excludes a specific entity ID from the uniqueness check (for updates).
     * 
     * @param baseSlug the base slug to check
     * @param entityClass the entity class to check uniqueness for
     * @param excludeId the entity ID to exclude from the check (can be null)
     * @return a unique slug
     */
    public String ensureUniqueSlugExcluding(String baseSlug, Class<?> entityClass, Long excludeId) {
        String slug = baseSlug;
        int counter = 2;
        
        while (slugExists(slug, entityClass, excludeId)) {
            slug = baseSlug + "-" + counter;
            counter++;
        }
        
        return slug;
    }
    
    /**
     * Check if a slug already exists for a given entity type.
     * 
     * @param slug the slug to check
     * @param entityClass the entity class to check
     * @param excludeId the entity ID to exclude from the check (can be null)
     * @return true if the slug exists, false otherwise
     */
    private boolean slugExists(String slug, Class<?> entityClass, Long excludeId) {
        if (entityClass == RankedList.class) {
            if (excludeId != null) {
                return rankedListRepository.findBySlug(slug)
                    .map(list -> !list.getId().equals(excludeId))
                    .orElse(false);
            }
            return rankedListRepository.existsBySlug(slug);
        } else if (entityClass == BlogPost.class) {
            if (excludeId != null) {
                return blogPostRepository.findBySlug(slug)
                    .map(post -> !post.getId().equals(excludeId))
                    .orElse(false);
            }
            return blogPostRepository.existsBySlug(slug);
        } else if (entityClass == Tag.class) {
            if (excludeId != null) {
                return tagRepository.findBySlug(slug)
                    .map(tag -> !tag.getId().equals(excludeId))
                    .orElse(false);
            }
            return tagRepository.existsBySlug(slug);
        }
        
        return false;
    }
}
