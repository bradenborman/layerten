# Design Document: LayerTen

## Overview

LayerTen is a monorepo web application that delivers countdown-style ranked lists and blog posts with a modern, interactive user experience. The system follows a three-tier architecture with a Spring Boot REST API backend, React TypeScript frontend, and PostgreSQL database. The design emphasizes clean separation of concerns, RESTful API principles, and an engaging "reveal" interaction pattern for ranked content.

### Key Design Principles

1. **Monorepo Structure**: Single repository with /server and /client directories for unified version control
2. **API-First Design**: Backend exposes RESTful endpoints consumed by the frontend
3. **Rank as First-Class Data**: Entry ordering is explicit via rank numbers, not implicit via creation order
4. **Stateless Backend**: Authentication via JWT or session cookies, no server-side UI rendering
5. **Media Persistence**: File-based storage with database metadata for portability between local and Railway
6. **Single Admin Model**: Simplified authentication with one admin user configured via environment variables

## Architecture

### System Components

```
┌─────────────────────────────────────────────────────────────┐
│                         Frontend                             │
│                   (React + TypeScript)                       │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐     │
│  │ Public Pages │  │ Admin Dashboard│ │ Suggestion   │     │
│  │ - Home       │  │ - Lists Mgmt  │ │ Form         │     │
│  │ - List Detail│  │ - Posts Mgmt  │ └──────────────┘     │
│  │ - Post Detail│  │ - Suggestions │                       │
│  └──────────────┘  └──────────────┘                        │
└─────────────────────────────────────────────────────────────┘
                            │ HTTP/REST
                            ▼
┌─────────────────────────────────────────────────────────────┐
│                         Backend                              │
│                    (Spring Boot 3.x)                         │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐     │
│  │ Controllers  │  │ Services     │  │ Security     │     │
│  │ - Public API │  │ - Business   │  │ - JWT/Session│     │
│  │ - Admin API  │  │   Logic      │  │ - Auth Filter│     │
│  └──────────────┘  └──────────────┘  └──────────────┘     │
│  ┌──────────────┐  ┌──────────────┐                        │
│  │ Repositories │  │ Media Service│                        │
│  │ - JPA        │  │ - File I/O   │                        │
│  └──────────────┘  └──────────────┘                        │
└─────────────────────────────────────────────────────────────┘
                            │
                ┌───────────┴───────────┐
                ▼                       ▼
        ┌──────────────┐        ┌──────────────┐
        │  PostgreSQL  │        │ File Storage │
        │   Database   │        │ (Volume/Disk)│
        └──────────────┘        └──────────────┘
```


### Technology Stack Rationale

- **Spring Boot 3.x + Java 21**: Mature ecosystem, excellent for REST APIs, strong typing, comprehensive Spring Data JPA support
- **React + TypeScript**: Type safety, component reusability, rich ecosystem for UI interactions
- **PostgreSQL**: Robust relational database with excellent JSON support for future extensibility
- **Vite**: Fast development server and optimized production builds for React
- **Railway**: Simple deployment with managed Postgres and volume storage
- **Docker Compose**: Consistent local development environment

## Components and Interfaces

### Backend Components

#### 1. Domain Entities

**Author Entity**
```java
@Entity
public class Author {
    @Id @GeneratedValue
    private Long id;
    private String name;
    private String email;
    private String bio;
    private LocalDateTime createdAt;
}
```

**Tag Entity**
```java
@Entity
public class Tag {
    @Id @GeneratedValue
    private Long id;
    @Column(unique = true)
    private String name;
    private String slug;
}
```

**RankedList Entity**
```java
@Entity
public class RankedList {
    @Id @GeneratedValue
    private Long id;
    private String title;
    private String subtitle;
    private String slug;
    @Column(columnDefinition = "TEXT")
    private String intro;
    @Column(columnDefinition = "TEXT")
    private String outro;
    @ManyToOne
    private MediaAsset coverImage;
    @ManyToMany
    private Set<Tag> tags;
    @OneToMany(mappedBy = "rankedList", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<RankedEntry> entries;
    private LocalDateTime publishedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
```

**RankedEntry Entity**
```java
@Entity
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"ranked_list_id", "rank"}))
public class RankedEntry {
    @Id @GeneratedValue
    private Long id;
    @ManyToOne
    private RankedList rankedList;
    private Integer rank;
    private String title;
    @Column(columnDefinition = "TEXT")
    private String blurb;
    @Column(columnDefinition = "TEXT")
    private String commentary;
    @Column(columnDefinition = "TEXT")
    private String funFact;
    private String externalLink;
    @ManyToOne
    private MediaAsset heroImage;
}
```

**BlogPost Entity**
```java
@Entity
public class BlogPost {
    @Id @GeneratedValue
    private Long id;
    private String title;
    private String slug;
    @Column(columnDefinition = "TEXT")
    private String excerpt;
    @Column(columnDefinition = "TEXT")
    private String body;
    @ManyToOne
    private MediaAsset coverImage;
    @ManyToMany
    private Set<Tag> tags;
    @Enumerated(EnumType.STRING)
    private PostStatus status; // DRAFT, PUBLISHED
    private LocalDateTime publishedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
```

**MediaAsset Entity**
```java
@Entity
public class MediaAsset {
    @Id @GeneratedValue
    private Long id;
    private String filename;
    private String contentType;
    private Long fileSize;
    private String altText;
    private String storagePath;
    private LocalDateTime createdAt;
}
```

**Suggestion Entity**
```java
@Entity
public class Suggestion {
    @Id @GeneratedValue
    private Long id;
    private String title;
    @Column(columnDefinition = "TEXT")
    private String description;
    private String category;
    @Column(columnDefinition = "TEXT")
    private String exampleEntries;
    private String submitterName;
    private String submitterEmail;
    @Enumerated(EnumType.STRING)
    private SuggestionStatus status; // NEW, REVIEWING, ACCEPTED, DECLINED
    private LocalDateTime createdAt;
}
```


#### 2. Repository Layer

Spring Data JPA repositories provide CRUD operations and custom queries:

```java
public interface RankedListRepository extends JpaRepository<RankedList, Long> {
    Optional<RankedList> findBySlug(String slug);
    Page<RankedList> findByPublishedAtIsNotNull(Pageable pageable);
    Page<RankedList> findByTitleContainingIgnoreCaseAndPublishedAtIsNotNull(String search, Pageable pageable);
    Page<RankedList> findByTagsNameAndPublishedAtIsNotNull(String tagName, Pageable pageable);
}

public interface RankedEntryRepository extends JpaRepository<RankedEntry, Long> {
    List<RankedEntry> findByRankedListOrderByRankDesc(RankedList rankedList);
}

public interface BlogPostRepository extends JpaRepository<BlogPost, Long> {
    Optional<BlogPost> findBySlug(String slug);
    Page<BlogPost> findByStatus(PostStatus status, Pageable pageable);
    Page<BlogPost> findByTitleContainingIgnoreCaseAndStatus(String search, PostStatus status, Pageable pageable);
    Page<BlogPost> findByTagsNameAndStatus(String tagName, PostStatus status, Pageable pageable);
}

public interface MediaAssetRepository extends JpaRepository<MediaAsset, Long> {
}

public interface SuggestionRepository extends JpaRepository<Suggestion, Long> {
    List<Suggestion> findByStatusOrderByCreatedAtDesc(SuggestionStatus status);
}

public interface TagRepository extends JpaRepository<Tag, Long> {
    Optional<Tag> findBySlug(String slug);
}
```

#### 3. Service Layer

**RankedListService**
- `createList(CreateListRequest)`: Create new ranked list with slug generation
- `updateList(Long id, UpdateListRequest)`: Update list metadata
- `deleteList(Long id)`: Delete list and cascade to entries
- `getListBySlug(String slug)`: Retrieve list with entries
- `searchLists(String search, String tag, Pageable)`: Filter and paginate lists
- `reorderEntries(Long listId, List<EntryRankUpdate>)`: Bulk update entry ranks atomically

**BlogPostService**
- `createPost(CreatePostRequest)`: Create new blog post with slug generation
- `updatePost(Long id, UpdatePostRequest)`: Update post content and metadata
- `deletePost(Long id)`: Delete post
- `getPostBySlug(String slug)`: Retrieve post
- `searchPosts(String search, String tag, PostStatus status, Pageable)`: Filter and paginate posts
- `publishPost(Long id)`: Set status to PUBLISHED and record timestamp

**MediaService**
- `uploadMedia(MultipartFile file, String altText)`: Save file to disk and create database record
- `getMediaById(Long id)`: Retrieve media metadata
- `getMediaFile(Long id)`: Stream file content with caching headers
- `deleteMedia(Long id)`: Remove file and database record

**SuggestionService**
- `createSuggestion(CreateSuggestionRequest)`: Store visitor suggestion
- `getAllSuggestions()`: Retrieve all suggestions for admin
- `updateSuggestionStatus(Long id, SuggestionStatus status)`: Change suggestion status

**SlugService**
- `generateSlug(String title)`: Convert title to URL-safe slug
- `ensureUniqueSlug(String baseSlug, Class<?> entityClass)`: Append number if slug exists


#### 4. Controller Layer

**Public API Controllers**

```java
@RestController
@RequestMapping("/api/lists")
public class PublicListController {
    @GetMapping
    public Page<RankedListSummaryDTO> getLists(
        @RequestParam(required = false) String search,
        @RequestParam(required = false) String tag,
        Pageable pageable
    );
    
    @GetMapping("/{slug}")
    public RankedListDetailDTO getListBySlug(@PathVariable String slug);
    
    @GetMapping("/{slug}/entries")
    public List<RankedEntryDTO> getListEntries(@PathVariable String slug);
}

@RestController
@RequestMapping("/api/posts")
public class PublicPostController {
    @GetMapping
    public Page<BlogPostSummaryDTO> getPosts(
        @RequestParam(required = false) String search,
        @RequestParam(required = false) String tag,
        Pageable pageable
    );
    
    @GetMapping("/{slug}")
    public BlogPostDetailDTO getPostBySlug(@PathVariable String slug);
}

@RestController
@RequestMapping("/api/suggestions")
public class SuggestionController {
    @PostMapping
    public SuggestionDTO createSuggestion(@Valid @RequestBody CreateSuggestionRequest request);
}

@RestController
@RequestMapping("/api/media")
public class MediaController {
    @GetMapping("/{id}")
    public ResponseEntity<Resource> getMedia(@PathVariable Long id);
}
```

**Admin API Controllers**

```java
@RestController
@RequestMapping("/api/admin/lists")
@PreAuthorize("hasRole('ADMIN')")
public class AdminListController {
    @PostMapping
    public RankedListDTO createList(@Valid @RequestBody CreateListRequest request);
    
    @PutMapping("/{id}")
    public RankedListDTO updateList(@PathVariable Long id, @Valid @RequestBody UpdateListRequest request);
    
    @DeleteMapping("/{id}")
    public void deleteList(@PathVariable Long id);
    
    @PostMapping("/{id}/entries")
    public RankedEntryDTO addEntry(@PathVariable Long id, @Valid @RequestBody CreateEntryRequest request);
    
    @PutMapping("/{id}/entries/reorder")
    public void reorderEntries(@PathVariable Long id, @Valid @RequestBody List<EntryRankUpdate> updates);
}

@RestController
@RequestMapping("/api/admin/posts")
@PreAuthorize("hasRole('ADMIN')")
public class AdminPostController {
    @PostMapping
    public BlogPostDTO createPost(@Valid @RequestBody CreatePostRequest request);
    
    @PutMapping("/{id}")
    public BlogPostDTO updatePost(@PathVariable Long id, @Valid @RequestBody UpdatePostRequest request);
    
    @DeleteMapping("/{id}")
    public void deletePost(@PathVariable Long id);
}

@RestController
@RequestMapping("/api/admin/media")
@PreAuthorize("hasRole('ADMIN')")
public class AdminMediaController {
    @PostMapping
    public MediaAssetDTO uploadMedia(
        @RequestParam("file") MultipartFile file,
        @RequestParam(required = false) String altText
    );
    
    @DeleteMapping("/{id}")
    public void deleteMedia(@PathVariable Long id);
}

@RestController
@RequestMapping("/api/admin/suggestions")
@PreAuthorize("hasRole('ADMIN')")
public class AdminSuggestionController {
    @GetMapping
    public List<SuggestionDTO> getAllSuggestions();
    
    @PutMapping("/{id}")
    public SuggestionDTO updateSuggestionStatus(
        @PathVariable Long id,
        @Valid @RequestBody UpdateSuggestionStatusRequest request
    );
}
```


#### 5. Security Configuration

**Authentication Strategy**: Single admin user with credentials from environment variables

```java
@Configuration
@EnableWebSecurity
public class SecurityConfig {
    @Value("${admin.username}")
    private String adminUsername;
    
    @Value("${admin.password}")
    private String adminPassword;
    
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf().disable() // For REST API
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/admin/**").authenticated()
                .requestMatchers("/api/**").permitAll()
            )
            .httpBasic() // Or JWT-based authentication
            .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS);
        return http.build();
    }
    
    @Bean
    public UserDetailsService userDetailsService() {
        UserDetails admin = User.builder()
            .username(adminUsername)
            .password(passwordEncoder().encode(adminPassword))
            .roles("ADMIN")
            .build();
        return new InMemoryUserDetailsManager(admin);
    }
    
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
```

### Frontend Components

#### 1. Public Pages

**HomePage Component**
- Displays featured ranked lists (e.g., most recent 3)
- Shows latest blog posts (e.g., most recent 3)
- Renders tag cloud or tag navigation
- Provides search bar for content discovery

**ListsIndexPage Component**
- Displays paginated grid of ranked list cards
- Provides search input and tag filters
- Shows list title, subtitle, cover image, tag badges
- Links to individual list detail pages

**ListDetailPage Component**
- Implements countdown player with two modes:
  - **Reveal Mode**: Shows entries one at a time with "Reveal Next" button
  - **Show All Mode**: Displays all entries in descending rank order
- Displays sticky progress indicator (e.g., "3 of 10 revealed")
- Supports deep linking via hash fragments (#rank-7)
- Renders entry cards with rank number, title, hero image, blurb, commentary, fun fact, external link

**PostsIndexPage Component**
- Displays paginated grid of blog post cards
- Provides search input and tag filters
- Shows post title, excerpt, cover image, publication date
- Links to individual post detail pages

**PostDetailPage Component**
- Renders markdown content with syntax highlighting
- Displays cover image, title, publication date, tags
- Provides social sharing buttons (optional)

**SuggestListPage Component**
- Form with fields: title, description, category, example entries, name, email
- Client-side validation
- Success message after submission


#### 2. Admin Dashboard

**AdminLayout Component**
- Navigation tabs: Lists, Posts, Suggestions, Media
- Logout button
- Protected route wrapper requiring authentication

**AdminListsTab Component**
- Table of all ranked lists (published and unpublished)
- Create new list button
- Edit/Delete actions per row
- Clicking edit opens list editor

**ListEditor Component**
- Form for list metadata (title, subtitle, intro, outro, tags, cover image)
- Entry management section:
  - Add new entry button
  - List of entries with rank, title, edit/delete actions
  - Drag-and-drop reordering interface
  - Bulk save button for rank updates
- Entry editor modal for adding/editing individual entries

**AdminPostsTab Component**
- Table of all blog posts with status badges
- Create new post button
- Edit/Delete actions per row
- Clicking edit opens post editor

**PostEditor Component**
- Form for post metadata (title, excerpt, tags, cover image, status)
- Markdown editor with preview pane
- Save draft / Publish buttons

**AdminSuggestionsTab Component**
- Table of all suggestions with status filter dropdown
- Displays title, description, submitter info, created date
- Status update dropdown per row (NEW, REVIEWING, ACCEPTED, DECLINED)

**AdminMediaTab Component**
- Grid of uploaded media assets with thumbnails
- Upload button with file picker
- Alt text input field
- Delete action per media item
- Displays filename, size, upload date

#### 3. Shared Components

**TagBadge Component**
- Displays tag name with styling
- Clickable to filter content by tag

**MarkdownRenderer Component**
- Renders markdown to HTML with sanitization
- Supports code syntax highlighting
- Handles images, links, lists, headings

**ImageUploader Component**
- File input with drag-and-drop support
- Image preview
- Alt text input
- Upload progress indicator

**Pagination Component**
- Page number buttons
- Previous/Next navigation
- Displays total count and current page


## Data Models

### DTOs (Data Transfer Objects)

**RankedListSummaryDTO**
```java
public record RankedListSummaryDTO(
    Long id,
    String title,
    String subtitle,
    String slug,
    MediaAssetDTO coverImage,
    Set<TagDTO> tags,
    Integer entryCount,
    LocalDateTime publishedAt
) {}
```

**RankedListDetailDTO**
```java
public record RankedListDetailDTO(
    Long id,
    String title,
    String subtitle,
    String slug,
    String intro,
    String outro,
    MediaAssetDTO coverImage,
    Set<TagDTO> tags,
    List<RankedEntryDTO> entries,
    LocalDateTime publishedAt
) {}
```

**RankedEntryDTO**
```java
public record RankedEntryDTO(
    Long id,
    Integer rank,
    String title,
    String blurb,
    String commentary,
    String funFact,
    String externalLink,
    MediaAssetDTO heroImage
) {}
```

**BlogPostSummaryDTO**
```java
public record BlogPostSummaryDTO(
    Long id,
    String title,
    String slug,
    String excerpt,
    MediaAssetDTO coverImage,
    Set<TagDTO> tags,
    PostStatus status,
    LocalDateTime publishedAt
) {}
```

**BlogPostDetailDTO**
```java
public record BlogPostDetailDTO(
    Long id,
    String title,
    String slug,
    String excerpt,
    String body,
    MediaAssetDTO coverImage,
    Set<TagDTO> tags,
    PostStatus status,
    LocalDateTime publishedAt
) {}
```

**MediaAssetDTO**
```java
public record MediaAssetDTO(
    Long id,
    String filename,
    String contentType,
    Long fileSize,
    String altText,
    String url // Computed as /api/media/{id}
) {}
```

**SuggestionDTO**
```java
public record SuggestionDTO(
    Long id,
    String title,
    String description,
    String category,
    String exampleEntries,
    String submitterName,
    String submitterEmail,
    SuggestionStatus status,
    LocalDateTime createdAt
) {}
```

**TagDTO**
```java
public record TagDTO(
    Long id,
    String name,
    String slug
) {}
```

### Request Models

**CreateListRequest**
```java
public record CreateListRequest(
    @NotBlank String title,
    String subtitle,
    @NotBlank String intro,
    String outro,
    Long coverImageId,
    Set<Long> tagIds
) {}
```

**UpdateListRequest**
```java
public record UpdateListRequest(
    String title,
    String subtitle,
    String intro,
    String outro,
    Long coverImageId,
    Set<Long> tagIds
) {}
```

**CreateEntryRequest**
```java
public record CreateEntryRequest(
    @NotNull @Positive Integer rank,
    @NotBlank String title,
    String blurb,
    String commentary,
    String funFact,
    String externalLink,
    Long heroImageId
) {}
```

**EntryRankUpdate**
```java
public record EntryRankUpdate(
    @NotNull Long entryId,
    @NotNull @Positive Integer newRank
) {}
```

**CreatePostRequest**
```java
public record CreatePostRequest(
    @NotBlank String title,
    @NotBlank String excerpt,
    @NotBlank String body,
    Long coverImageId,
    Set<Long> tagIds,
    PostStatus status
) {}
```

**CreateSuggestionRequest**
```java
public record CreateSuggestionRequest(
    @NotBlank String title,
    @NotBlank String description,
    String category,
    String exampleEntries,
    String submitterName,
    @Email String submitterEmail
) {}
```

**UpdateSuggestionStatusRequest**
```java
public record UpdateSuggestionStatusRequest(
    @NotNull SuggestionStatus status
) {}
```

### Database Schema

**Tables and Relationships**

```sql
-- Core content tables
CREATE TABLE author (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255),
    bio TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE tag (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    slug VARCHAR(100) NOT NULL UNIQUE
);

CREATE TABLE media_asset (
    id BIGSERIAL PRIMARY KEY,
    filename VARCHAR(255) NOT NULL,
    content_type VARCHAR(100) NOT NULL,
    file_size BIGINT NOT NULL,
    alt_text VARCHAR(255),
    storage_path VARCHAR(500) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE ranked_list (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    subtitle VARCHAR(255),
    slug VARCHAR(255) NOT NULL UNIQUE,
    intro TEXT NOT NULL,
    outro TEXT,
    cover_image_id BIGINT REFERENCES media_asset(id),
    published_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE ranked_entry (
    id BIGSERIAL PRIMARY KEY,
    ranked_list_id BIGINT NOT NULL REFERENCES ranked_list(id) ON DELETE CASCADE,
    rank INTEGER NOT NULL,
    title VARCHAR(255) NOT NULL,
    blurb TEXT,
    commentary TEXT,
    fun_fact TEXT,
    external_link VARCHAR(500),
    hero_image_id BIGINT REFERENCES media_asset(id),
    UNIQUE(ranked_list_id, rank)
);

CREATE TABLE blog_post (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    slug VARCHAR(255) NOT NULL UNIQUE,
    excerpt TEXT NOT NULL,
    body TEXT NOT NULL,
    cover_image_id BIGINT REFERENCES media_asset(id),
    status VARCHAR(20) NOT NULL DEFAULT 'DRAFT',
    published_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE suggestion (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    description TEXT NOT NULL,
    category VARCHAR(100),
    example_entries TEXT,
    submitter_name VARCHAR(255),
    submitter_email VARCHAR(255),
    status VARCHAR(20) NOT NULL DEFAULT 'NEW',
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

-- Many-to-many join tables
CREATE TABLE ranked_list_tags (
    ranked_list_id BIGINT NOT NULL REFERENCES ranked_list(id) ON DELETE CASCADE,
    tag_id BIGINT NOT NULL REFERENCES tag(id) ON DELETE CASCADE,
    PRIMARY KEY (ranked_list_id, tag_id)
);

CREATE TABLE blog_post_tags (
    blog_post_id BIGINT NOT NULL REFERENCES blog_post(id) ON DELETE CASCADE,
    tag_id BIGINT NOT NULL REFERENCES tag(id) ON DELETE CASCADE,
    PRIMARY KEY (blog_post_id, tag_id)
);

-- Indexes for performance
CREATE INDEX idx_ranked_list_slug ON ranked_list(slug);
CREATE INDEX idx_ranked_list_published_at ON ranked_list(published_at);
CREATE INDEX idx_ranked_entry_list_rank ON ranked_entry(ranked_list_id, rank);
CREATE INDEX idx_blog_post_slug ON blog_post(slug);
CREATE INDEX idx_blog_post_status ON blog_post(status);
CREATE INDEX idx_blog_post_published_at ON blog_post(published_at);
CREATE INDEX idx_suggestion_status ON suggestion(status);
CREATE INDEX idx_tag_slug ON tag(slug);
```


## Correctness Properties

*A property is a characteristic or behavior that should hold true across all valid executions of a system—essentially, a formal statement about what the system should do. Properties serve as the bridge between human-readable specifications and machine-verifiable correctness guarantees.*

### Property Reflection

After analyzing all acceptance criteria, several redundancies were identified:
- Requirements 8.4, 8.5, 8.6, 8.7 duplicate requirements 1.5, 5.5, 5.4, 1.3 respectively
- Many UI layout requirements (10.1-10.8, 11.1-11.8) are not functionally testable
- Infrastructure and tooling requirements (8.1, 8.8, 12.1, 12.2, 13.4-13.6) are not functional requirements
- Some configuration requirements are better tested as examples rather than properties

The following properties focus on unique, testable functional requirements.

### Data Persistence and Integrity Properties

**Property 1: Ranked list data persistence**
*For any* ranked list with title, subtitle, tags, intro, and outro, when stored by the backend, retrieving it should return all fields with their original values.
**Validates: Requirements 1.1**

**Property 2: Ranked entry data persistence**
*For any* ranked entry with rank, title, blurb, commentary, fun fact, external link, and hero image reference, when stored by the backend, retrieving it should return all fields with their original values.
**Validates: Requirements 1.2**

**Property 3: Rank uniqueness within list**
*For any* ranked list, attempting to add two entries with the same rank number should result in a constraint violation error.
**Validates: Requirements 1.3**

**Property 4: Atomic rank reordering**
*For any* ranked list with multiple entries, when reordering entries, either all rank updates succeed or none do (no partial updates).
**Validates: Requirements 1.4**

**Property 5: Cascade deletion of entries**
*For any* ranked list with entries, when the list is deleted, all associated entries should also be deleted from the database.
**Validates: Requirements 1.5**

**Property 6: Slug uniqueness for lists**
*For any* two ranked lists with the same title, the generated slugs should be unique (e.g., "top-10-movies" and "top-10-movies-2").
**Validates: Requirements 1.6**

**Property 7: Blog post data persistence**
*For any* blog post with title, slug, excerpt, body, tags, and cover image reference, when stored by the backend, retrieving it should return all fields with their original values.
**Validates: Requirements 3.1**

**Property 8: Blog post status management**
*For any* blog post, when saved with status DRAFT or PUBLISHED, retrieving it should return the same status value.
**Validates: Requirements 3.2**

**Property 9: Slug uniqueness for posts**
*For any* two blog posts with the same title, the generated slugs should be unique.
**Validates: Requirements 3.4**

**Property 10: Suggestion data persistence**
*For any* suggestion with title, description, category, example entries, and contact information, when stored by the backend, retrieving it should return all fields with their original values.
**Validates: Requirements 4.1**

**Property 11: Suggestion initial status**
*For any* newly created suggestion, its status should be set to NEW.
**Validates: Requirements 4.2**

**Property 12: Suggestion status validation**
*For any* suggestion, attempting to set its status to a value other than NEW, REVIEWING, ACCEPTED, or DECLINED should result in a validation error.
**Validates: Requirements 4.4**


### Media Management Properties

**Property 13: Media file storage**
*For any* uploaded image file, the file should exist on disk at the configured MEDIA_ROOT path after upload.
**Validates: Requirements 5.1**

**Property 14: Media metadata persistence**
*For any* uploaded image, the backend should store metadata including content type, filename, file size, creation timestamp, and alt text, and retrieving the media asset should return all metadata.
**Validates: Requirements 5.2**

**Property 15: Media caching headers**
*For any* media asset request, the HTTP response should include Cache-Control and ETag headers.
**Validates: Requirements 5.3, 15.1, 15.2**

**Property 16: Media asset relationships**
*For any* ranked list, ranked entry, or blog post with a cover/hero image, the media asset ID relationship should be stored and retrievable.
**Validates: Requirements 5.4, 5.5, 5.6**

### Query and Filtering Properties

**Property 17: Entry ordering by rank**
*For any* ranked list with entries, when retrieved by slug, the entries should be ordered by rank in descending order (highest rank first).
**Validates: Requirements 2.1, 2.7**

**Property 18: Published post filtering**
*For any* set of blog posts with mixed statuses, when a visitor requests published posts, only posts with status PUBLISHED should be returned.
**Validates: Requirements 3.5**

**Property 19: Admin post access**
*For any* set of blog posts with mixed statuses, when an admin requests posts, all posts regardless of status should be returned.
**Validates: Requirements 3.6**

**Property 20: Suggestion retrieval**
*For any* set of suggestions, when an admin requests all suggestions, every suggestion should be returned with its current status.
**Validates: Requirements 4.3**

**Property 21: List search functionality**
*For any* search term, when searching ranked lists, only lists where the title or intro contains the search term (case-insensitive) should be returned.
**Validates: Requirements 7.1**

**Property 22: List tag filtering**
*For any* tag, when filtering ranked lists by that tag, only lists associated with that tag should be returned.
**Validates: Requirements 7.2**

**Property 23: Post search functionality**
*For any* search term, when searching blog posts, only posts where the title or excerpt contains the search term (case-insensitive) should be returned.
**Validates: Requirements 7.3**

**Property 24: Post tag filtering**
*For any* tag, when filtering blog posts by that tag, only posts associated with that tag should be returned.
**Validates: Requirements 7.4**

**Property 25: Pagination metadata**
*For any* paginated content request, the response should include metadata with total count, page size, current page number, and total pages.
**Validates: Requirements 7.5**

### Relationship Properties

**Property 26: Many-to-many list-tag relationship**
*For any* ranked list and set of tags, the list should be able to have multiple tags, and each tag should be able to be associated with multiple lists.
**Validates: Requirements 8.2**

**Property 27: Many-to-many post-tag relationship**
*For any* blog post and set of tags, the post should be able to have multiple tags, and each tag should be able to be associated with multiple posts.
**Validates: Requirements 8.3**


### Authentication and Authorization Properties

**Property 28: Unauthenticated admin access denial**
*For any* admin endpoint, when accessed without valid authentication credentials, the backend should return HTTP 401 Unauthorized.
**Validates: Requirements 6.2**

**Property 29: Token validation**
*For any* authenticated request with a valid token, the backend should allow access; for any request with an invalid or expired token, the backend should deny access.
**Validates: Requirements 6.4, 6.5**

**Property 30: Admin endpoint protection**
*For any* endpoint under /api/admin/*, the backend should require authentication before allowing access.
**Validates: Requirements 6.6**

### API Contract Properties

**Property 31: List retrieval API contract**
*For any* request to GET /api/lists with search and tag parameters, the response should be a paginated list of ranked list summaries matching the filters.
**Validates: Requirements 9.1**

**Property 32: List detail API contract**
*For any* request to GET /api/lists/{slug}, the response should include the list metadata and all entries ordered by rank.
**Validates: Requirements 9.2, 9.3**

**Property 33: Post retrieval API contract**
*For any* request to GET /api/posts with search and tag parameters, the response should be a paginated list of blog post summaries matching the filters.
**Validates: Requirements 9.4**

**Property 34: Post detail API contract**
*For any* request to GET /api/posts/{slug}, the response should include the complete post with rendered markdown.
**Validates: Requirements 9.5**

**Property 35: Suggestion creation API contract**
*For any* valid suggestion data posted to POST /api/suggestions, the backend should create the suggestion and return HTTP 201 with the created suggestion.
**Validates: Requirements 9.6**

**Property 36: Media serving API contract**
*For any* request to GET /api/media/{id}, the backend should serve the file with appropriate content type and caching headers.
**Validates: Requirements 9.7**

**Property 37: HTTP status code correctness**
*For any* API request, the backend should return appropriate HTTP status codes: 200 for success, 201 for creation, 400 for validation errors, 401 for unauthorized, 404 for not found.
**Validates: Requirements 9.12**

### Validation Properties

**Property 38: Invalid data rejection**
*For any* API endpoint, when invalid data is submitted, the backend should return HTTP 400 with validation error details.
**Validates: Requirements 14.1**

**Property 39: Not found error handling**
*For any* request for a non-existent resource, the backend should return HTTP 404 with a clear error message.
**Validates: Requirements 14.2**

**Property 40: Positive rank validation**
*For any* ranked entry, attempting to set a rank that is not a positive integer should result in a validation error.
**Validates: Requirements 14.4**

**Property 41: Required field validation**
*For any* entity with required fields (title, slug, etc.), attempting to create or update with empty required fields should result in a validation error.
**Validates: Requirements 14.5**

**Property 42: Image format validation**
*For any* file upload, attempting to upload a file that is not a valid image format (JPEG, PNG, GIF, WebP) should result in a validation error.
**Validates: Requirements 14.6**

**Property 43: File size validation**
*For any* file upload, attempting to upload a file larger than the configured maximum size should result in a validation error.
**Validates: Requirements 14.7**

### Frontend State Management Properties

**Property 44: Reveal mode progress tracking**
*For any* ranked list with N entries, after revealing K entries, the progress indicator should show "K of N revealed".
**Validates: Requirements 2.5**

**Property 45: Markdown rendering**
*For any* blog post body in markdown format, the frontend should render it as properly formatted HTML with headings, lists, links, and code blocks.
**Validates: Requirements 10.6**


## Error Handling

### Backend Error Handling Strategy

**Validation Errors (HTTP 400)**
- Use Spring's `@Valid` annotation with Bean Validation constraints
- Return structured error responses with field-level error messages
- Example response:
```json
{
  "status": 400,
  "message": "Validation failed",
  "errors": [
    {"field": "title", "message": "Title must not be blank"},
    {"field": "rank", "message": "Rank must be a positive integer"}
  ]
}
```

**Authentication Errors (HTTP 401)**
- Return when no credentials provided or credentials are invalid
- Include WWW-Authenticate header for HTTP Basic or Bearer token schemes
- Clear message: "Authentication required" or "Invalid credentials"

**Authorization Errors (HTTP 403)**
- Return when authenticated user lacks permission (future use if roles expand)
- Message: "Access denied"

**Not Found Errors (HTTP 404)**
- Return when requested resource doesn't exist
- Include resource type and identifier in message
- Example: "Ranked list with slug 'top-10-movies' not found"

**Conflict Errors (HTTP 409)**
- Return when operation violates uniqueness constraints
- Example: "Entry with rank 5 already exists in this list"
- Example: "Slug 'top-10-movies' already exists"

**Server Errors (HTTP 500)**
- Catch unexpected exceptions at controller level
- Log full stack trace with correlation ID
- Return generic message to client: "An internal error occurred"
- Never expose sensitive implementation details

**File Upload Errors**
- Invalid file type: HTTP 400 with message "File must be an image (JPEG, PNG, GIF, WebP)"
- File too large: HTTP 413 with message "File size exceeds maximum of 10MB"
- Storage failure: HTTP 500 with generic error message

### Frontend Error Handling Strategy

**API Error Handling**
- Wrap all API calls in try-catch blocks
- Display user-friendly error messages in toast notifications or inline alerts
- For validation errors, highlight specific form fields with error messages
- For network errors, show retry button

**Form Validation**
- Client-side validation before submission to provide immediate feedback
- Validate required fields, email format, URL format, file types
- Display inline error messages below form fields
- Disable submit button until form is valid

**Loading States**
- Show loading spinners during async operations
- Disable form inputs during submission to prevent duplicate requests
- Provide feedback for long-running operations (e.g., file uploads)

**Graceful Degradation**
- If media fails to load, show placeholder image
- If markdown rendering fails, display raw text
- If tag loading fails, hide tag navigation rather than breaking page


## Testing Strategy

### Dual Testing Approach

LayerTen will use both **unit tests** and **property-based tests** for comprehensive coverage. These approaches are complementary:

- **Unit tests**: Verify specific examples, edge cases, and integration points
- **Property tests**: Verify universal properties across all inputs through randomization

Unit tests should focus on concrete scenarios and edge cases, while property tests handle broad input coverage. Avoid writing too many unit tests for scenarios that property tests already cover.

### Backend Testing

**Property-Based Testing with JUnit QuickCheck**

JUnit QuickCheck is a property-based testing library for Java that integrates with JUnit 5. Each property test will:
- Run a minimum of 100 iterations with randomized inputs
- Reference the design document property in a comment
- Tag format: `// Feature: layerten, Property {number}: {property_text}`

Example property test:
```java
@Property(trials = 100)
// Feature: layerten, Property 1: Ranked list data persistence
void rankedListDataPersistence(@ForAll String title, @ForAll String intro) {
    // Create list with random data
    RankedList list = new RankedList(title, intro);
    RankedList saved = repository.save(list);
    
    // Retrieve and verify all fields match
    RankedList retrieved = repository.findById(saved.getId()).orElseThrow();
    assertEquals(title, retrieved.getTitle());
    assertEquals(intro, retrieved.getIntro());
}
```

**Unit Testing with JUnit 5**

Unit tests will cover:
- Specific examples of business logic
- Edge cases (empty strings, null values, boundary conditions)
- Error conditions and exception handling
- Integration between service and repository layers
- Controller endpoint contracts with MockMvc

Example unit test:
```java
@Test
void shouldReturnNotFoundWhenListDoesNotExist() {
    mockMvc.perform(get("/api/lists/nonexistent-slug"))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.message").value("Ranked list with slug 'nonexistent-slug' not found"));
}
```

**Test Coverage Goals**
- Service layer: 80%+ line coverage
- Controller layer: 100% endpoint coverage
- Repository layer: Custom queries tested
- All 45 correctness properties implemented as property tests
- Critical edge cases covered by unit tests

### Frontend Testing

**Component Testing with React Testing Library**

Focus on user interactions and component behavior:
- Render components with various props
- Simulate user events (clicks, form inputs)
- Verify DOM updates and state changes
- Mock API calls with MSW (Mock Service Worker)

Example component test:
```typescript
test('reveal mode shows one entry at a time', () => {
  const entries = [
    { rank: 10, title: 'Entry 10' },
    { rank: 9, title: 'Entry 9' }
  ];
  
  render(<ListDetail entries={entries} />);
  
  // Initially shows only first entry
  expect(screen.getByText('Entry 10')).toBeInTheDocument();
  expect(screen.queryByText('Entry 9')).not.toBeInTheDocument();
  
  // After clicking reveal
  fireEvent.click(screen.getByText('Reveal Next'));
  expect(screen.getByText('Entry 9')).toBeInTheDocument();
});
```

**Integration Testing**

Test complete user flows:
- Browse lists → view list detail → reveal entries
- Submit suggestion form → verify success message
- Admin login → create list → add entries → publish

**Property-Based Testing with fast-check**

For frontend logic that processes data (e.g., markdown rendering, slug generation):
```typescript
fc.assert(
  fc.property(fc.string(), (markdown) => {
    // Feature: layerten, Property 45: Markdown rendering
    const html = renderMarkdown(markdown);
    // Verify HTML is valid and safe (no script tags)
    expect(html).not.toContain('<script>');
  }),
  { numRuns: 100 }
);
```

### Integration Testing

**API Integration Tests**

Test complete request-response cycles:
- Use TestRestTemplate or WebTestClient
- Test with real database (H2 or Testcontainers PostgreSQL)
- Verify end-to-end flows: create list → add entries → retrieve → verify order

**Database Integration Tests**

- Use @DataJpaTest for repository tests
- Verify cascade operations, constraints, and relationships
- Test transaction rollback on errors

### Test Data Management

**Test Fixtures**
- Create builder classes for test entities
- Use factory methods for common test scenarios
- Randomize non-critical fields to avoid test coupling

**Database Cleanup**
- Use @Transactional with rollback for unit tests
- Use @DirtiesContext sparingly for integration tests
- Clear test data between test classes

### Continuous Integration

**CI Pipeline**
- Run all tests on every commit
- Fail build if any test fails
- Generate coverage reports
- Run property tests with increased iterations (500+) on main branch

### Manual Testing Checklist

Before deployment, manually verify:
- [ ] Admin login works
- [ ] Create and publish a ranked list
- [ ] Reveal mode works correctly
- [ ] Deep linking to specific ranks works
- [ ] Image upload and display works
- [ ] Suggestion form submission works
- [ ] Search and tag filtering works
- [ ] Mobile responsive design works
- [ ] Railway deployment with volume storage works

