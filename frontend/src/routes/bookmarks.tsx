import { createRoute, Link } from '@tanstack/react-router';
import { Route as rootRoute } from './__root';
import { useBookmarks, useToggleBookmark } from '@/hooks/useBookmarks';
import { Heart, MessageCircle, MapPin, Bookmark, X } from 'lucide-react';
import type { TravelPost } from '@/types';
import { CategoryBadge } from '@/components/post/CategoryBadge';
import { useCallback, useRef, useEffect } from 'react';
import { useAuth } from '@/lib/auth';

export const Route = createRoute({
  getParentRoute: () => rootRoute,
  path: '/bookmarks',
  component: BookmarksPage,
});

function SavedCard({ post }: { post: TravelPost }) {
  const toggleBookmark = useToggleBookmark();

  const handleRemove = (e: React.MouseEvent) => {
    e.preventDefault();
    e.stopPropagation();
    toggleBookmark.mutate({ postId: post.id, isBookmarked: true });
  };

  return (
    <Link to={`/post/${post.id}`} className="block group">
      <article className="bg-white rounded-xl shadow-sm border border-gray-100 overflow-hidden hover:shadow-md transition-shadow">
        <div className="flex">
          {post.media.length > 0 && (
            <div className="w-28 h-28 flex-shrink-0 bg-gray-100">
              <img src={post.media[0].mediaUrl} alt={post.placeName}
                className="w-full h-full object-cover" loading="lazy" />
            </div>
          )}
          <div className="flex-1 p-3 min-w-0">
            <div className="flex items-start justify-between gap-2">
              <div className="min-w-0">
                <div className="flex items-center gap-1 text-xs text-gray-500 mb-0.5">
                  <MapPin size={12} />
                  <span className="truncate">{post.placeName}, {post.countryCode}</span>
                </div>
                <p className="text-sm font-medium text-gray-800 line-clamp-2">{post.content}</p>
              </div>
              <button onClick={handleRemove}
                className="flex-shrink-0 p-1 text-gray-400 hover:text-red-500 rounded transition">
                <X size={16} />
              </button>
            </div>
            <div className="flex items-center gap-3 mt-2 text-xs text-gray-500">
              <CategoryBadge category={post.category} />
              <span className="flex items-center gap-0.5"><Heart size={12} /> {post.likesCount}</span>
              <span className="flex items-center gap-0.5"><MessageCircle size={12} /> {post.commentsCount}</span>
            </div>
          </div>
        </div>
      </article>
    </Link>
  );
}

function BookmarksPage() {
  const { isAuthenticated } = useAuth();
  const { data, fetchNextPage, hasNextPage, isFetchingNextPage, isLoading } = useBookmarks();

  const observerRef = useRef<IntersectionObserver | null>(null);
  const lastRef = useCallback(
    (node: HTMLDivElement | null) => {
      if (isFetchingNextPage) return;
      if (observerRef.current) observerRef.current.disconnect();
      observerRef.current = new IntersectionObserver((entries) => {
        if (entries[0].isIntersecting && hasNextPage) fetchNextPage();
      });
      if (node) observerRef.current.observe(node);
    },
    [isFetchingNextPage, hasNextPage, fetchNextPage]
  );

  useEffect(() => { return () => observerRef.current?.disconnect(); }, []);

  const posts = data?.pages.flatMap(p => p.content) ?? [];

  if (!isAuthenticated) {
    return (
      <div className="max-w-2xl mx-auto px-4 py-20 text-center text-gray-500">
        <Bookmark size={48} className="mx-auto mb-4 text-gray-300" />
        <p className="text-lg font-medium mb-2">Sign in to see your saved posts</p>
        <Link to="/auth/login" className="text-wanderlust-primary font-medium hover:underline">Sign in</Link>
      </div>
    );
  }

  if (isLoading) {
    return (
      <div className="flex justify-center py-20">
        <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-wanderlust-primary" />
      </div>
    );
  }

  return (
    <div className="max-w-2xl mx-auto px-4 py-6 pb-20 md:pb-6">
      <h1 className="text-2xl font-bold mb-6">Saved Posts</h1>

      {posts.length === 0 ? (
        <div className="text-center py-12 text-gray-500">
          <Bookmark size={48} className="mx-auto mb-4 text-gray-300" />
          <p className="text-lg mb-2">No saved posts yet</p>
          <p className="text-sm">Bookmark posts you want to revisit later</p>
        </div>
      ) : (
        <div className="space-y-3">
          {posts.map((post, i) => (
            <div key={post.id} ref={i === posts.length - 1 ? lastRef : undefined}>
              <SavedCard post={post} />
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
