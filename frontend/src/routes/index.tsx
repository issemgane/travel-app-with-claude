import { createRoute } from '@tanstack/react-router';
import { Route as rootRoute } from './__root';
import { useFeed } from '@/hooks/usePosts';
import { useTrending } from '@/hooks/useDiscovery';
import { useUserStats } from '@/hooks/useProfile';
import { useAuth } from '@/lib/auth';
import { FeedCard } from '@/components/feed/FeedCard';
import { useCallback, useRef, useEffect, useState } from 'react';
import { Users, Compass } from 'lucide-react';

export const Route = createRoute({
  getParentRoute: () => rootRoute,
  path: '/feed',
  component: FeedPage,
});

function FeedPage() {
  const { user, isAuthenticated } = useAuth();
  const { data: stats } = useUserStats(user?.id ?? '');

  // Determine default tab: if user follows nobody, default to Discover
  const hasFollowing = (stats?.followingCount ?? 0) > 0;
  const [activeTab, setActiveTab] = useState<'following' | 'discover' | null>(null);

  // Set default tab once stats load
  useEffect(() => {
    if (activeTab === null && stats !== undefined) {
      setActiveTab(hasFollowing ? 'following' : 'discover');
    }
  }, [stats, hasFollowing]);

  const tab = activeTab ?? 'discover';

  const followingQuery = useFeed();
  const discoverQuery = useTrending();

  const query = tab === 'following' ? followingQuery : discoverQuery;
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

  if (!isAuthenticated) {
    return (
      <div className="max-w-2xl mx-auto px-4 py-12 text-center">
        <h1 className="text-2xl font-bold mb-3">Your Feed</h1>
        <p className="text-gray-500">Sign in to see posts from travelers you follow.</p>
      </div>
    );
  }

  return (
    <div className="max-w-2xl mx-auto px-4 py-6 pb-20 md:pb-6">
      <h1 className="text-2xl font-bold mb-4">Your Feed</h1>

      {/* Tabs */}
      <div className="flex border-b border-gray-200 mb-6">
        <button
          onClick={() => setActiveTab('following')}
          className={`flex items-center gap-2 px-4 py-3 text-sm font-medium border-b-2 transition ${
            tab === 'following'
              ? 'border-wanderlust-primary text-wanderlust-primary'
              : 'border-transparent text-gray-500 hover:text-gray-700'
          }`}
        >
          <Users size={16} /> Following
        </button>
        <button
          onClick={() => setActiveTab('discover')}
          className={`flex items-center gap-2 px-4 py-3 text-sm font-medium border-b-2 transition ${
            tab === 'discover'
              ? 'border-wanderlust-primary text-wanderlust-primary'
              : 'border-transparent text-gray-500 hover:text-gray-700'
          }`}
        >
          <Compass size={16} /> Discover
        </button>
      </div>

      {isLoading ? (
        <div className="flex justify-center items-center py-20">
          <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-wanderlust-primary" />
        </div>
      ) : posts.length === 0 ? (
        <div className="text-center py-12 text-gray-500">
          {tab === 'following' ? (
            <>
              <p className="text-lg mb-2">No posts from people you follow</p>
              <p className="text-sm mb-4">Follow more travelers to fill your feed, or check Discover.</p>
              <button onClick={() => setActiveTab('discover')}
                className="text-wanderlust-primary font-medium hover:underline">
                Switch to Discover
              </button>
            </>
          ) : (
            <>
              <p className="text-lg mb-2">No posts found</p>
              <p className="text-sm">Check back later for new content</p>
            </>
          )}
        </div>
      ) : (
        <div className="space-y-6">
          {posts.map((post, index) => (
            <div key={post.id} ref={index === posts.length - 1 ? lastPostRef : undefined}>
              <FeedCard post={post} />
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
