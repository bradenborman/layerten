# Requirements Document: LayerTen

## Introduction

LayerTen is a modern content platform focused on countdown/Top-10 style ranked lists and blog posts. The platform enables content creators to publish engaging ranked lists with a reveal-style user experience, alongside traditional blog posts. Visitors can browse content and submit suggestions for future lists. The system is designed as a monorepo web application with a Spring Boot backend and React frontend, deployable to Railway with local development support.

## Glossary

- **System**: The LayerTen web application (backend and frontend combined)
- **Backend**: The Spring Boot REST API server
- **Frontend**: The React + TypeScript client application
- **Admin**: The single authenticated user who manages content
- **Visitor**: Any unauthenticated user browsing the public site
- **Ranked_List**: A countdown-style list with N entries, each having an explicit numeric rank
- **Ranked_Entry**: A single item within a Ranked_List with a specific rank position
- **Blog_Post**: A traditional blog article with markdown content
- **Suggestion**: A visitor-submitted idea for a future ranked list
- **Media_Asset**: An uploaded image file with metadata stored in the database
- **Tag**: A categorization label that can be applied to Ranked_Lists and Blog_Posts
- **Reveal_Mode**: The interactive UI experience where entries are shown one at a time
- **Show_All_Mode**: The UI mode where all entries in a list are displayed simultaneously

## Requirements

### Requirement 1: Ranked List Management

**User Story:** As an admin, I want to create and manage ranked lists with multiple entries, so that I can publish engaging countdown-style content.

#### Acceptance Criteria

1. WHEN an admin creates a Ranked_List, THE Backend SHALL store the title, optional subtitle, tags, intro text, and outro text
2. WHEN an admin adds a Ranked_Entry to a Ranked_List, THE Backend SHALL store the rank number, title, hero image reference, short blurb, commentary (markdown), optional fun fact, and optional external link
3. WHEN an admin assigns a rank to a Ranked_Entry, THE Backend SHALL ensure the rank is unique within that Ranked_List
4. WHEN an admin reorders entries in a Ranked_List, THE Backend SHALL update all affected rank numbers atomically
5. WHEN an admin deletes a Ranked_List, THE Backend SHALL cascade delete all associated Ranked_Entries
6. THE Backend SHALL generate a unique slug for each Ranked_List based on its title
7. WHEN a Ranked_List is published, THE Backend SHALL record the publication timestamp

### Requirement 2: Ranked List Public Display

**User Story:** As a visitor, I want to view ranked lists with an interactive reveal experience, so that I can enjoy countdown-style content.

#### Acceptance Criteria

1. WHEN a visitor requests a Ranked_List, THE Frontend SHALL display entries in descending rank order (highest rank first)
2. WHEN a visitor views a Ranked_List in Reveal_Mode, THE Frontend SHALL initially show only the first unrevealed entry
3. WHEN a visitor clicks "reveal next" in Reveal_Mode, THE Frontend SHALL display the next entry in rank order
4. WHEN a visitor toggles to Show_All_Mode, THE Frontend SHALL display all entries simultaneously
5. WHEN a visitor is viewing a Ranked_List, THE Frontend SHALL display a progress indicator showing how many entries have been revealed
6. WHEN a visitor navigates to a URL with a rank hash fragment (e.g., #rank-7), THE Frontend SHALL scroll to and highlight that specific entry
7. WHEN a visitor requests a Ranked_List by slug, THE Backend SHALL return the list metadata and all associated entries ordered by rank

### Requirement 3: Blog Post Management

**User Story:** As an admin, I want to create and manage blog posts, so that I can publish traditional article content alongside ranked lists.

#### Acceptance Criteria

1. WHEN an admin creates a Blog_Post, THE Backend SHALL store the title, slug, excerpt, body (markdown), tags, and optional cover image reference
2. WHEN an admin saves a Blog_Post, THE Backend SHALL allow setting the state to either published or draft
3. WHEN an admin publishes a Blog_Post, THE Backend SHALL record the publication timestamp
4. THE Backend SHALL generate a unique slug for each Blog_Post based on its title
5. WHEN a visitor requests published Blog_Posts, THE Backend SHALL return only posts with published state
6. WHEN an admin requests Blog_Posts, THE Backend SHALL return posts in all states

### Requirement 4: Visitor Suggestion System

**User Story:** As a visitor, I want to submit suggestions for future ranked lists, so that I can contribute ideas to the platform.

#### Acceptance Criteria

1. WHEN a visitor submits a Suggestion, THE Backend SHALL store the title, description, optional category/tag, optional example entries, and optional contact information
2. WHEN a Suggestion is created, THE Backend SHALL set its initial status to NEW
3. WHEN an admin views Suggestions, THE Backend SHALL return all suggestions with their current status
4. WHEN an admin updates a Suggestion status, THE Backend SHALL accept values: NEW, REVIEWING, ACCEPTED, or DECLINED
5. THE Frontend SHALL provide a public page or modal for visitors to submit suggestions without authentication

### Requirement 5: Media Asset Management

**User Story:** As an admin, I want to upload and manage images, so that I can attach visual content to lists and entries.

#### Acceptance Criteria

1. WHEN an admin uploads an image, THE Backend SHALL store the file to disk at the configured MEDIA_ROOT path
2. WHEN an image is uploaded, THE Backend SHALL store metadata including content type, filename, file size, creation timestamp, and optional alt text
3. WHEN a visitor requests a Media_Asset by ID, THE Backend SHALL serve the file with appropriate caching headers
4. WHEN a Ranked_List references a cover image, THE Backend SHALL store the Media_Asset ID relationship
5. WHEN a Ranked_Entry references a hero image, THE Backend SHALL store the Media_Asset ID relationship
6. WHEN a Blog_Post references a cover image, THE Backend SHALL store the Media_Asset ID relationship
7. THE Backend SHALL support storing media at /mnt/media for Railway deployment and ./local-media for local development

### Requirement 6: Authentication and Authorization

**User Story:** As the platform owner, I want a single admin user authenticated via environment variables, so that I can securely manage content.

#### Acceptance Criteria

1. WHEN the Backend starts, THE Backend SHALL load admin credentials from environment variables
2. WHEN a user attempts to access an admin endpoint without authentication, THE Backend SHALL return HTTP 401 Unauthorized
3. WHEN a user provides valid admin credentials, THE Backend SHALL create a session or JWT token
4. WHEN an authenticated admin makes a request, THE Backend SHALL validate the session or JWT token
5. WHEN a session or token expires, THE Backend SHALL require re-authentication
6. THE Backend SHALL protect all endpoints under /api/admin/* with authentication

### Requirement 7: Content Discovery and Filtering

**User Story:** As a visitor, I want to search and filter content by tags and keywords, so that I can find relevant lists and posts.

#### Acceptance Criteria

1. WHEN a visitor requests Ranked_Lists with a search parameter, THE Backend SHALL return lists where the title or intro contains the search term
2. WHEN a visitor requests Ranked_Lists with a tag filter, THE Backend SHALL return only lists tagged with that tag
3. WHEN a visitor requests Blog_Posts with a search parameter, THE Backend SHALL return posts where the title or excerpt contains the search term
4. WHEN a visitor requests Blog_Posts with a tag filter, THE Backend SHALL return only posts tagged with that tag
5. WHEN a visitor requests paginated content, THE Backend SHALL return results with page metadata including total count and page size
6. THE Frontend SHALL display tag navigation on the home page for easy content discovery

### Requirement 8: Database Schema and Relationships

**User Story:** As a system architect, I want a well-structured relational database schema, so that data integrity is maintained and queries are efficient.

#### Acceptance Criteria

1. THE Backend SHALL define entities for Author, Tag, Blog_Post, Ranked_List, Ranked_Entry, Media_Asset, and Suggestion
2. WHEN a Ranked_List is associated with Tags, THE Backend SHALL use a many-to-many relationship
3. WHEN a Blog_Post is associated with Tags, THE Backend SHALL use a many-to-many relationship
4. WHEN a Ranked_List contains Ranked_Entries, THE Backend SHALL use a one-to-many relationship with cascade delete
5. WHEN a Ranked_Entry references a Media_Asset, THE Backend SHALL use a many-to-one relationship
6. WHEN a Ranked_List references a cover Media_Asset, THE Backend SHALL use a many-to-one relationship
7. THE Backend SHALL enforce unique constraints on rank numbers within each Ranked_List
8. THE Backend SHALL use Flyway for database migrations to ensure schema versioning

### Requirement 9: API Design and REST Conventions

**User Story:** As a frontend developer, I want a well-designed REST API, so that I can easily integrate the client application.

#### Acceptance Criteria

1. THE Backend SHALL expose GET /api/lists to return paginated Ranked_Lists with search and tag filters
2. THE Backend SHALL expose GET /api/lists/{slug} to return a specific Ranked_List by slug
3. THE Backend SHALL expose GET /api/lists/{slug}/entries to return all Ranked_Entries for a list ordered by rank
4. THE Backend SHALL expose GET /api/posts to return paginated Blog_Posts with search and tag filters
5. THE Backend SHALL expose GET /api/posts/{slug} to return a specific Blog_Post by slug
6. THE Backend SHALL expose POST /api/suggestions to create a new Suggestion
7. THE Backend SHALL expose GET /api/media/{id} to serve a Media_Asset file
8. THE Backend SHALL expose CRUD endpoints under /api/admin/* for managing lists, entries, posts, tags, and media
9. THE Backend SHALL expose PUT /api/admin/lists/{id}/entries/reorder to bulk update entry ranks
10. THE Backend SHALL expose GET /api/admin/suggestions to retrieve all suggestions
11. THE Backend SHALL expose PUT /api/admin/suggestions/{id} to update a suggestion status
12. THE Backend SHALL return appropriate HTTP status codes (200, 201, 400, 401, 404, 500)

### Requirement 10: Frontend User Experience

**User Story:** As a visitor, I want a modern and playful interface, so that browsing content is enjoyable and intuitive.

#### Acceptance Criteria

1. WHEN a visitor loads the home page, THE Frontend SHALL display featured lists, latest lists, latest posts, and tag navigation
2. WHEN a visitor views the lists index, THE Frontend SHALL provide filters for search and tags
3. WHEN a visitor views a Ranked_List detail page, THE Frontend SHALL provide a countdown player with reveal mode and show-all mode toggle
4. WHEN a visitor is in Reveal_Mode, THE Frontend SHALL display a sticky progress indicator
5. WHEN a visitor views the posts index, THE Frontend SHALL display posts with excerpts and cover images
6. WHEN a visitor views a Blog_Post detail page, THE Frontend SHALL render markdown content with proper formatting
7. THE Frontend SHALL provide a suggest-a-list page or modal accessible from the navigation
8. THE Frontend SHALL use modern styling with TailwindCSS or CSS modules for a clean, playful aesthetic

### Requirement 11: Admin User Interface

**User Story:** As an admin, I want a dedicated admin interface, so that I can efficiently manage all content and settings.

#### Acceptance Criteria

1. WHEN an admin accesses /admin, THE Frontend SHALL display a login page if not authenticated
2. WHEN an admin logs in successfully, THE Frontend SHALL display a dashboard with tabs for Lists, Posts, Suggestions, and Media
3. WHEN an admin is on the Lists tab, THE Frontend SHALL provide forms to create, edit, and delete Ranked_Lists
4. WHEN an admin edits a Ranked_List, THE Frontend SHALL provide an interface to add, edit, delete, and reorder Ranked_Entries
5. WHEN an admin is on the Posts tab, THE Frontend SHALL provide forms to create, edit, and delete Blog_Posts with markdown editing
6. WHEN an admin is on the Suggestions tab, THE Frontend SHALL display all suggestions with status filters and update controls
7. WHEN an admin is on the Media tab, THE Frontend SHALL provide an upload interface and display all uploaded Media_Assets
8. WHEN an admin uploads an image, THE Frontend SHALL display a preview and allow setting alt text

### Requirement 12: Local Development Environment

**User Story:** As a developer, I want a simple local development setup, so that I can quickly start working on the application.

#### Acceptance Criteria

1. THE System SHALL provide a docker-compose.yml file in the repository root for running PostgreSQL locally
2. WHEN a developer runs docker-compose up, THE System SHALL start a PostgreSQL container with the correct database configuration
3. THE Backend SHALL read database connection details from environment variables (DATABASE_URL or individual DB_HOST, DB_PORT, DB_NAME, DB_USER, DB_PASSWORD)
4. THE Backend SHALL use ./local-media as the default MEDIA_ROOT for local development
5. THE Frontend SHALL proxy /api requests to the Backend during local development
6. THE System SHALL provide a root README.md with setup instructions for both backend and frontend

### Requirement 13: Railway Deployment

**User Story:** As a platform owner, I want to deploy the application to Railway, so that it is accessible to users on the internet.

#### Acceptance Criteria

1. THE Backend SHALL read DATABASE_URL from Railway's PostgreSQL plugin environment variable
2. THE Backend SHALL use /mnt/media as MEDIA_ROOT when deployed to Railway
3. THE Backend SHALL read the PORT environment variable provided by Railway
4. THE System SHALL configure Railway to mount a Volume at /mnt/media for persistent media storage
5. THE Frontend SHALL be built as static assets and served by the Backend or a separate Railway service
6. THE System SHALL require minimal environment variable changes between local and Railway deployments

### Requirement 14: Data Validation and Error Handling

**User Story:** As a system architect, I want robust validation and error handling, so that the application is reliable and provides clear feedback.

#### Acceptance Criteria

1. WHEN invalid data is submitted to any endpoint, THE Backend SHALL return HTTP 400 with validation error details
2. WHEN a requested resource does not exist, THE Backend SHALL return HTTP 404 with a clear error message
3. WHEN a server error occurs, THE Backend SHALL return HTTP 500 and log the error details
4. THE Backend SHALL validate that Ranked_Entry rank numbers are positive integers
5. THE Backend SHALL validate that required fields (title, slug, etc.) are not empty
6. THE Backend SHALL validate that uploaded files are valid image formats (JPEG, PNG, GIF, WebP)
7. THE Backend SHALL validate that file sizes do not exceed a configured maximum (e.g., 10MB)
8. WHEN validation fails on the Frontend, THE Frontend SHALL display user-friendly error messages

### Requirement 15: Performance and Caching

**User Story:** As a visitor, I want fast page loads and responsive interactions, so that my browsing experience is smooth.

#### Acceptance Criteria

1. WHEN serving Media_Assets, THE Backend SHALL include Cache-Control headers with appropriate max-age values
2. WHEN serving Media_Assets, THE Backend SHALL include ETag headers for conditional requests
3. THE Backend SHALL use database indexes on frequently queried fields (slug, tags, publication date)
4. THE Frontend SHALL implement lazy loading for images in lists and posts
5. THE Frontend SHALL cache API responses where appropriate to reduce redundant requests
6. WHEN a visitor navigates between pages, THE Frontend SHALL provide loading indicators for async operations
