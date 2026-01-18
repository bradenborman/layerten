package com.layerten.integration;

import com.layerten.entity.*;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Basic test to verify JPA entity mappings are correct.
 * This test ensures that the entities can be loaded by Spring Boot.
 */
@SpringBootTest
@TestPropertySource(properties = {
    "spring.datasource.url=jdbc:h2:mem:testdb",
    "spring.datasource.driver-class-name=org.h2.Driver",
    "spring.jpa.hibernate.ddl-auto=create-drop",
    "spring.flyway.enabled=false"
})
class EntityMappingTest {

    @Test
    void contextLoads() {
        // This test verifies that the Spring context loads successfully
        // with all entity mappings configured correctly
        assertTrue(true);
    }
    
    @Test
    void testAuthorEntity() {
        Author author = new Author("John Doe", "john@example.com", "A test author");
        assertNotNull(author);
        assertEquals("John Doe", author.getName());
        assertEquals("john@example.com", author.getEmail());
        assertEquals("A test author", author.getBio());
    }
    
    @Test
    void testTagEntity() {
        Tag tag = new Tag("Technology", "technology");
        assertNotNull(tag);
        assertEquals("Technology", tag.getName());
        assertEquals("technology", tag.getSlug());
    }
    
    @Test
    void testMediaAssetEntity() {
        MediaAsset media = new MediaAsset("test.jpg", "image/jpeg", 1024L, "Test image", "/path/to/test.jpg");
        assertNotNull(media);
        assertEquals("test.jpg", media.getFilename());
        assertEquals("image/jpeg", media.getContentType());
        assertEquals(1024L, media.getFileSize());
    }
    
    @Test
    void testRankedListEntity() {
        RankedList list = new RankedList("Top 10 Movies", "Best of 2024", "top-10-movies", "Introduction", "Conclusion");
        assertNotNull(list);
        assertEquals("Top 10 Movies", list.getTitle());
        assertEquals("top-10-movies", list.getSlug());
    }
    
    @Test
    void testRankedEntryEntity() {
        RankedEntry entry = new RankedEntry(1, "Entry Title", "Blurb", "Commentary", "Fun fact", "https://example.com");
        assertNotNull(entry);
        assertEquals(1, entry.getRank());
        assertEquals("Entry Title", entry.getTitle());
    }
    
    @Test
    void testBlogPostEntity() {
        BlogPost post = new BlogPost("Blog Title", "blog-title", "Excerpt", "Body content", PostStatus.DRAFT);
        assertNotNull(post);
        assertEquals("Blog Title", post.getTitle());
        assertEquals(PostStatus.DRAFT, post.getStatus());
    }
    
    @Test
    void testSuggestionEntity() {
        Suggestion suggestion = new Suggestion("Suggestion Title", "Description", "Category", "Examples", "John", "john@example.com");
        assertNotNull(suggestion);
        assertEquals("Suggestion Title", suggestion.getTitle());
        assertEquals(SuggestionStatus.NEW, suggestion.getStatus());
    }
    
    @Test
    void testPostStatusEnum() {
        assertEquals(PostStatus.DRAFT, PostStatus.valueOf("DRAFT"));
        assertEquals(PostStatus.PUBLISHED, PostStatus.valueOf("PUBLISHED"));
    }
    
    @Test
    void testSuggestionStatusEnum() {
        assertEquals(SuggestionStatus.NEW, SuggestionStatus.valueOf("NEW"));
        assertEquals(SuggestionStatus.REVIEWING, SuggestionStatus.valueOf("REVIEWING"));
        assertEquals(SuggestionStatus.ACCEPTED, SuggestionStatus.valueOf("ACCEPTED"));
        assertEquals(SuggestionStatus.DECLINED, SuggestionStatus.valueOf("DECLINED"));
    }
    
    @Test
    void testRankedListEntryRelationship() {
        RankedList list = new RankedList("Test List", null, "test-list", "Intro", null);
        RankedEntry entry = new RankedEntry(1, "Entry 1", null, null, null, null);
        
        list.addEntry(entry);
        
        assertEquals(1, list.getEntries().size());
        assertEquals(list, entry.getRankedList());
    }
    
    @Test
    void testRankedListTagRelationship() {
        RankedList list = new RankedList("Test List", null, "test-list", "Intro", null);
        Tag tag = new Tag("Test Tag", "test-tag");
        
        list.addTag(tag);
        
        assertTrue(list.getTags().contains(tag));
    }
    
    @Test
    void testBlogPostTagRelationship() {
        BlogPost post = new BlogPost("Test Post", "test-post", "Excerpt", "Body", PostStatus.DRAFT);
        Tag tag = new Tag("Test Tag", "test-tag");
        
        post.addTag(tag);
        
        assertTrue(post.getTags().contains(tag));
    }
}
