-- LayerTen Database Schema Migration
-- Creates core tables for ranked lists, blog posts, media assets, tags, and suggestions

-- Author table
CREATE TABLE author (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255),
    bio TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

-- Tag table
CREATE TABLE tag (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    slug VARCHAR(100) NOT NULL UNIQUE
);

-- Media Asset table
CREATE TABLE media_asset (
    id BIGSERIAL PRIMARY KEY,
    filename VARCHAR(255) NOT NULL,
    content_type VARCHAR(100) NOT NULL,
    file_size BIGINT NOT NULL,
    alt_text VARCHAR(255),
    storage_path VARCHAR(500) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

-- Ranked List table
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

-- Ranked Entry table
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

-- Blog Post table
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

-- Suggestion table
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

-- Many-to-many join table: Ranked List <-> Tags
CREATE TABLE ranked_list_tags (
    ranked_list_id BIGINT NOT NULL REFERENCES ranked_list(id) ON DELETE CASCADE,
    tag_id BIGINT NOT NULL REFERENCES tag(id) ON DELETE CASCADE,
    PRIMARY KEY (ranked_list_id, tag_id)
);

-- Many-to-many join table: Blog Post <-> Tags
CREATE TABLE blog_post_tags (
    blog_post_id BIGINT NOT NULL REFERENCES blog_post(id) ON DELETE CASCADE,
    tag_id BIGINT NOT NULL REFERENCES tag(id) ON DELETE CASCADE,
    PRIMARY KEY (blog_post_id, tag_id)
);

-- Indexes for performance optimization
CREATE INDEX idx_ranked_list_slug ON ranked_list(slug);
CREATE INDEX idx_ranked_list_published_at ON ranked_list(published_at);
CREATE INDEX idx_ranked_entry_list_rank ON ranked_entry(ranked_list_id, rank);
CREATE INDEX idx_blog_post_slug ON blog_post(slug);
CREATE INDEX idx_blog_post_status ON blog_post(status);
CREATE INDEX idx_blog_post_published_at ON blog_post(published_at);
CREATE INDEX idx_suggestion_status ON suggestion(status);
CREATE INDEX idx_tag_slug ON tag(slug);
