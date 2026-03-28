# Wanderlust — Technical Architecture

## 1. System Overview

```
┌─────────────────────────────────────────────────────────────────┐
│                         CLIENTS                                  │
│  React SPA (TanStack Router + Query) ──── ShardCDN (static)    │
└──────────────────────┬──────────────────────────────────────────┘
                       │ HTTPS / REST
                       ▼
┌──────────────────────────────────────────────────────────────────┐
│                      EDGE / AUTH                                  │
│  Keycloak (OIDC Provider) ◄──── JWT validation ────►  Spring    │
│  - Social login (Google, Apple)                       Security   │
│  - User registration                                  OAuth2     │
│  - Token issuance + refresh                           Resource   │
└──────────────────────┬──────────────────────────────────────────┘
                       │
                       ▼
┌──────────────────────────────────────────────────────────────────┐
│              SPRING BOOT API (Modular Monolith)                   │
│                                                                    │
│  ┌──────────┐ ┌──────────┐ ┌──────────┐ ┌───────────────┐       │
│  │   Auth   │ │   User   │ │   Post   │ │  Interaction  │       │
│  │  Module  │ │  Module  │ │  Module  │ │    Module     │       │
│  └──────────┘ └──────────┘ └──────────┘ └───────────────┘       │
│  ┌──────────┐ ┌──────────┐ ┌──────────┐ ┌───────────────┐       │
│  │   Feed   │ │Discovery │ │Itinerary │ │    Media      │       │
│  │  Module  │ │  Module  │ │  Module  │ │    Module     │       │
│  └──────────┘ └──────────┘ └──────────┘ └───────────────┘       │
│                                                                    │
│  Spring ApplicationEvents for inter-module communication          │
└─────┬──────────────┬──────────────┬──────────────┬───────────────┘
      │              │              │              │
      ▼              ▼              ▼              ▼
┌──────────┐  ┌──────────┐  ┌──────────┐  ┌──────────────┐
│PostgreSQL│  │  Redis   │  │  MinIO   │  │   Keycloak   │
│+ PostGIS │  │  Cache   │  │(S3-compat)│  │   (OIDC)    │
│          │  │          │  │  Media    │  │              │
└──────────┘  └──────────┘  └──────────┘  └──────────────┘
```

## 2. Tech Stack

| Layer | Technology | Version | Purpose |
|-------|-----------|---------|---------|
| Frontend | React | 18.x | UI framework |
| Routing | TanStack Router | 1.x | Type-safe file-based routing |
| Data fetching | TanStack Query | 5.x | Server state management, caching, infinite scroll |
| UI components | shadcn/ui | latest | Accessible, customizable component library |
| Maps | Leaflet + react-leaflet | 1.9 / 4.x | Interactive discovery map |
| Build | Vite | 5.x | Fast dev server + production builds |
| CDN | ShardCDN | — | Static asset + media delivery |
| Backend | Spring Boot | 3.2.5 | REST API framework |
| Language | Java | 21 | LTS with virtual threads support |
| Security | Spring Security + OAuth2 | 6.x | JWT validation, authorization |
| ORM | Hibernate + Spatial | 6.4 | JPA with PostGIS support |
| Database | PostgreSQL | 16 | Primary data store |
| Geospatial | PostGIS | 3.4 | Location queries, spatial indexing |
| Cache | Redis | 7.x | Feed caching, session data, rate limiting |
| Object storage | MinIO | latest | S3-compatible media storage |
| Auth provider | Keycloak | 23.x | OIDC, social login, user management |
| API docs | SpringDoc OpenAPI | 2.3 | Swagger UI auto-generation |
| Containers | Docker Compose | 3.8 | Local dev environment |

## 3. Scalable Architecture Approach

### Modular Monolith Strategy

Start as a **modular monolith** with clear bounded contexts. Each module is a separate Java package with:
- Its own entities, repositories, services, and controllers
- No direct entity cross-references (use IDs, not JPA relationships across modules)
- Communication via Spring `ApplicationEvents` for async operations
- Each module can be extracted to a microservice later by replacing events with a message broker

### Module Boundaries

```
com.wanderlust.api
├── auth/            → Keycloak integration, JWT processing
├── user/            → Profiles, follows, wishlists, travel style
├── post/            → Travel Cards, media references, CRUD
├── feed/            → Feed generation, ranking, caching
├── interaction/     → Likes, comments, Q&A
├── discovery/       → Map queries, destination pages, search
├── itinerary/       → Trip plans, days, items, cloning
├── media/           → Upload orchestration, presigned URLs
├── common/          → Shared DTOs, exceptions, utilities
└── config/          → Security, CORS, Redis, S3, OpenAPI
```

### Scaling Path

```
Phase 1 (MVP):     Single Spring Boot instance + PostgreSQL
Phase 2 (Growth):  Read replicas + Redis cluster + CDN for media
Phase 3 (Scale):   Extract feed/discovery to separate services
                   Add Elasticsearch for search
                   Event bus (Kafka/RabbitMQ) replaces ApplicationEvents
                   Horizontal scaling behind load balancer
```

## 4. Database Schema

### Entity Relationship Diagram

```
users ──────< travel_posts ──────< post_media
  │                │
  │                ├──────< likes
  │                │
  │                ├──────< comments
  │                │
  │                └──────< bookmarks
  │
  ├──────< follows (self-referencing)
  │
  ├──────< user_wishlist_destinations
  │
  └──────< itineraries ──────< itinerary_days ──────< itinerary_items
                                                          │
                                                          └──── travel_posts (FK, nullable)
```

### Key Schema Decisions

1. **Geography columns** use SRID 4326 (WGS84) for global lat/lng coordinates
2. **Arrays** (tags, country_codes) stored as PostgreSQL native arrays with GIN indexes
3. **Denormalized counters** (likes_count, comments_count) on posts for read performance — updated via triggers or application events
4. **Soft deletes** not used in MVP — hard deletes with cascade
5. **UUID primary keys** for all tables — no sequential IDs exposed in API

See `db/init.sql` for complete DDL.

## 5. API Design

### Authentication
| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| GET | `/api/auth/me` | Required | Current user info from JWT |

*Note: Registration, login, and token management handled by Keycloak directly.*

### Users & Profiles
| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| GET | `/api/users/{id}` | Optional | Get user profile |
| PUT | `/api/users/me` | Required | Update own profile |
| GET | `/api/users/{id}/stats` | Optional | Get user statistics |
| PUT | `/api/users/me/travel-style` | Required | Update travel style |
| PUT | `/api/users/me/location` | Required | Update current location |
| POST | `/api/users/{id}/follow` | Required | Follow a user |
| DELETE | `/api/users/{id}/follow` | Required | Unfollow a user |
| GET | `/api/users/{id}/followers` | Optional | List followers (paginated) |
| GET | `/api/users/{id}/following` | Optional | List following (paginated) |

### Travel Posts
| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| POST | `/api/posts` | Required | Create a Travel Card |
| GET | `/api/posts/{id}` | Optional | Get post detail |
| PUT | `/api/posts/{id}` | Required | Update own post |
| DELETE | `/api/posts/{id}` | Required | Delete own post |
| GET | `/api/posts/feed` | Required | Personalized feed (cursor-paginated) |
| GET | `/api/posts/near` | Optional | Posts near location (`?lat=&lng=&radius=`) |
| GET | `/api/posts/destination/{code}` | Optional | Posts by country code |
| GET | `/api/posts/search` | Optional | Full-text search (`?q=&category=&costLevel=`) |

### Interactions
| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| POST | `/api/posts/{id}/like` | Required | Like a post |
| DELETE | `/api/posts/{id}/like` | Required | Unlike a post |
| GET | `/api/posts/{id}/comments` | Optional | Get comments (paginated) |
| POST | `/api/posts/{id}/comments` | Required | Add comment |
| GET | `/api/posts/{id}/questions` | Optional | Get Q&A pairs |

### Discovery
| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| GET | `/api/discover/map` | Optional | Posts in bounding box (`?neLat=&neLng=&swLat=&swLng=`) |
| GET | `/api/discover/trending` | Optional | Trending posts (last 7 days) |
| GET | `/api/discover/destinations/{code}` | Optional | Destination summary page |

### Itineraries
| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| POST | `/api/itineraries` | Required | Create itinerary |
| GET | `/api/itineraries/{id}` | Optional | Get itinerary with days/items |
| PUT | `/api/itineraries/{id}` | Required | Update itinerary |
| DELETE | `/api/itineraries/{id}` | Required | Delete itinerary |
| POST | `/api/itineraries/{id}/clone` | Required | Clone itinerary |
| GET | `/api/itineraries/{id}/days` | Optional | Get itinerary days |
| POST | `/api/itineraries/{id}/days/{dayId}/items` | Required | Add item to day |

### Bookmarks
| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| POST | `/api/bookmarks/{postId}` | Required | Bookmark a post |
| DELETE | `/api/bookmarks/{postId}` | Required | Remove bookmark |
| GET | `/api/bookmarks` | Required | List bookmarks (paginated) |

### Media
| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| POST | `/api/media/presigned-url` | Required | Get presigned upload URL |
| POST | `/api/media/confirm` | Required | Confirm upload complete |

### Pagination Strategy

- **Feed**: Cursor-based (after=`{postId}`, size=20). No offset/limit to avoid stale page issues.
- **All other lists**: Offset-based (page=0, size=20) via Spring `Pageable`.
- **Response envelope**: `{ content: [...], page, size, totalElements, totalPages, last }`

### Error Response Format

```json
{
  "status": 404,
  "error": "Not Found",
  "message": "Post not found with id: 550e8400-e29b-41d4-a716-446655440000",
  "timestamp": "2026-03-28T10:30:00Z",
  "path": "/api/posts/550e8400-e29b-41d4-a716-446655440000"
}
```

## 6. Frontend Architecture

### Route Structure (TanStack Router)

```
src/routes/
├── __root.tsx              → Root layout (navbar, auth provider)
├── index.tsx               → Feed (home page, infinite scroll)
├── explore/
│   ├── index.tsx           → Discovery map + search
│   └── destination.$code.tsx → Destination page
├── post/
│   ├── $postId.tsx         → Post detail view
│   └── create.tsx          → Travel Card creation form
├── profile/
│   ├── $userId.tsx         → User profile page
│   └── edit.tsx            → Edit own profile
├── itinerary/
│   ├── $itineraryId.tsx    → Itinerary detail view
│   ├── create.tsx          → Itinerary builder
│   └── edit.$itineraryId.tsx → Edit itinerary
├── bookmarks.tsx           → Saved posts
└── auth/
    ├── login.tsx           → Login (redirects to Keycloak)
    └── callback.tsx        → OIDC callback handler
```

### Component Architecture

```
src/components/
├── ui/                     → shadcn/ui primitives (Button, Card, Dialog, etc.)
├── layout/
│   ├── Navbar.tsx          → Top navigation bar
│   ├── BottomNav.tsx       → Mobile bottom navigation
│   └── PageContainer.tsx   → Responsive page wrapper
├── feed/
│   ├── FeedCard.tsx        → Travel Card in feed view (compact)
│   ├── FeedList.tsx        → Infinite scroll container
│   └── FeedFilters.tsx     → Feed filter bar
├── post/
│   ├── TravelCard.tsx      → Full Travel Card display
│   ├── PostForm.tsx        → Create/edit post form
│   ├── MediaCarousel.tsx   → Photo/video carousel
│   ├── LocationPicker.tsx  → Map-based location selector
│   └── CategoryBadge.tsx   → Post category indicator
├── map/
│   ├── DiscoveryMap.tsx    → Full-screen Leaflet map
│   ├── PostMarker.tsx      → Custom map marker for posts
│   └── MapFilters.tsx      → Map overlay filters
├── itinerary/
│   ├── ItineraryBuilder.tsx → Drag-and-drop day builder
│   ├── DayView.tsx         → Single day in itinerary
│   ├── ItineraryCard.tsx   → Itinerary preview card
│   └── RouteMap.tsx        → Itinerary route on map
├── profile/
│   ├── ProfileHeader.tsx   → Profile banner + stats
│   ├── TravelStats.tsx     → Countries visited, trips, etc.
│   └── PostGrid.tsx        → Grid of user's posts
└── interaction/
    ├── LikeButton.tsx      → Animated like toggle
    ├── CommentList.tsx     → Threaded comments
    ├── CommentForm.tsx     → Add comment / ask question
    └── QASection.tsx       → Q&A tab on post detail
```

### Data Fetching (TanStack Query)

```
src/hooks/
├── useAuth.ts              → Auth state, login/logout, current user
├── usePosts.ts             → useInfiniteQuery for feed, useQuery for post detail
├── useCreatePost.ts        → useMutation for post creation
├── useComments.ts          → Comments CRUD with optimistic updates
├── useLike.ts              → Optimistic like toggle
├── useDiscovery.ts         → Map bounds query, destination data
├── useItineraries.ts       → Itinerary CRUD + clone
├── useProfile.ts           → User profile + stats
├── useFollow.ts            → Follow/unfollow with optimistic updates
├── useBookmarks.ts         → Bookmark toggle + list
└── useMediaUpload.ts       → Presigned URL flow + upload progress
```

### State Management

- **Server state**: TanStack Query (sole source of truth for API data)
- **Auth state**: React Context wrapping Keycloak JS adapter
- **UI state**: React useState/useReducer (local, no global store needed for MVP)
- **URL state**: TanStack Router search params for filters, map bounds

## 7. Security

### Authentication Flow

```
1. User clicks "Login" → Redirect to Keycloak login page
2. User authenticates (email/password or Google/Apple)
3. Keycloak redirects back with authorization code
4. Frontend exchanges code for tokens (PKCE flow)
5. Access token (JWT) sent in Authorization header on every API call
6. Spring Security validates JWT signature + expiry against Keycloak JWKS
7. Refresh token used to get new access token before expiry
```

### Authorization Rules

- **Public endpoints**: GET posts, GET profiles, GET discovery, GET itineraries
- **Authenticated**: All write operations, feed, bookmarks, follow
- **Owner-only**: Update/delete own posts, update own profile, update own itineraries
- **Rate limiting**: Redis-based. 100 req/min for authenticated, 30 req/min for anonymous.

## 8. Infrastructure (Docker Compose)

Development environment runs entirely in Docker:

| Service | Image | Port | Purpose |
|---------|-------|------|---------|
| postgres | postgis/postgis:16-3.4 | 5432 | Database with PostGIS |
| redis | redis:7-alpine | 6379 | Cache |
| keycloak | keycloak/keycloak:23.0 | 8180 | Identity provider |
| minio | minio/minio | 9000/9001 | Object storage |

See `docker-compose.yml` in project root.

## 9. Monitoring & Observability (Post-MVP)

- **Health checks**: Spring Boot Actuator `/actuator/health`
- **Metrics**: Micrometer → Prometheus → Grafana
- **Logging**: Structured JSON logs → ELK stack
- **Tracing**: OpenTelemetry for distributed tracing (when microservices split)
