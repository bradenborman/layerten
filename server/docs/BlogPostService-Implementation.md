# BlogPostService Implementation

## Overview
This document describes the implementation of the BlogPostService for task 5.1.

## Components Created

### DTOs
1. **BlogPostSummaryDTO** - Summary information for list views
   - id, title, slug, excerpt, coverImage, tags, status, publishedAt

2. **BlogPostDetailDTO** - Full post information for detail views
   - All summary fields plus body (markdown content)

3. **CreatePostRequest** - Request DTO for creating posts
   - title (required), excerpt (required), body (required)
   - coverImageId (optional), tagIds (optional), status (optional)
   - Validation: @NotBlank on required fields

4. **UpdatePostRequest** - Request DTO for updating posts
   - All fields optional (only provided fields are updated)
   - title, excerpt, body, coverImageId, tagIds, status

### Service Methods

#### createPost(CreatePostRequest)
- Generates unique slug from title using SlugService
- Defaults to DRAFT status if not provided
- Sets publishedAt timestamp when status is PUBLISHED
- Validates and associates cover image and tags
- **Validates Requirements: 3.1, 3.2, 3.4**

#### updatePost(Long id, UpdatePostRequest)
- Updates only provided fields
- Regenerates slug if title changes
- Sets publishedAt when transitioning from DRAFT to PUBLISHED
- Validates and updates cover image and tags
- **Validates Requirements: 3.1, 3.2**

#### deletePost(Long id)
- Deletes blog post by ID
- Throws EntityNotFoundException if post doesn't exist
- **Validates Requirements: 3.1**

#### getPostBySlug(String slug)
- Retrieves post by slug
- Returns full post details
- Throws EntityNotFoundException if slug not found
- **Validates Requirements: 3.4**

#### searchPosts(String search, String tag, PostStatus status, Pageable)
- Supports pagination with search and tag filters
- When status is null: returns all posts (admin view)
- When status is PUBLISHED: returns only published posts (public view)
- Search filters by title or excerpt (case-insensitive)
- Tag filter by tag name
- Supports combining search and tag filters
- **Validates Requirements: 3.5, 3.6, 7.3, 7.4**

#### publishPost(Long id)
- Sets status to PUBLISHED
- Records publishedAt timestamp if not already set
- Does not overwrite existing publishedAt
- **Validates Requirements: 3.3**

## Design Patterns

### Dependency Injection
- Constructor-based injection for all dependencies
- BlogPostRepository, TagRepository, MediaAssetRepository, SlugService

### Transaction Management
- @Transactional on class level for write operations
- @Transactional(readOnly = true) for read operations

### DTO Conversion
- Private helper methods for entity-to-DTO conversion
- toSummaryDTO() for list views
- toDetailDTO() for detail views
- Consistent with RankedListService pattern

### Error Handling
- EntityNotFoundException for missing resources
- Clear error messages with resource type and identifier

## Testing

### Unit Tests (BlogPostServiceTest)
Created comprehensive unit tests covering:
- ✓ Create post with generated slug
- ✓ Default to DRAFT status when not provided
- ✓ Set publishedAt when status is PUBLISHED
- ✓ Throw exception when media not found
- ✓ Throw exception when tag not found
- ✓ Update post fields
- ✓ Set publishedAt when transitioning to PUBLISHED
- ✓ Throw exception when post not found (update)
- ✓ Delete post
- ✓ Throw exception when post not found (delete)
- ✓ Get post by slug
- ✓ Throw exception when slug not found
- ✓ Search posts with no filters
- ✓ Search posts with status null (admin view)
- ✓ Filter by search term
- ✓ Filter by tag
- ✓ Filter by both search and tag
- ✓ Publish post and record timestamp
- ✓ Not overwrite existing publishedAt
- ✓ Throw exception when post not found (publish)

Total: 21 unit tests

## Requirements Validation

### Requirement 3.1: Blog Post Data Persistence
✓ createPost stores title, slug, excerpt, body, tags, cover image
✓ updatePost updates all fields
✓ deletePost removes posts

### Requirement 3.2: Blog Post Status Management
✓ createPost allows setting status to DRAFT or PUBLISHED
✓ updatePost allows changing status
✓ Default status is DRAFT

### Requirement 3.3: Publication Timestamp
✓ publishPost records publishedAt timestamp
✓ createPost sets publishedAt when status is PUBLISHED
✓ updatePost sets publishedAt when transitioning to PUBLISHED

### Requirement 3.4: Slug Generation
✓ createPost generates unique slug from title
✓ updatePost regenerates slug when title changes
✓ Uses SlugService for slug generation

### Requirement 3.5: Published Post Filtering
✓ searchPosts with status=PUBLISHED returns only published posts

### Requirement 3.6: Admin Post Access
✓ searchPosts with status=null returns all posts regardless of status

### Requirement 7.3: Post Search Functionality
✓ searchPosts filters by title or excerpt (case-insensitive)

### Requirement 7.4: Post Tag Filtering
✓ searchPosts filters by tag name
✓ Supports combining search and tag filters

## Integration with Existing Code

### SlugService
- Uses existing SlugService.generateSlug() for slug generation
- Uses existing SlugService.ensureUniqueSlug() for uniqueness
- Uses existing SlugService.ensureUniqueSlugExcluding() for updates

### Repositories
- Uses existing BlogPostRepository with custom query methods
- Uses existing TagRepository for tag lookup
- Uses existing MediaAssetRepository for media lookup

### DTOs
- Follows same pattern as RankedListService DTOs
- Uses existing MediaAssetDTO and TagDTO

## Next Steps

The BlogPostService is now complete and ready for:
1. Integration with admin controllers (Task 11.2)
2. Integration with public controllers (Task 10.2)
3. Property-based testing (Tasks 5.2-5.6)
