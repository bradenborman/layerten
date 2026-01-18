# Implementation Plan: LayerTen

## Overview

This implementation plan breaks down the LayerTen monorepo web application into discrete coding tasks. The approach follows an incremental strategy: set up infrastructure first, implement backend API with core entities, then build frontend components, and finally integrate everything. Each task builds on previous work, with checkpoints to ensure stability.

## Tasks

- [x] 1. Set up monorepo structure and development environment
  - Create /server directory with Spring Boot Gradle project (Java 21, Spring Boot 3.x)
  - Create /client directory with Vite + React + TypeScript project
  - Create root docker-compose.yml for local PostgreSQL
  - Create root README.md with setup instructions
  - Configure Gradle dependencies: Spring Web, Spring Security, Spring Data JPA, Flyway, Validation, PostgreSQL driver
  - Configure Vite with React, TypeScript, TailwindCSS, React Router
  - _Requirements: 12.1, 12.2, 12.6_

- [x] 2. Implement database schema and migrations
  - [x] 2.1 Create Flyway migration for core tables
    - Create tables: author, tag, media_asset, ranked_list, ranked_entry, blog_post, suggestion
    - Create join tables: ranked_list_tags, blog_post_tags
    - Add unique constraints, foreign keys, and indexes
    - _Requirements: 8.1, 8.2, 8.3, 8.4, 8.7, 15.3_
  
  - [x] 2.2 Configure database connection
    - Add application.yml with database configuration using environment variables
    - Support both DATABASE_URL (Railway) and individual DB_* variables (local)
    - Configure Flyway to run migrations on startup
    - _Requirements: 12.3, 13.1_

- [x] 3. Implement domain entities and repositories
  - [x] 3.1 Create JPA entities
    - Implement Author, Tag, MediaAsset, RankedList, RankedEntry, BlogPost, Suggestion entities
    - Add JPA annotations, relationships, and constraints
    - Add enums: PostStatus, SuggestionStatus
    - _Requirements: 8.1, 8.2, 8.3, 8.4_
  
  - [x] 3.2 Create Spring Data JPA repositories
    - Implement repositories for all entities with custom query methods
    - Add methods for slug lookup, tag filtering, search, status filtering
    - _Requirements: 7.1, 7.2, 7.3, 7.4_
  
  - [x]* 3.3 Write property test for rank uniqueness
    - **Property 3: Rank uniqueness within list**
    - **Validates: Requirements 1.3**
  
  - [x]* 3.4 Write property test for cascade deletion
    - **Property 5: Cascade deletion of entries**
    - **Validates: Requirements 1.5**


- [x] 4. Implement service layer for ranked lists
  - [x] 4.1 Create SlugService for slug generation
    - Implement generateSlug(String title) to convert title to URL-safe slug
    - Implement ensureUniqueSlug to append numbers if slug exists
    - _Requirements: 1.6, 3.4_
  
  - [x] 4.2 Create RankedListService
    - Implement createList, updateList, deleteList methods
    - Implement getListBySlug with entries
    - Implement searchLists with pagination, search, and tag filters
    - Implement reorderEntries with atomic transaction
    - _Requirements: 1.1, 1.4, 1.5, 1.7, 7.1, 7.2_
  
  - [ ]* 4.3 Write property test for list data persistence
    - **Property 1: Ranked list data persistence**
    - **Validates: Requirements 1.1**
  
  - [ ]* 4.4 Write property test for entry data persistence
    - **Property 2: Ranked entry data persistence**
    - **Validates: Requirements 1.2**
  
  - [ ]* 4.5 Write property test for atomic rank reordering
    - **Property 4: Atomic rank reordering**
    - **Validates: Requirements 1.4**
  
  - [ ]* 4.6 Write property test for slug uniqueness
    - **Property 6: Slug uniqueness for lists**
    - **Validates: Requirements 1.6**
  
  - [ ]* 4.7 Write unit tests for edge cases
    - Test empty list retrieval
    - Test reordering with invalid entry IDs
    - Test slug generation with special characters

- [x] 5. Implement service layer for blog posts
  - [x] 5.1 Create BlogPostService
    - Implement createPost, updatePost, deletePost methods
    - Implement getPostBySlug
    - Implement searchPosts with pagination, search, tag filters, and status filtering
    - Implement publishPost to set status and timestamp
    - _Requirements: 3.1, 3.2, 3.3, 3.4, 3.5, 3.6, 7.3, 7.4_
  
  - [ ]* 5.2 Write property test for post data persistence
    - **Property 7: Blog post data persistence**
    - **Validates: Requirements 3.1**
  
  - [ ]* 5.3 Write property test for post status management
    - **Property 8: Blog post status management**
    - **Validates: Requirements 3.2**
  
  - [ ]* 5.4 Write property test for post slug uniqueness
    - **Property 9: Slug uniqueness for posts**
    - **Validates: Requirements 3.4**
  
  - [ ]* 5.5 Write property test for published post filtering
    - **Property 18: Published post filtering**
    - **Validates: Requirements 3.5**
  
  - [ ]* 5.6 Write property test for admin post access
    - **Property 19: Admin post access**
    - **Validates: Requirements 3.6**

- [x] 6. Implement media service and file storage
  - [x] 6.1 Create MediaService
    - Implement uploadMedia to save file to disk and create database record
    - Implement getMediaById and getMediaFile
    - Implement deleteMedia to remove file and database record
    - Configure MEDIA_ROOT from environment variable with defaults (./local-media or /mnt/media)
    - _Requirements: 5.1, 5.2, 5.7, 12.4, 13.2_
  
  - [ ]* 6.2 Write property test for media file storage
    - **Property 13: Media file storage**
    - **Validates: Requirements 5.1**
  
  - [ ]* 6.3 Write property test for media metadata persistence
    - **Property 14: Media metadata persistence**
    - **Validates: Requirements 5.2**
  
  - [ ]* 6.4 Write property test for media asset relationships
    - **Property 16: Media asset relationships**
    - **Validates: Requirements 5.4, 5.5, 5.6**
  
  - [ ]* 6.5 Write unit tests for file upload edge cases
    - Test invalid file types
    - Test oversized files
    - Test file storage failures


- [x] 7. Implement suggestion service
  - [x] 7.1 Create SuggestionService
    - Implement createSuggestion with initial status NEW
    - Implement getAllSuggestions for admin
    - Implement updateSuggestionStatus with validation
    - _Requirements: 4.1, 4.2, 4.3, 4.4_
  
  - [ ]* 7.2 Write property test for suggestion data persistence
    - **Property 10: Suggestion data persistence**
    - **Validates: Requirements 4.1**
  
  - [ ]* 7.3 Write property test for suggestion initial status
    - **Property 11: Suggestion initial status**
    - **Validates: Requirements 4.2**
  
  - [ ]* 7.4 Write property test for suggestion status validation
    - **Property 12: Suggestion status validation**
    - **Validates: Requirements 4.4**
  
  - [ ]* 7.5 Write property test for suggestion retrieval
    - **Property 20: Suggestion retrieval**
    - **Validates: Requirements 4.3**

- [x] 8. Implement Spring Security configuration
  - [x] 8.1 Create SecurityConfig
    - Configure HTTP Basic or JWT authentication
    - Load admin credentials from environment variables (ADMIN_USERNAME, ADMIN_PASSWORD)
    - Protect /api/admin/* endpoints with authentication
    - Allow public access to /api/lists, /api/posts, /api/suggestions, /api/media
    - Configure CORS for frontend development
    - _Requirements: 6.1, 6.2, 6.3, 6.4, 6.5, 6.6_
  
  - [ ]* 8.2 Write property test for unauthenticated access denial
    - **Property 28: Unauthenticated admin access denial**
    - **Validates: Requirements 6.2**
  
  - [ ]* 8.3 Write property test for admin endpoint protection
    - **Property 30: Admin endpoint protection**
    - **Validates: Requirements 6.6**
  
  - [ ]* 8.4 Write unit tests for authentication flows
    - Test valid credentials return token
    - Test invalid credentials return 401
    - Test expired token returns 401

- [x] 9. Checkpoint - Ensure backend services and security work
  - Run all backend tests to verify service layer and security
  - Manually test database migrations and entity relationships
  - Ensure all tests pass, ask the user if questions arise

- [x] 10. Implement public API controllers
  - [x] 10.1 Create PublicListController
    - Implement GET /api/lists with search, tag, and pagination
    - Implement GET /api/lists/{slug} with entries
    - Implement GET /api/lists/{slug}/entries
    - Add DTOs: RankedListSummaryDTO, RankedListDetailDTO, RankedEntryDTO
    - _Requirements: 9.1, 9.2, 9.3_
  
  - [x] 10.2 Create PublicPostController
    - Implement GET /api/posts with search, tag, and pagination
    - Implement GET /api/posts/{slug}
    - Add DTOs: BlogPostSummaryDTO, BlogPostDetailDTO
    - _Requirements: 9.4, 9.5_
  
  - [x] 10.3 Create SuggestionController
    - Implement POST /api/suggestions
    - Add DTO: CreateSuggestionRequest, SuggestionDTO
    - Add validation annotations
    - _Requirements: 9.6_
  
  - [x] 10.4 Create MediaController
    - Implement GET /api/media/{id} to serve files
    - Add Cache-Control and ETag headers
    - Stream file content with appropriate content type
    - _Requirements: 9.7, 15.1, 15.2_
  
  - [ ]* 10.5 Write property test for list retrieval API contract
    - **Property 31: List retrieval API contract**
    - **Validates: Requirements 9.1**
  
  - [ ]* 10.6 Write property test for list detail API contract
    - **Property 32: List detail API contract**
    - **Validates: Requirements 9.2, 9.3**
  
  - [ ]* 10.7 Write property test for post retrieval API contract
    - **Property 33: Post retrieval API contract**
    - **Validates: Requirements 9.4**
  
  - [ ]* 10.8 Write property test for post detail API contract
    - **Property 34: Post detail API contract**
    - **Validates: Requirements 9.5**
  
  - [ ]* 10.9 Write property test for entry ordering
    - **Property 17: Entry ordering by rank**
    - **Validates: Requirements 2.1, 2.7**
  
  - [ ]* 10.10 Write property test for media caching headers
    - **Property 15: Media caching headers**
    - **Validates: Requirements 5.3, 15.1, 15.2**


- [x] 11. Implement admin API controllers
  - [x] 11.1 Create AdminListController
    - Implement POST /api/admin/lists to create list
    - Implement PUT /api/admin/lists/{id} to update list
    - Implement DELETE /api/admin/lists/{id} to delete list
    - Implement POST /api/admin/lists/{id}/entries to add entry
    - Implement PUT /api/admin/lists/{id}/entries/reorder to bulk update ranks
    - Add request DTOs: CreateListRequest, UpdateListRequest, CreateEntryRequest, EntryRankUpdate
    - Add @PreAuthorize("hasRole('ADMIN')") annotations
    - _Requirements: 9.8, 9.9_
  
  - [x] 11.2 Create AdminPostController
    - Implement POST /api/admin/posts to create post
    - Implement PUT /api/admin/posts/{id} to update post
    - Implement DELETE /api/admin/posts/{id} to delete post
    - Add request DTOs: CreatePostRequest, UpdatePostRequest
    - Add @PreAuthorize("hasRole('ADMIN')") annotations
    - _Requirements: 9.8_
  
  - [x] 11.3 Create AdminMediaController
    - Implement POST /api/admin/media to upload file
    - Implement DELETE /api/admin/media/{id} to delete media
    - Add @PreAuthorize("hasRole('ADMIN')") annotations
    - _Requirements: 9.8_
  
  - [x] 11.4 Create AdminSuggestionController
    - Implement GET /api/admin/suggestions to retrieve all
    - Implement PUT /api/admin/suggestions/{id} to update status
    - Add request DTO: UpdateSuggestionStatusRequest
    - Add @PreAuthorize("hasRole('ADMIN')") annotations
    - _Requirements: 9.10, 9.11_
  
  - [ ]* 11.5 Write property test for suggestion creation API
    - **Property 35: Suggestion creation API contract**
    - **Validates: Requirements 9.6**
  
  - [ ]* 11.6 Write property test for media serving API
    - **Property 36: Media serving API contract**
    - **Validates: Requirements 9.7**

- [x] 12. Implement global exception handling
  - [x] 12.1 Create @ControllerAdvice for exception handling
    - Handle validation errors (MethodArgumentNotValidException) → HTTP 400
    - Handle not found errors (EntityNotFoundException) → HTTP 404
    - Handle constraint violations (DataIntegrityViolationException) → HTTP 409
    - Handle authentication errors (AuthenticationException) → HTTP 401
    - Handle generic exceptions → HTTP 500 with logging
    - Return structured error responses with field-level details
    - _Requirements: 14.1, 14.2, 14.3_
  
  - [ ]* 12.2 Write property test for invalid data rejection
    - **Property 38: Invalid data rejection**
    - **Validates: Requirements 14.1**
  
  - [ ]* 12.3 Write property test for not found error handling
    - **Property 39: Not found error handling**
    - **Validates: Requirements 14.2**
  
  - [ ]* 12.4 Write property test for HTTP status code correctness
    - **Property 37: HTTP status code correctness**
    - **Validates: Requirements 9.12**

- [x] 13. Implement validation logic
  - [x] 13.1 Add Bean Validation annotations to request DTOs
    - Add @NotBlank, @NotNull, @Positive, @Email annotations
    - Create custom validator for image file types
    - Create custom validator for file size limits
    - _Requirements: 14.4, 14.5, 14.6, 14.7_
  
  - [ ]* 13.2 Write property test for positive rank validation
    - **Property 40: Positive rank validation**
    - **Validates: Requirements 14.4**
  
  - [ ]* 13.3 Write property test for required field validation
    - **Property 41: Required field validation**
    - **Validates: Requirements 14.5**
  
  - [ ]* 13.4 Write property test for image format validation
    - **Property 42: Image format validation**
    - **Validates: Requirements 14.6**
  
  - [ ]* 13.5 Write property test for file size validation
    - **Property 43: File size validation**
    - **Validates: Requirements 14.7**

- [x] 14. Implement search and filtering properties
  - [ ]* 14.1 Write property test for list search functionality
    - **Property 21: List search functionality**
    - **Validates: Requirements 7.1**
  
  - [ ]* 14.2 Write property test for list tag filtering
    - **Property 22: List tag filtering**
    - **Validates: Requirements 7.2**
  
  - [ ]* 14.3 Write property test for post search functionality
    - **Property 23: Post search functionality**
    - **Validates: Requirements 7.3**
  
  - [ ]* 14.4 Write property test for post tag filtering
    - **Property 24: Post tag filtering**
    - **Validates: Requirements 7.4**
  
  - [ ]* 14.5 Write property test for pagination metadata
    - **Property 25: Pagination metadata**
    - **Validates: Requirements 7.5**

- [x] 15. Implement relationship properties
  - [ ]* 15.1 Write property test for many-to-many list-tag relationship
    - **Property 26: Many-to-many list-tag relationship**
    - **Validates: Requirements 8.2**
  
  - [ ]* 15.2 Write property test for many-to-many post-tag relationship
    - **Property 27: Many-to-many post-tag relationship**
    - **Validates: Requirements 8.3**

- [x] 16. Checkpoint - Ensure all backend tests pass
  - Run complete backend test suite including all property tests
  - Verify API endpoints work with Postman or curl
  - Test authentication and authorization flows
  - Ensure all tests pass, ask the user if questions arise


- [x] 17. Set up frontend project structure
  - [x] 17.1 Configure React Router
    - Set up routes for public pages: /, /lists, /lists/:slug, /posts, /posts/:slug, /suggest
    - Set up routes for admin pages: /admin/login, /admin/dashboard
    - Create protected route wrapper for admin pages
    - _Requirements: 10.1, 10.2, 10.3, 10.5, 11.1_
  
  - [x] 17.2 Create API client service
    - Create axios instance with base URL configuration
    - Create API methods for all public endpoints
    - Create API methods for all admin endpoints with auth headers
    - Add error handling and response interceptors
    - _Requirements: 9.1-9.12_
  
  - [x] 17.3 Set up TailwindCSS and global styles
    - Configure Tailwind with custom theme colors
    - Create global CSS for typography and layout
    - Set up responsive breakpoints
    - _Requirements: 10.8_

- [x] 18. Implement shared frontend components
  - [x] 18.1 Create TagBadge component
    - Display tag name with styling
    - Make clickable to filter by tag
    - _Requirements: 7.2, 7.4_
  
  - [x] 18.2 Create MarkdownRenderer component
    - Use react-markdown library
    - Add syntax highlighting with react-syntax-highlighter
    - Sanitize HTML output
    - _Requirements: 10.6_
  
  - [x] 18.3 Create ImageUploader component
    - File input with drag-and-drop support
    - Image preview
    - Alt text input field
    - Upload progress indicator
    - _Requirements: 11.7_
  
  - [x] 18.4 Create Pagination component
    - Page number buttons
    - Previous/Next navigation
    - Display total count and current page
    - _Requirements: 7.5_
  
  - [ ]* 18.5 Write property test for markdown rendering
    - **Property 45: Markdown rendering**
    - **Validates: Requirements 10.6**

- [x] 19. Implement public pages - Home and Lists
  - [x] 19.1 Create HomePage component
    - Fetch and display featured lists (3 most recent)
    - Fetch and display latest blog posts (3 most recent)
    - Display tag navigation
    - Add search bar
    - _Requirements: 10.1_
  
  - [x] 19.2 Create ListsIndexPage component
    - Fetch paginated lists with search and tag filters
    - Display list cards with title, subtitle, cover image, tags
    - Add search input and tag filter dropdowns
    - Integrate Pagination component
    - _Requirements: 10.2_
  
  - [x] 19.3 Create ListDetailPage component
    - Fetch list with entries by slug
    - Implement reveal mode state management
    - Implement show-all mode toggle
    - Display sticky progress indicator
    - Support deep linking via hash fragments (#rank-7)
    - Render entry cards with rank, title, hero image, blurb, commentary, fun fact, external link
    - _Requirements: 2.1, 2.2, 2.3, 2.4, 2.5, 2.6, 10.3, 10.4_
  
  - [ ]* 19.4 Write component test for reveal mode
    - Test initial state shows only first entry
    - Test reveal next button shows next entry
    - Test toggle to show-all mode displays all entries
  
  - [ ]* 19.5 Write property test for reveal mode progress tracking
    - **Property 44: Reveal mode progress tracking**
    - **Validates: Requirements 2.5**

- [ ] 20. Implement public pages - Posts and Suggestions
  - [ ] 20.1 Create PostsIndexPage component
    - Fetch paginated posts with search and tag filters
    - Display post cards with title, excerpt, cover image, publication date
    - Add search input and tag filter dropdowns
    - Integrate Pagination component
    - _Requirements: 10.5_
  
  - [ ] 20.2 Create PostDetailPage component
    - Fetch post by slug
    - Display cover image, title, publication date, tags
    - Render markdown body with MarkdownRenderer
    - _Requirements: 10.6_
  
  - [ ] 20.3 Create SuggestListPage component
    - Form with fields: title, description, category, example entries, name, email
    - Client-side validation
    - Submit to POST /api/suggestions
    - Display success message after submission
    - _Requirements: 4.5, 10.7_
  
  - [ ]* 20.4 Write component tests for suggestion form
    - Test form validation
    - Test successful submission
    - Test error handling


- [ ] 21. Implement admin authentication
  - [ ] 21.1 Create AdminLoginPage component
    - Login form with username and password fields
    - Submit credentials to backend
    - Store auth token in localStorage or sessionStorage
    - Redirect to admin dashboard on success
    - Display error message on failure
    - _Requirements: 6.3, 11.2_
  
  - [ ] 21.2 Create AuthContext for state management
    - Provide authentication state to all components
    - Implement login, logout, and token validation functions
    - Check token validity on app load
    - _Requirements: 6.4, 6.5_
  
  - [ ] 21.3 Create ProtectedRoute component
    - Wrap admin routes to require authentication
    - Redirect to login page if not authenticated
    - _Requirements: 6.2, 6.6_

- [ ] 22. Implement admin dashboard - Lists management
  - [ ] 22.1 Create AdminLayout component
    - Navigation tabs: Lists, Posts, Suggestions, Media
    - Logout button
    - _Requirements: 11.1_
  
  - [ ] 22.2 Create AdminListsTab component
    - Fetch and display all ranked lists in a table
    - Add create new list button
    - Add edit/delete actions per row
    - _Requirements: 11.3_
  
  - [ ] 22.3 Create ListEditor component
    - Form for list metadata (title, subtitle, intro, outro, tags, cover image)
    - Entry management section with add/edit/delete
    - Drag-and-drop reordering interface for entries
    - Bulk save button for rank updates
    - _Requirements: 11.4_
  
  - [ ] 22.4 Create EntryEditor modal component
    - Form for entry fields (rank, title, blurb, commentary, fun fact, external link, hero image)
    - Validation for required fields
    - _Requirements: 11.4_
  
  - [ ]* 22.5 Write component tests for list editor
    - Test creating a new list
    - Test adding entries
    - Test reordering entries
    - Test validation errors

- [ ] 23. Implement admin dashboard - Posts and Suggestions
  - [ ] 23.1 Create AdminPostsTab component
    - Fetch and display all blog posts in a table with status badges
    - Add create new post button
    - Add edit/delete actions per row
    - _Requirements: 11.5_
  
  - [ ] 23.2 Create PostEditor component
    - Form for post metadata (title, excerpt, tags, cover image, status)
    - Markdown editor with preview pane (use react-markdown-editor-lite or similar)
    - Save draft / Publish buttons
    - _Requirements: 11.5_
  
  - [ ] 23.3 Create AdminSuggestionsTab component
    - Fetch and display all suggestions in a table
    - Add status filter dropdown
    - Display title, description, submitter info, created date
    - Add status update dropdown per row (NEW, REVIEWING, ACCEPTED, DECLINED)
    - _Requirements: 11.6_
  
  - [ ]* 23.4 Write component tests for post editor
    - Test creating a draft post
    - Test publishing a post
    - Test markdown preview

- [ ] 24. Implement admin dashboard - Media management
  - [ ] 24.1 Create AdminMediaTab component
    - Fetch and display all media assets in a grid with thumbnails
    - Add upload button with ImageUploader component
    - Display filename, size, upload date per media item
    - Add delete action per media item
    - _Requirements: 11.7_
  
  - [ ]* 24.2 Write component tests for media upload
    - Test file selection and preview
    - Test successful upload
    - Test validation errors (invalid type, oversized file)

- [ ] 25. Implement error handling and loading states
  - [ ] 25.1 Create ErrorBoundary component
    - Catch React errors and display fallback UI
    - Log errors to console
    - _Requirements: 14.8_
  
  - [ ] 25.2 Add loading spinners to all async operations
    - Show spinner during API calls
    - Disable form inputs during submission
    - _Requirements: 15.6_
  
  - [ ] 25.3 Add error toast notifications
    - Display user-friendly error messages
    - Show validation errors inline on forms
    - Add retry button for network errors
    - _Requirements: 14.8_

- [ ] 26. Checkpoint - Ensure frontend works end-to-end
  - Test all public pages (home, lists, posts, suggest)
  - Test admin login and dashboard functionality
  - Test creating, editing, and deleting content
  - Test image uploads and display
  - Verify responsive design on mobile
  - Ensure all tests pass, ask the user if questions arise


- [ ] 27. Configure deployment for Railway
  - [ ] 27.1 Create Railway configuration files
    - Create railway.toml or use Railway.json for build/start commands
    - Configure backend to read PORT from environment
    - Configure backend to use /mnt/media for MEDIA_ROOT
    - Set environment variables: DATABASE_URL, ADMIN_USERNAME, ADMIN_PASSWORD, PORT, MEDIA_ROOT
    - _Requirements: 13.1, 13.2, 13.3_
  
  - [ ] 27.2 Configure frontend build for production
    - Update Vite config to build static assets
    - Configure backend to serve frontend static files (or deploy frontend separately)
    - Set API base URL for production
    - _Requirements: 13.5_
  
  - [ ] 27.3 Create deployment documentation
    - Document Railway setup steps
    - Document environment variables needed
    - Document volume mounting for /mnt/media
    - Document database plugin setup
    - _Requirements: 13.4_

- [ ] 28. Integration testing and final polish
  - [ ]* 28.1 Write integration tests for complete user flows
    - Test: Browse lists → view list detail → reveal entries
    - Test: Submit suggestion → verify in admin dashboard
    - Test: Admin login → create list → add entries → publish → view on public site
    - _Requirements: All_
  
  - [ ]* 28.2 Run all property tests with increased iterations
    - Run backend property tests with 500 iterations
    - Run frontend property tests with 500 iterations
    - Verify all tests pass
  
  - [ ]* 28.3 Perform manual testing checklist
    - Test admin login works
    - Test creating and publishing a ranked list
    - Test reveal mode works correctly
    - Test deep linking to specific ranks
    - Test image upload and display
    - Test suggestion form submission
    - Test search and tag filtering
    - Test mobile responsive design
    - Test Railway deployment with volume storage

- [ ] 29. Final checkpoint - Production readiness
  - Verify all tests pass (unit, property, integration, component)
  - Verify application runs locally with docker-compose
  - Verify application deploys successfully to Railway
  - Verify all acceptance criteria are met
  - Ensure all tests pass, ask the user if questions arise

## Notes

- Each task references specific requirements for traceability
- Checkpoints ensure incremental validation at key milestones
- Property tests validate universal correctness properties with 100+ iterations
- Unit tests validate specific examples and edge cases
- Backend uses JUnit QuickCheck for property-based testing
- Frontend uses fast-check for property-based testing
- Integration tests verify end-to-end flows
