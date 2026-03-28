-- Wanderlust Database Schema
-- PostgreSQL 16 + PostGIS 3.4

-- Enable extensions
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE EXTENSION IF NOT EXISTS "postgis";

-- ============================================================
-- ENUM TYPES
-- ============================================================

CREATE TYPE travel_style AS ENUM (
    'BACKPACKER', 'LUXURY', 'FAMILY', 'SOLO', 'ADVENTURE', 'DIGITAL_NOMAD'
);

CREATE TYPE post_category AS ENUM (
    'SPOT', 'FOOD', 'STAY', 'ACTIVITY', 'TIP', 'WARNING'
);

CREATE TYPE media_type AS ENUM (
    'IMAGE', 'VIDEO'
);

-- ============================================================
-- USERS
-- ============================================================

CREATE TABLE users (
    id              UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    keycloak_id     VARCHAR(255) NOT NULL UNIQUE,
    username        VARCHAR(50) NOT NULL UNIQUE,
    display_name    VARCHAR(100) NOT NULL,
    bio             TEXT,
    avatar_url      VARCHAR(500),
    travel_style    travel_style,
    countries_visited_count INTEGER NOT NULL DEFAULT 0,
    current_location GEOGRAPHY(POINT, 4326),
    created_at      TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_users_keycloak_id ON users (keycloak_id);
CREATE INDEX idx_users_username ON users (username);
CREATE INDEX idx_users_current_location ON users USING GIST (current_location);

-- ============================================================
-- FOLLOWS
-- ============================================================

CREATE TABLE follows (
    follower_id     UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    following_id    UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    created_at      TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    PRIMARY KEY (follower_id, following_id),
    CHECK (follower_id != following_id)
);

CREATE INDEX idx_follows_following ON follows (following_id);

-- ============================================================
-- USER WISHLIST DESTINATIONS
-- ============================================================

CREATE TABLE user_wishlist_destinations (
    id              UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id         UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    country_code    VARCHAR(3) NOT NULL,
    place_name      VARCHAR(255) NOT NULL,
    location        GEOGRAPHY(POINT, 4326),
    created_at      TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_wishlist_user ON user_wishlist_destinations (user_id);
CREATE INDEX idx_wishlist_location ON user_wishlist_destinations USING GIST (location);

-- ============================================================
-- TRAVEL POSTS
-- ============================================================

CREATE TABLE travel_posts (
    id                      UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id                 UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    content                 TEXT,
    category                post_category NOT NULL,
    cost_level              SMALLINT CHECK (cost_level BETWEEN 1 AND 5),
    best_season             VARCHAR(20),
    duration_suggested      VARCHAR(50),
    accessibility_rating    SMALLINT CHECK (accessibility_rating BETWEEN 1 AND 5),
    location                GEOGRAPHY(POINT, 4326) NOT NULL,
    place_name              VARCHAR(255) NOT NULL,
    country_code            VARCHAR(3) NOT NULL,
    tags                    TEXT[] DEFAULT '{}',
    likes_count             INTEGER NOT NULL DEFAULT 0,
    comments_count          INTEGER NOT NULL DEFAULT 0,
    created_at              TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at              TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_posts_user ON travel_posts (user_id);
CREATE INDEX idx_posts_location ON travel_posts USING GIST (location);
CREATE INDEX idx_posts_country ON travel_posts (country_code);
CREATE INDEX idx_posts_category ON travel_posts (category);
CREATE INDEX idx_posts_created ON travel_posts (created_at DESC);
CREATE INDEX idx_posts_tags ON travel_posts USING GIN (tags);
CREATE INDEX idx_posts_trending ON travel_posts (likes_count DESC, created_at DESC);

-- Full-text search index on content and place_name
CREATE INDEX idx_posts_fts ON travel_posts USING GIN (
    to_tsvector('english', COALESCE(content, '') || ' ' || place_name)
);

-- ============================================================
-- POST MEDIA
-- ============================================================

CREATE TABLE post_media (
    id              UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    post_id         UUID NOT NULL REFERENCES travel_posts(id) ON DELETE CASCADE,
    media_url       VARCHAR(500) NOT NULL,
    media_type      media_type NOT NULL DEFAULT 'IMAGE',
    display_order   INTEGER NOT NULL DEFAULT 0,
    width           INTEGER,
    height          INTEGER,
    created_at      TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_media_post ON post_media (post_id, display_order);

-- ============================================================
-- LIKES
-- ============================================================

CREATE TABLE likes (
    id              UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id         UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    post_id         UUID NOT NULL REFERENCES travel_posts(id) ON DELETE CASCADE,
    created_at      TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    UNIQUE (user_id, post_id)
);

CREATE INDEX idx_likes_post ON likes (post_id);
CREATE INDEX idx_likes_user ON likes (user_id);

-- ============================================================
-- COMMENTS
-- ============================================================

CREATE TABLE comments (
    id              UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    post_id         UUID NOT NULL REFERENCES travel_posts(id) ON DELETE CASCADE,
    user_id         UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    parent_id       UUID REFERENCES comments(id) ON DELETE CASCADE,
    content         TEXT NOT NULL,
    is_question     BOOLEAN NOT NULL DEFAULT FALSE,
    is_answer       BOOLEAN NOT NULL DEFAULT FALSE,
    created_at      TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_comments_post ON comments (post_id, created_at DESC);
CREATE INDEX idx_comments_parent ON comments (parent_id);
CREATE INDEX idx_comments_questions ON comments (post_id) WHERE is_question = TRUE;

-- ============================================================
-- BOOKMARKS
-- ============================================================

CREATE TABLE bookmarks (
    user_id         UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    post_id         UUID NOT NULL REFERENCES travel_posts(id) ON DELETE CASCADE,
    created_at      TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    PRIMARY KEY (user_id, post_id)
);

CREATE INDEX idx_bookmarks_user ON bookmarks (user_id, created_at DESC);

-- ============================================================
-- ITINERARIES
-- ============================================================

CREATE TABLE itineraries (
    id                  UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id             UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    title               VARCHAR(200) NOT NULL,
    description         TEXT,
    country_codes       TEXT[] DEFAULT '{}',
    duration_days       INTEGER NOT NULL,
    estimated_budget_usd DECIMAL(10, 2),
    cover_image_url     VARCHAR(500),
    is_published        BOOLEAN NOT NULL DEFAULT FALSE,
    cloned_from         UUID REFERENCES itineraries(id) ON DELETE SET NULL,
    created_at          TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_itineraries_user ON itineraries (user_id, created_at DESC);
CREATE INDEX idx_itineraries_published ON itineraries (created_at DESC) WHERE is_published = TRUE;
CREATE INDEX idx_itineraries_countries ON itineraries USING GIN (country_codes);

-- ============================================================
-- ITINERARY DAYS
-- ============================================================

CREATE TABLE itinerary_days (
    id              UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    itinerary_id    UUID NOT NULL REFERENCES itineraries(id) ON DELETE CASCADE,
    day_number      INTEGER NOT NULL,
    title           VARCHAR(200),
    notes           TEXT,
    UNIQUE (itinerary_id, day_number)
);

CREATE INDEX idx_itinerary_days ON itinerary_days (itinerary_id, day_number);

-- ============================================================
-- ITINERARY ITEMS
-- ============================================================

CREATE TABLE itinerary_items (
    id                  UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    day_id              UUID NOT NULL REFERENCES itinerary_days(id) ON DELETE CASCADE,
    post_id             UUID REFERENCES travel_posts(id) ON DELETE SET NULL,
    custom_title        VARCHAR(200),
    custom_note         TEXT,
    transport_to_next   VARCHAR(100),
    display_order       INTEGER NOT NULL DEFAULT 0
);

CREATE INDEX idx_itinerary_items_day ON itinerary_items (day_id, display_order);

-- ============================================================
-- FUNCTIONS: Update counters
-- ============================================================

-- Function to update likes_count on travel_posts
CREATE OR REPLACE FUNCTION update_post_likes_count()
RETURNS TRIGGER AS $$
BEGIN
    IF TG_OP = 'INSERT' THEN
        UPDATE travel_posts SET likes_count = likes_count + 1 WHERE id = NEW.post_id;
        RETURN NEW;
    ELSIF TG_OP = 'DELETE' THEN
        UPDATE travel_posts SET likes_count = likes_count - 1 WHERE id = OLD.post_id;
        RETURN OLD;
    END IF;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_likes_count
AFTER INSERT OR DELETE ON likes
FOR EACH ROW EXECUTE FUNCTION update_post_likes_count();

-- Function to update comments_count on travel_posts
CREATE OR REPLACE FUNCTION update_post_comments_count()
RETURNS TRIGGER AS $$
BEGIN
    IF TG_OP = 'INSERT' THEN
        UPDATE travel_posts SET comments_count = comments_count + 1 WHERE id = NEW.post_id;
        RETURN NEW;
    ELSIF TG_OP = 'DELETE' THEN
        UPDATE travel_posts SET comments_count = comments_count - 1 WHERE id = OLD.post_id;
        RETURN OLD;
    END IF;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_comments_count
AFTER INSERT OR DELETE ON comments
FOR EACH ROW EXECUTE FUNCTION update_post_comments_count();

-- Function to auto-update countries_visited_count
CREATE OR REPLACE FUNCTION update_user_countries_count()
RETURNS TRIGGER AS $$
BEGIN
    UPDATE users
    SET countries_visited_count = (
        SELECT COUNT(DISTINCT country_code)
        FROM travel_posts
        WHERE user_id = NEW.user_id
    )
    WHERE id = NEW.user_id;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_countries_count
AFTER INSERT OR DELETE ON travel_posts
FOR EACH ROW EXECUTE FUNCTION update_user_countries_count();

-- ============================================================
-- FUNCTION: updated_at auto-update
-- ============================================================

CREATE OR REPLACE FUNCTION update_updated_at()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = NOW();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_users_updated_at
    BEFORE UPDATE ON users FOR EACH ROW EXECUTE FUNCTION update_updated_at();
CREATE TRIGGER trg_posts_updated_at
    BEFORE UPDATE ON travel_posts FOR EACH ROW EXECUTE FUNCTION update_updated_at();
CREATE TRIGGER trg_itineraries_updated_at
    BEFORE UPDATE ON itineraries FOR EACH ROW EXECUTE FUNCTION update_updated_at();
