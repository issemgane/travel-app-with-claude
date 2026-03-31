import { createRoute, Link } from '@tanstack/react-router';
import { Route as rootRoute } from '../__root';
import { useTrending } from '@/hooks/useDiscovery';
import { useSearchPosts } from '@/hooks/usePosts';
import { Star, MapPin, Search, X, Clock, DollarSign, Heart, ChevronRight, Utensils, Bed, Mountain, Lightbulb, AlertTriangle, Compass } from 'lucide-react';
import type { TravelPost, PostCategory } from '@/types';
import { useCallback, useRef, useEffect, useState, useMemo } from 'react';

export const Route = createRoute({
  getParentRoute: () => rootRoute,
  path: '/',
  component: ExplorePage,
});

const CATEGORIES: { key: PostCategory | 'ALL'; label: string; icon: typeof Compass }[] = [
  { key: 'ALL', label: 'All', icon: Compass },
  { key: 'SPOT', label: 'Destinations', icon: MapPin },
  { key: 'ACTIVITY', label: 'Activities', icon: Mountain },
  { key: 'FOOD', label: 'Food & Drink', icon: Utensils },
  { key: 'STAY', label: 'Accommodation', icon: Bed },
  { key: 'TIP', label: 'Travel Tips', icon: Lightbulb },
  { key: 'WARNING', label: 'Warnings', icon: AlertTriangle },
];

function ExperienceCard({ post }: { post: TravelPost }) {
  const rating = Math.min(5, 3.5 + (post.likesCount / 30));
  const reviewCount = post.likesCount + post.commentsCount;

  return (
    <Link to={`/post/${post.id}`} className="block group">
      <article className="h-full flex flex-col">
        {/* Image */}
        <div className="relative aspect-[3/2] rounded-xl overflow-hidden mb-3">
          {post.media.length > 0 ? (
            <img
              src={post.media[0].mediaUrl}
              alt={post.placeName}
              className="w-full h-full object-cover group-hover:scale-105 transition-transform duration-500"
              loading="lazy"
            />
          ) : (
            <div className="w-full h-full bg-gray-200 flex items-center justify-center">
              <MapPin size={48} className="text-gray-400" />
            </div>
          )}
          {/* Wishlist heart */}
          <button
            onClick={(e) => { e.preventDefault(); e.stopPropagation(); }}
            className="absolute top-3 right-3 w-8 h-8 bg-white/90 backdrop-blur-sm rounded-full flex items-center justify-center shadow-sm hover:bg-white transition"
          >
            <Heart size={16} className="text-gray-600" />
          </button>
          {/* Category badge */}
          <div className="absolute bottom-3 left-3">
            <span className="bg-white/95 backdrop-blur-sm text-gray-800 text-xs font-semibold px-2.5 py-1 rounded-md shadow-sm">
              {post.category.charAt(0) + post.category.slice(1).toLowerCase()}
            </span>
          </div>
        </div>

        {/* Content */}
        <div className="flex flex-col flex-1">
          {/* Location */}
          <p className="text-xs font-medium text-gray-500 uppercase tracking-wide mb-1">
            {post.countryCode} \u00b7 {post.placeName}
          </p>

          {/* Title / Content */}
          <h3 className="text-[15px] font-semibold text-gray-900 leading-snug line-clamp-2 mb-2 group-hover:text-wanderlust-primary transition-colors">
            {post.content}
          </h3>

          {/* Duration + Season */}
          <div className="flex items-center gap-3 text-xs text-gray-500 mb-2">
            {post.durationSuggested && (
              <span className="flex items-center gap-1">
                <Clock size={12} /> {post.durationSuggested}
              </span>
            )}
            {post.bestSeason && (
              <span>Best in {post.bestSeason.charAt(0) + post.bestSeason.slice(1).toLowerCase()}</span>
            )}
          </div>

          {/* Spacer */}
          <div className="mt-auto" />

          {/* Rating + Reviews */}
          <div className="flex items-center gap-1.5 mb-1.5">
            <div className="flex items-center gap-0.5">
              <Star size={14} className="fill-yellow-400 text-yellow-400" />
              <span className="text-sm font-bold text-gray-900">{rating.toFixed(1)}</span>
            </div>
            <span className="text-xs text-gray-500">({reviewCount})</span>
          </div>

          {/* Price / Cost */}
          <div className="flex items-center gap-1">
            <span className="text-xs text-gray-500">Cost level</span>
            <div className="flex items-center">
              {Array.from({ length: 5 }, (_, i) => (
                <DollarSign
                  key={i}
                  size={12}
                  className={i < (post.costLevel ?? 0) ? 'text-gray-900' : 'text-gray-300'}
                />
              ))}
            </div>
          </div>
        </div>
      </article>
    </Link>
  );
}

function DestinationCard({ country, count, image }: { country: string; count: number; image: string }) {
  return (
    <div className="relative rounded-xl overflow-hidden aspect-[4/3] group cursor-pointer">
      <img src={image} alt={country} className="w-full h-full object-cover group-hover:scale-105 transition-transform duration-500" />
      <div className="absolute inset-0 bg-gradient-to-t from-black/70 via-black/20 to-transparent" />
      <div className="absolute bottom-4 left-4 text-white">
        <h3 className="text-lg font-bold">{country}</h3>
        <p className="text-sm text-white/80">{count} experiences</p>
      </div>
    </div>
  );
}

function ExplorePage() {
  const [searchQuery, setSearchQuery] = useState('');
  const [activeSearch, setActiveSearch] = useState('');
  const [activeCategory, setActiveCategory] = useState<PostCategory | 'ALL'>('ALL');

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

  const allPosts = data?.pages.flatMap((page) => page.content) ?? [];
  const posts = activeCategory === 'ALL' ? allPosts : allPosts.filter(p => p.category === activeCategory);

  // Build top destinations from posts
  const destinations = useMemo(() => {
    const countryMap: Record<string, { count: number; image: string; name: string }> = {};
    allPosts.forEach(p => {
      if (!countryMap[p.countryCode]) {
        countryMap[p.countryCode] = { count: 0, image: p.media[0]?.mediaUrl ?? '', name: p.placeName.split(',')[0] };
      }
      countryMap[p.countryCode].count++;
    });
    return Object.entries(countryMap)
      .sort((a, b) => b[1].count - a[1].count)
      .slice(0, 4)
      .map(([code, d]) => ({ code, ...d }));
  }, [allPosts]);

  const handleSearch = (e: React.FormEvent) => {
    e.preventDefault();
    setActiveSearch(searchQuery);
  };

  const clearSearch = () => {
    setSearchQuery('');
    setActiveSearch('');
  };

  return (
    <div className="min-h-screen bg-white">
      {/* Hero Section */}
      <div className="relative bg-gradient-to-br from-brand-800 via-brand-700 to-brand-600 overflow-hidden">
        {/* Background pattern */}
        <div className="absolute inset-0 opacity-10">
          <div className="absolute top-10 left-10 w-72 h-72 bg-white rounded-full blur-3xl" />
          <div className="absolute bottom-10 right-10 w-96 h-96 bg-wanderlust-secondary rounded-full blur-3xl" />
        </div>

        <div className="relative max-w-5xl mx-auto px-4 py-16 md:py-24 text-center">
          <h1 className="text-3xl md:text-5xl font-extrabold text-white mb-4 leading-tight">
            Unforgettable travel experiences
          </h1>
          <p className="text-lg md:text-xl text-blue-100 mb-8 max-w-2xl mx-auto">
            Discover destinations, activities, and travel tips from explorers worldwide
          </p>

          {/* Search Bar */}
          <form onSubmit={handleSearch} className="max-w-2xl mx-auto">
            <div className="relative bg-white rounded-full shadow-xl flex items-center">
              <Search size={20} className="absolute left-5 text-gray-400" />
              <input
                type="text"
                placeholder="Search destinations, activities, or experiences..."
                value={searchQuery}
                onChange={(e) => setSearchQuery(e.target.value)}
                className="w-full pl-14 pr-32 py-4 rounded-full text-gray-800 text-base focus:outline-none"
              />
              {searchQuery && (
                <button
                  type="button"
                  onClick={clearSearch}
                  className="absolute right-28 text-gray-400 hover:text-gray-600"
                >
                  <X size={18} />
                </button>
              )}
              <button
                type="submit"
                className="absolute right-2 bg-wanderlust-primary hover:bg-brand-800 text-white px-6 py-2.5 rounded-full text-sm font-semibold transition"
              >
                Search
              </button>
            </div>
          </form>
        </div>
      </div>

      {/* Category Pills */}
      <div className="border-b border-gray-100 bg-white sticky top-14 z-40">
        <div className="max-w-6xl mx-auto px-4">
          <div className="flex gap-1 overflow-x-auto py-3 scrollbar-hide -mx-4 px-4">
            {CATEGORIES.map(({ key, label, icon: Icon }) => (
              <button
                key={key}
                onClick={() => setActiveCategory(key)}
                className={`flex items-center gap-2 px-4 py-2 rounded-full text-sm font-medium whitespace-nowrap transition-all ${
                  activeCategory === key
                    ? 'bg-gray-900 text-white shadow-sm'
                    : 'bg-gray-100 text-gray-600 hover:bg-gray-200'
                }`}
              >
                <Icon size={16} />
                {label}
              </button>
            ))}
          </div>
        </div>
      </div>

      <div className="max-w-6xl mx-auto px-4 py-8 pb-20 md:pb-8">
        {/* Top Destinations (only when not searching and showing all) */}
        {!isSearching && activeCategory === 'ALL' && destinations.length > 0 && (
          <section className="mb-10">
            <div className="flex items-center justify-between mb-5">
              <h2 className="text-xl md:text-2xl font-bold text-gray-900">Top destinations</h2>
              <button className="flex items-center gap-1 text-sm font-semibold text-wanderlust-primary hover:underline">
                See all <ChevronRight size={16} />
              </button>
            </div>
            <div className="grid grid-cols-2 md:grid-cols-4 gap-3 md:gap-4">
              {destinations.map(d => (
                <DestinationCard key={d.code} country={d.name} count={d.count} image={d.image} />
              ))}
            </div>
          </section>
        )}

        {/* Experiences Grid */}
        <section>
          <div className="flex items-center justify-between mb-5">
            <h2 className="text-xl md:text-2xl font-bold text-gray-900">
              {isSearching
                ? `Results for "${activeSearch}"`
                : activeCategory === 'ALL'
                  ? 'Trending experiences'
                  : `${CATEGORIES.find(c => c.key === activeCategory)?.label ?? ''} experiences`
              }
            </h2>
            {posts.length > 0 && (
              <span className="text-sm text-gray-500">{posts.length} results</span>
            )}
          </div>

          {isLoading ? (
            <div className="flex justify-center items-center py-20">
              <div className="animate-spin rounded-full h-10 w-10 border-[3px] border-gray-200 border-t-wanderlust-primary" />
            </div>
          ) : posts.length === 0 ? (
            <div className="text-center py-16 bg-gray-50 rounded-2xl">
              <Compass size={48} className="mx-auto text-gray-300 mb-4" />
              <p className="text-lg font-semibold text-gray-700 mb-1">No experiences found</p>
              <p className="text-sm text-gray-500">
                {isSearching ? 'Try a different search term' : 'Check back later for new content'}
              </p>
            </div>
          ) : (
            <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-x-5 gap-y-8">
              {posts.map((post, index) => (
                <div key={post.id} ref={index === posts.length - 1 ? lastPostRef : undefined}>
                  <ExperienceCard post={post} />
                </div>
              ))}
            </div>
          )}

          {isFetchingNextPage && (
            <div className="flex justify-center py-8">
              <div className="animate-spin rounded-full h-8 w-8 border-[3px] border-gray-200 border-t-wanderlust-primary" />
            </div>
          )}
        </section>
      </div>
    </div>
  );
}
