# Task 3.1: JPA Entities Implementation

## Overview
This document describes the implementation of JPA entities for the LayerTen application.

## Entities Implemented

### 1. Enums
- **PostStatus**: Enum for blog post status (DRAFT, PUBLISHED)
- **SuggestionStatus**: Enum for suggestion status (NEW, REVIEWING, ACCEPTED, DECLINED)

### 2. Core Entities

#### Author
- Represents content authors
- Fields: id, name, email, bio, createdAt
- Auto-generates createdAt timestamp on persist

#### Tag
- Represents categorization tags
- Fields: id, name, slug
- Unique constraints on name and slug

#### MediaAsset
- Represents uploaded media files (images)
- Fields: id, filename, contentType, fileSize, altText, storagePath, createdAt
- Auto-generates createdAt timestamp on persist

#### RankedList
- Represents countdown-style ranked lists
- Fields: id, title, subtitle, slug, intro, outro, coverImage, tags, entries, publishedAt, createdAt, updatedAt
- Relationships:
  - ManyToOne with MediaAsset (coverImage)
  - ManyToMany with Tag
  - OneToMany with RankedEntry (cascade delete, orphan removal)
- Auto-generates createdAt and updatedAt timestamps
- Helper methods for managing bidirectional relationships

#### RankedEntry
- Represents individual entries within a ranked list
- Fields: id, rankedList, rank, title, blurb, commentary, funFact, externalLink, heroImage
- Relationships:
  - ManyToOne with RankedList
  - ManyToOne with MediaAsset (heroImage)
- Unique constraint on (rankedList, rank) combination

#### BlogPost
- Represents blog articles
- Fields: id, title, slug, excerpt, body, coverImage, tags, status, publishedAt, createdAt, updatedAt
- Relationships:
  - ManyToOne with MediaAsset (coverImage)
  - ManyToMany with Tag
- Default status is DRAFT
- Auto-generates createdAt and updatedAt timestamps
- Helper methods for managing bidirectional relationships

#### Suggestion
- Represents visitor-submitted suggestions
- Fields: id, title, description, category, exampleEntries, submitterName, submitterEmail, status, createdAt
- Default status is NEW
- Auto-generates createdAt timestamp on persist

## Key Design Decisions

### 1. Lazy Fetching
All relationships use `FetchType.LAZY` to avoid N+1 query problems and improve performance.

### 2. Cascade Operations
- RankedList → RankedEntry: CASCADE ALL with orphan removal
- This ensures entries are deleted when their parent list is deleted

### 3. Bidirectional Relationships
Helper methods (addEntry, removeEntry, addTag, removeTag) maintain both sides of bidirectional relationships.

### 4. Timestamp Management
- @PrePersist: Sets createdAt on entity creation
- @PreUpdate: Updates updatedAt on entity modification

### 5. Unique Constraints
- Tag: name and slug must be unique
- RankedList: slug must be unique
- BlogPost: slug must be unique
- RankedEntry: (rankedList, rank) combination must be unique

## Database Schema Alignment
All entities align with the Flyway migration V1__Create_core_tables.sql:
- Table names match exactly
- Column names match (using @Column annotations where needed)
- Constraints match (unique, nullable, foreign keys)
- Join tables match for many-to-many relationships

## Testing
Created EntityMappingTest.java with:
- Context loading test to verify Spring Boot can load all entities
- Basic entity instantiation tests
- Relationship tests for bidirectional associations
- Enum value tests

## Requirements Validated
This implementation satisfies:
- Requirement 8.1: Define entities for Author, Tag, Blog_Post, Ranked_List, Ranked_Entry, Media_Asset, and Suggestion
- Requirement 8.2: Many-to-many relationship between Ranked_List and Tags
- Requirement 8.3: Many-to-many relationship between Blog_Post and Tags
- Requirement 8.4: One-to-many relationship between Ranked_List and Ranked_Entries with cascade delete

## Next Steps
Task 3.2 will implement Spring Data JPA repositories for these entities with custom query methods.
