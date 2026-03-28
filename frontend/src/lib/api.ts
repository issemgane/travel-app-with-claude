const API_BASE = import.meta.env.VITE_API_URL || '/api';

class ApiClient {
  private token: string | null = null;

  setToken(token: string | null) {
    this.token = token;
  }

  private async request<T>(path: string, options: RequestInit = {}): Promise<T> {
    const headers: Record<string, string> = {
      'Content-Type': 'application/json',
      ...((options.headers as Record<string, string>) || {}),
    };

    if (this.token) {
      headers['Authorization'] = `Bearer ${this.token}`;
    }

    const response = await fetch(`${API_BASE}${path}`, {
      ...options,
      headers,
    });

    if (!response.ok) {
      const error = await response.json().catch(() => ({ message: response.statusText }));
      throw new Error(error.message || `HTTP ${response.status}`);
    }

    if (response.status === 204) return undefined as T;
    return response.json();
  }

  // Auth
  login(email: string, password: string) {
    return this.request<{ token: string }>('/auth/login', {
      method: 'POST',
      body: JSON.stringify({ email, password }),
    });
  }

  register(username: string, displayName: string, email: string, password: string) {
    return this.request<{ token: string }>('/auth/register', {
      method: 'POST',
      body: JSON.stringify({ username, displayName, email, password }),
    });
  }

  getMe() {
    return this.request<import('@/types').User>('/auth/me');
  }

  // Users
  getUser(id: string) {
    return this.request<import('@/types').User>(`/users/${id}`);
  }

  getUserStats(id: string) {
    return this.request<import('@/types').UserStats>(`/users/${id}/stats`);
  }

  updateProfile(data: Partial<import('@/types').User>) {
    return this.request<import('@/types').User>('/users/me', {
      method: 'PUT',
      body: JSON.stringify(data),
    });
  }

  follow(userId: string) {
    return this.request<void>(`/users/${userId}/follow`, { method: 'POST' });
  }

  unfollow(userId: string) {
    return this.request<void>(`/users/${userId}/follow`, { method: 'DELETE' });
  }

  // Posts
  createPost(data: import('@/types').CreatePostRequest) {
    return this.request<import('@/types').TravelPost>('/posts', {
      method: 'POST',
      body: JSON.stringify(data),
    });
  }

  getPost(id: string) {
    return this.request<import('@/types').TravelPost>(`/posts/${id}`);
  }

  deletePost(id: string) {
    return this.request<void>(`/posts/${id}`, { method: 'DELETE' });
  }

  getFeed(page = 0, size = 20) {
    return this.request<import('@/types').PagedResponse<import('@/types').TravelPost>>(
      `/posts/feed?page=${page}&size=${size}`
    );
  }

  getNearby(lat: number, lng: number, radius = 10000, page = 0) {
    return this.request<import('@/types').PagedResponse<import('@/types').TravelPost>>(
      `/posts/near?lat=${lat}&lng=${lng}&radius=${radius}&page=${page}`
    );
  }

  getByDestination(countryCode: string, page = 0) {
    return this.request<import('@/types').PagedResponse<import('@/types').TravelPost>>(
      `/posts/destination/${countryCode}?page=${page}`
    );
  }

  searchPosts(q: string, page = 0) {
    return this.request<import('@/types').PagedResponse<import('@/types').TravelPost>>(
      `/posts/search?q=${encodeURIComponent(q)}&page=${page}`
    );
  }

  // Likes
  toggleLike(postId: string) {
    return this.request<{ liked: boolean }>(`/posts/${postId}/like`, { method: 'POST' });
  }

  // Comments
  getComments(postId: string, page = 0) {
    return this.request<import('@/types').PagedResponse<import('@/types').Comment>>(
      `/posts/${postId}/comments?page=${page}`
    );
  }

  addComment(postId: string, content: string, isQuestion = false, parentId?: string) {
    return this.request<import('@/types').Comment>(`/posts/${postId}/comments`, {
      method: 'POST',
      body: JSON.stringify({ content, isQuestion, parentId }),
    });
  }

  getQuestions(postId: string, page = 0) {
    return this.request<import('@/types').PagedResponse<import('@/types').Comment>>(
      `/posts/${postId}/comments/questions?page=${page}`
    );
  }

  // Discovery
  getMapPosts(swLat: number, swLng: number, neLat: number, neLng: number, page = 0) {
    return this.request<import('@/types').PagedResponse<import('@/types').TravelPost>>(
      `/discover/map?swLat=${swLat}&swLng=${swLng}&neLat=${neLat}&neLng=${neLng}&page=${page}`
    );
  }

  getTrending(page = 0) {
    return this.request<import('@/types').PagedResponse<import('@/types').TravelPost>>(
      `/discover/trending?page=${page}`
    );
  }

  getDestination(countryCode: string) {
    return this.request<import('@/types').DestinationSummary>(
      `/discover/destinations/${countryCode}`
    );
  }

  // Itineraries
  createItinerary(data: { title: string; description?: string; countryCodes?: string[]; durationDays: number; estimatedBudgetUsd?: number }) {
    return this.request<import('@/types').Itinerary>('/itineraries', {
      method: 'POST',
      body: JSON.stringify(data),
    });
  }

  getItinerary(id: string) {
    return this.request<import('@/types').Itinerary>(`/itineraries/${id}`);
  }

  cloneItinerary(id: string) {
    return this.request<import('@/types').Itinerary>(`/itineraries/${id}/clone`, {
      method: 'POST',
    });
  }

  // Bookmarks
  bookmark(postId: string) {
    return this.request<void>(`/bookmarks/${postId}`, { method: 'POST' });
  }

  removeBookmark(postId: string) {
    return this.request<void>(`/bookmarks/${postId}`, { method: 'DELETE' });
  }

  getBookmarks(page = 0) {
    return this.request<import('@/types').PagedResponse<import('@/types').TravelPost>>(
      `/bookmarks?page=${page}`
    );
  }

}

export const api = new ApiClient();
