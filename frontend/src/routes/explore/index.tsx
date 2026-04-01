import { createRoute, Link } from '@tanstack/react-router';
import { Route as rootRoute } from '../__root';
import { useTrending } from '@/hooks/useDiscovery';
import { useSearchPosts } from '@/hooks/usePosts';
import { Heart, MessageCircle, MapPin, Search, X } from 'lucide-react';
import type { TravelPost } from '@/types';
import { CategoryBadge } from '@/components/post/CategoryBadge';
import { useCallback, useRef, useEffect, useState } from 'react';

export const Route = createRoute({
  getParentRoute: () => rootRoute,
  path: '/',
  component: ExplorePage,
});

function PostCard({ post }: { post: TravelPost }) {
  return (
    <Link to={`/post/${post.id}`} className="block group">
      <article className="bg-white rounded-xl shadow-sm border border-gray-100 overflow-hidden hover:shadow-md transition-shadow">
        {/* Image */}
        <div className="aspect-[4/3] bg-gray-100 relative overflow-hidden">
          {post.media.length > 0 ? (
            <img
              src={post.media[0].mediaUrl}
              alt={post.placeName}
              className="w-full h-full object-cover group-hover:scale-105 transition-transform duration-300"
              loading="lazy"
            />
          ) : (
            <div className="w-full h-full flex items-center justify-center text-gray-400">
              <MapPin size={48} />
            </div>
          )}
          <div className="absolute top-2 left-2">
            <CategoryBadge category={post.category} />
          </div>
          {post.media.length > 1 && (
            <div className="absolute top-2 right-2 bg-black/60 text-white text-xs px-2 py-0.5 rounded-full">
              1/{post.media.length}
            </div>
          )}
        </div>

        {/* Content */}
        <div className="p-3">
          {/* Location */}
          <div className="flex items-center gap-1 text-xs text-gray-500 mb-1">
            <MapPin size={12} className="shrink-0" />
            <span className="truncate">{post.placeName}, {post.countryCode}</span>
          </div>

          {/* Text */}
          <p className="text-sm text-gray-800 line-clamp-2 mb-2">{post.content}</p>

          {/* User + Stats */}
          <div className="flex items-center justify-between">
            <div className="flex items-center gap-2">
              <div className="w-6 h-6 rounded-full bg-brand-100 overflow-hidden flex items-center justify-center">
                {post.user.avatarUrl ? (
                  <img src={post.user.avatarUrl} alt="" className="w-full h-full object-cover" />
                ) : (
                  <span className="text-xs font-medium text-brand-700">
                    {post.user.displayName.charAt(0).toUpperCase()}
                  </span>
                )}
              </div>
              <span className="text-xs text-gray-600 truncate max-w-[100px]">{post.user.displayName}</span>
            </div>
            <div className="flex items-center gap-3 text-xs text-gray-500">
              <span className="flex items-center gap-1">
                <Heart size={14} /> {post.likesCount}
              </span>
              <span className="flex items-center gap-1">
                <MessageCircle size={14} /> {post.commentsCount}
              </span>
            </div>
          </div>

          {/* Tags */}
          {post.tags.length > 0 && (
            <div className="flex flex-wrap gap-1 mt-2">
              {post.tags.slice(0, 3).map((tag) => (
                <span key={tag} className="text-[10px] text-brand-600 bg-brand-50 px-1.5 py-0.5 rounded-full">
                  #{tag}
                </span>
              ))}
            </div>
          )}
        </div>
      </article>
    </Link>
  );
}

function ExplorePage() {
  const [searchQuery, setSearchQuery] = useState('');
  const [activeSearch, setActiveSearch] = useState('');

  const trending = useTrending();
  const searchResults = useSearchPosts(activeSearch);

  const isSearching = activeSearch.length > 2;
  const query = isSearching ? searchResults : trending;

  const { data, fetchNextPage, hasNextPage, isFetchingNextPage, isLoading } = query;

  const observerRef = useRef<IntersectionObserver | null>(null);
  const lastPostRef = useCallback(
    (node: HTMLDivElement | null) => {
      if (isFetchingNextPage) return;
      if (observerRef.current) observerRef.current.disconnect();
      observerRef.current = new IntersectionObserver((entries) => {
        if (entries[0].isIntersecting && hasNextPage) {
          fetchNextPage();
        }
      });
      if (node) observerRef.current.observe(node);
    },
    [isFetchingNextPage, hasNextPage, fetchNextPage]
  );

  useEffect(() => {
    return () => observerRef.current?.disconnect();
  }, []);

  const posts = data?.pages.flatMap((page) => page.content) ?? [];

  const handleSearch = (e: React.FormEvent) => {
    e.preventDefault();
    setActiveSearch(searchQuery);
  };

  const clearSearch = () => {
    setSearchQuery('');
    setActiveSearch('');
  };

  return (
    <div className="max-w-6xl mx-auto px-4 py-6 pb-20 md:pb-6">
      {/* Header */}
      <div className="mb-6">
        <h1 className="text-2xl font-bold mb-2">Explore</h1>
        <p className="text-gray-500 text-sm">Discover amazing travel experiences from around the world</p>
      </div>

      {/* Search */}
      <form onSubmit={handleSearch} className="mb-6">
        <div className="relative">
          <Search size={18} className="absolute left-3 top-1/2 -translate-y-1/2 text-gray-400" />
          <input
            type="text"
            placeholder="Search places, countries, or keywords..."
            value={searchQuery}
            onChange={(e) => setSearchQuery(e.target.value)}
            className="w-full pl-10 pr-10 py-2.5 rounded-lg border border-gray-200 focus:outline-none focus:ring-2 focus:ring-brand-500 focus:border-transparent text-sm"
          />
          {searchQuery && (
            <button
              type="button"
              onClick={clearSearch}
              className="absolute right-3 top-1/2 -translate-y-1/2 text-gray-400 hover:text-gray-600"
            >
              <X size={18} />
            </button>
          )}
        </div>
      </form>

      {/* Section title */}
      <h2 className="text-lg font-semibold mb-4">
        {isSearching ? `Results for "${activeSearch}"` : 'Trending Posts'}
      </h2>

      {isLoading ? (
        <div className="flex justify-center items-center py-20">
          <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-wanderlust-primary" />
        </div>
      ) : posts.length === 0 ? (
        <div className="text-center py-12 text-gray-500">
          <p className="text-lg mb-2">No posts found</p>
          <p className="text-sm">
            {isSearching ? 'Try a different search term' : 'Check back later for trending content'}
          </p>
        </div>
      ) : (
        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-4">
          {posts.map((post, index) => (
            <div key={post.id} ref={index === posts.length - 1 ? lastPostRef : undefined}>
              <PostCard post={post} />
            </div>
          ))}
        </div>
      )}

      {isFetchingNextPage && (
        <div className="flex justify-center py-4">
          <div className="animate-spin rounded-full h-6 w-6 border-b-2 border-wanderlust-primary" />
        </div>
      )}
    </div>
  );
}
