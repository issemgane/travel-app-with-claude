# Wanderlust — Product Specification

## 1. Product Positioning

### What Makes This Different From Instagram

Instagram is a general-purpose photo-sharing platform. Travel content drowns in a feed of food pics, selfies, and memes. Wanderlust is purpose-built for travel:

| Dimension | Instagram | Wanderlust |
|-----------|-----------|------------|
| Content model | Flat photo + caption | Structured Travel Card (media + location + cost + season + category) |
| Discovery | Hashtags, Explore algorithm | Map-based, destination-first, filtered by budget/season/style |
| Planning utility | Zero | Clonable itineraries, bookmarkable trip plans |
| Location data | Optional tag | Required, PostGIS-powered, searchable |
| Content lifecycle | Ephemeral (stories) or static | Evergreen — a post about Kyoto is useful years later |

### Why Travelers Choose It

- **Before a trip**: Search a destination → find real itineraries with costs, timing, and honest tips — not just pretty photos.
- **During a trip**: Discover what's nearby, sorted by category (food, spots, activities).
- **After a trip**: Share structured experiences that actually help other travelers, not just flex.

### Core Value Proposition

> **"Turn every traveler's experience into someone else's perfect trip plan."**

---

## 2. Core MVP Features

### 2.1 Authentication

**Method**: JWT-based OIDC with Keycloak

| User Story | Acceptance Criteria |
|------------|-------------------|
| As a user, I can sign up with email or Google/Apple | Keycloak handles identity. JWT issued on login. |
| As a user, I complete a 30-second onboarding quiz | Travel style (backpacker/luxury/family/solo/adventure/digital-nomad) saved to profile. Dream destinations captured for feed personalization. |
| As a user, my session persists across browser refreshes | JWT stored securely. Refresh token rotation enabled. |

### 2.2 Traveler Profiles

Not a generic bio page — a **traveler identity card**.

**Fields**:
- Display name, username, avatar
- Bio (travel-focused placeholder: "Where I've been, where I'm going")
- Travel style badge (from onboarding)
- Countries visited count (auto-calculated from posts)
- "Currently in" location (optional, manually set)
- Trip wishlist (list of dream destinations)

| User Story | Acceptance Criteria |
|------------|-------------------|
| As a visitor, I see a traveler's profile with their travel stats | Profile page shows: posts count, followers, following, countries visited, itineraries created |
| As a user, I can set my travel style and wishlist | Editable from profile settings. Affects feed ranking. |
| As a user, I can follow other travelers | Follow/unfollow with follower/following counts |

### 2.3 Travel Posts (Structured Travel Cards)

This is the **core differentiator**. Every post is a structured "Travel Card," not just a photo.

**Required fields**:
- Media (1-10 photos or 1 video)
- Location (map pin → lat/lng + place name + country)
- Category: `spot` | `food` | `stay` | `activity` | `tip` | `warning`

**Optional but prompted fields**:
- Cost level (1-5 scale: $ to $$$$$)
- Best season to visit
- Suggested duration (e.g., "2 hours", "half day")
- Accessibility rating (1-5)
- Tags (auto-suggested from location + manual)

**Narrative**: Free-text travel story/description.

| User Story | Acceptance Criteria |
|------------|-------------------|
| As a user, I create a Travel Card with guided form | Creation flow: select media → pin location (required) → pick category (required) → add details → write narrative → publish |
| As a viewer, I see structured info at a glance | Card displays: category badge, place name, cost dots, season tag, media carousel |
| As a viewer, I can tap location to see it on map | Mini-map on post detail links to full destination page |

### 2.4 Smart Feed (Infinite Scroll)

Not a generic timeline — a **travel-aware, personalized feed**.

**Ranking signals** (in priority order):
1. Posts from followed users (recency-weighted)
2. Posts from wishlist destinations
3. Posts matching user's travel style
4. Seasonal relevance (beach content surfaces for northern hemisphere users in winter)
5. Trending posts (high engagement in last 7 days)
6. Nearby posts (if location permission granted)

**Implementation**: Cursor-based pagination. Redis-cached feed per user. Background job refreshes feeds periodically.

| User Story | Acceptance Criteria |
|------------|-------------------|
| As a user, I scroll an infinite feed of Travel Cards | Cursor-based pagination, 20 posts per page, smooth infinite scroll |
| As a new user, my feed is immediately useful | Onboarding quiz seeds feed with wishlist destination content + trending posts |
| As a user, I see a "Near You" section | Geolocation-based section at top of feed (opt-in) |

### 2.5 Likes, Comments & Travel Q&A

Comments are enhanced with a **structured Q&A mode**.

**Regular comments**: Standard threaded comments on posts.

**Travel Q&A**: A special comment type where users ask specific questions:
- "How much did this hotel cost per night?"
- "Is this hike safe for beginners?"
- "Best time of day to visit?"

The post author's answer is pinned and the Q&A pair becomes **searchable, indexed knowledge**.

| User Story | Acceptance Criteria |
|------------|-------------------|
| As a user, I can like/unlike a post | Toggle like. Counter updates. Like state persists. |
| As a user, I can comment on a post | Threaded comments with timestamps and user avatars |
| As a user, I can ask a structured question | "Ask" button on post opens Q&A form. Question tagged as `is_question`. Author's reply tagged as `is_answer`. |
| As a viewer, I can browse Q&A on any post | Separate "Q&A" tab on post detail shows only question-answer pairs |

### 2.6 Location-Based Discovery

The **map is a first-class navigation element**, not an afterthought.

**Features**:
- Interactive full-screen map (Mapbox/Leaflet)
- Clustered markers for posts (zoom to expand)
- Search by destination name (country, city, place)
- Filter overlay: category, cost level, season, travel style
- Tap a cluster/marker → see post previews
- Destination page: all content for a specific location

| User Story | Acceptance Criteria |
|------------|-------------------|
| As a user, I browse the discovery map | Full-screen map with clustered post markers. Bounding-box query loads visible posts. |
| As a user, I search "Bali" | Search returns destination page: post count, top posts grid, recent itineraries, average cost, popular seasons |
| As a user, I filter by "food" + "$$" | Map and list update to show only matching posts |

### 2.7 Itineraries (Basic MVP)

Users build **day-by-day trip plans** from their posts and bookmarks.

**Structure**:
```
Itinerary
  ├── Title, description, countries, duration, budget
  ├── Day 1
  │   ├── Item: [linked Travel Card] + transport note
  │   ├── Item: [custom note] "Take the 9am ferry"
  │   └── Item: [linked Travel Card]
  ├── Day 2
  │   └── ...
  └── Day N
```

**Key capability**: Other users can **clone** an itinerary and customize it for their own trip.

| User Story | Acceptance Criteria |
|------------|-------------------|
| As a user, I create an itinerary from my posts + bookmarks | Builder UI: add days, drag posts into days, add custom notes, set transport between stops |
| As a viewer, I browse someone's published itinerary | Day-by-day view with linked Travel Cards, map of the route, total cost estimate |
| As a user, I clone an itinerary to customize it | "Clone" button creates a copy under my profile. I can edit days, swap posts, adjust notes. |

---

## 3. Unique Differentiating Features

### 3.1 Structured Travel Cards
Every post has machine-readable metadata (cost, season, duration, category). Enables smart search, filtering, and recommendations that Instagram's flat caption model cannot support.

### 3.2 Interactive Destination Discovery Map
PostGIS-powered map where you tap any destination to see all posts, itineraries, and tips. Clustered markers, filterable by category/budget/season.

### 3.3 Clonable Itineraries
Browse complete trip plans from real travelers, clone them, customize dates/budget, and use them as your actual trip guide. Instagram has no concept of sequential, plannable content.

### 3.4 Travel Q&A on Posts
Structured questions on any post become searchable knowledge. "How much was this hotel?" — the answer is tagged, indexed, and findable by future travelers researching the same destination.

### 3.5 Seasonal & Budget Intelligence
The platform aggregates cost and timing data from all posts. It can surface insights like "September is the best time to visit Santorini based on 340 traveler posts" and "average daily budget: $85."

---

## 4. Key User Experience

### Main User Journey

```
1. OPEN APP → Sign up → 30-second travel style quiz + pick dream destinations
                              ↓
2. PERSONALIZED FEED → Immediately see posts from dream destinations, matching travel style
                              ↓
3. DISCOVER → Tap a post → Rich Travel Card with location, cost, tips, mini-map
            → Tap location → Full destination page with all content
                              ↓
4. SAVE → Bookmark posts → Wishlist grows → Trip planning begins naturally
                              ↓
5. PLAN → Browse itineraries → Clone one → Customize for your trip
                              ↓
6. TRAVEL → Use itinerary as guide → Discover nearby spots on the map
                              ↓
7. SHARE → Post your own Travel Cards → Build an itinerary → Help future travelers
```

### The "Aha Moment"

A user searches a destination they want to visit and finds a **complete, real itinerary with costs, timing, and honest tips** — not just pretty photos. They realize: *"I can actually plan my entire trip from this app."*

### Retention Hooks

- **Dream destination alerts**: "12 new posts from Bali this week!"
- **Pre-trip content digest**: "Your trip to Japan is in 3 weeks — here's what travelers recommend"
- **Post-trip prompt**: "You just got back from Tokyo — share your experience and help 2,400 travelers researching Japan"
- **Seasonal nudges**: "It's the perfect time to visit Portugal — see what's trending"
- **Itinerary engagement**: Notification when someone clones your itinerary

---

## 5. Content Strategy

### What Type of Content Dominates

**Structured Travel Cards** — not raw photos. Every post answers: *Where? What? How much? When? For whom?*

Content hierarchy (feed priority):
1. **Itineraries** — highest value, most planning utility
2. **Travel Cards with full metadata** — structured, searchable, useful
3. **Quick Tips / Warnings** — high utility, low effort to create
4. **Photo-only posts** — lowest priority in ranking (still allowed, but not rewarded)

### How Content Is Structured

Every post has three layers:
1. **Visual**: Photo carousel or video (the hook)
2. **Structured data**: Category, location, cost, season, duration (the utility)
3. **Narrative**: The traveler's story, tips, warnings (the depth)

### Avoiding the Photo-App Trap

- Creation flow **requires** location and category — you cannot post without them
- Cost, season, and tips are **optional but prompted** with smart defaults
- The UI makes structured content the **path of least resistance**
- Feed algorithm **rewards** posts with complete metadata (more visibility)
- Profile stats highlight **useful** metrics (countries visited, itineraries created) over vanity metrics

---

## 6. MVP Scope & Priorities

### Phase 1 — MVP (Weeks 1-8)
- Authentication with Keycloak
- User profiles with travel style
- Travel Card creation and display
- Feed with basic personalization (follows + recency)
- Likes and comments (without Q&A)
- Discovery map (basic)
- Bookmarks

### Phase 2 — Enhanced MVP (Weeks 9-14)
- Travel Q&A on posts
- Itinerary builder + clone
- Destination pages
- Feed ranking improvements (wishlist, seasonal)
- Search with filters
- Notification system

### Phase 3 — Growth (Weeks 15-20)
- Seasonal & budget intelligence (aggregated insights)
- Pre-trip content digest
- Itinerary sharing (public links)
- Content quality scoring
- Mobile-responsive PWA optimizations
