// ── Enums ────────────────────────────────────────────────
export type TravelStyle = 'BACKPACKER' | 'LUXURY' | 'FAMILY' | 'SOLO' | 'ADVENTURE' | 'DIGITAL_NOMAD';
export type PostCategory = 'SPOT' | 'FOOD' | 'STAY' | 'ACTIVITY' | 'TIP' | 'WARNING';
export type MediaType = 'IMAGE' | 'VIDEO';

// ── Pagination ───────────────────────────────────────────
export interface PagedResponse<T> {
  content: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
  last: boolean;
}

// ── User ─────────────────────────────────────────────────
export interface User {
  id: string;
  username: string;
  displayName: string;
  bio: string | null;
  avatarUrl: string | null;
  travelStyle: TravelStyle | null;
  countriesVisitedCount: number;
  createdAt: string;
}

export interface UserStats {
  postsCount: number;
  followersCount: number;
  followingCount: number;
  countriesVisited: number;
  itinerariesCount: number;
}

// ── Post ─────────────────────────────────────────────────
export interface PostMedia {
  id: string;
  mediaUrl: string;
  mediaType: MediaType;
  displayOrder: number;
  width: number | null;
  height: number | null;
}

export interface UserSummary {
  id: string;
  username: string;
  displayName: string;
  avatarUrl: string | null;
}

export interface TravelPost {
  id: string;
  user: UserSummary;
  content: string;
  category: PostCategory;
  costLevel: number | null;
  bestSeason: string | null;
  durationSuggested: string | null;
  accessibilityRating: number | null;
  latitude: number;
  longitude: number;
  placeName: string;
  countryCode: string;
  tags: string[];
  likesCount: number;
  commentsCount: number;
  media: PostMedia[];
  createdAt: string;
  updatedAt: string;
}

export interface CreatePostRequest {
  content: string;
  category?: PostCategory;
  latitude: number;
  longitude: number;
  placeName: string;
  countryCode: string;
  costLevel?: number;
  bestSeason?: string;
  durationSuggested?: string;
  accessibilityRating?: number;
  tags?: string[];
  mediaItems: {
    mediaUrl: string;
    mediaType?: MediaType;
    width?: number;
    height?: number;
  }[];
}

// ── Comment ──────────────────────────────────────────────
export interface Comment {
  id: string;
  postId: string;
  parentId: string | null;
  userId: string;
  username: string;
  displayName: string;
  avatarUrl: string | null;
  content: string;
  isQuestion: boolean;
  isAnswer: boolean;
  createdAt: string;
}

// ── Itinerary ────────────────────────────────────────────
export interface ItineraryItem {
  id: string;
  postId: string | null;
  customTitle: string | null;
  customNote: string | null;
  transportToNext: string | null;
  displayOrder: number;
}

export interface ItineraryDay {
  id: string;
  dayNumber: number;
  title: string | null;
  notes: string | null;
  items: ItineraryItem[];
}

export interface Itinerary {
  id: string;
  userId: string;
  username: string;
  title: string;
  description: string | null;
  countryCodes: string[];
  durationDays: number;
  estimatedBudgetUsd: number | null;
  coverImageUrl: string | null;
  isPublished: boolean;
  clonedFrom: string | null;
  days: ItineraryDay[];
  createdAt: string;
  updatedAt: string;
}

// ── Discovery ────────────────────────────────────────────
export interface DestinationSummary {
  countryCode: string;
  postCount: number;
  topPosts: TravelPost[];
  averageCostLevel: number | null;
}

// ── Media ────────────────────────────────────────────────
export interface PresignedUrlResponse {
  uploadUrl: string;
  mediaUrl: string;
  key: string;
}
