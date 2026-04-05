import { createRoute, Link } from '@tanstack/react-router';
import { Route as rootRoute } from '../__root';
import { useFollowers, useFollowStatus, useFollow, useUnfollow } from '@/hooks/useProfile';
import { useAuth } from '@/lib/auth';
import { ArrowLeft, UserPlus, UserCheck, Loader2 } from 'lucide-react';
import type { User } from '@/types';

export const Route = createRoute({
  getParentRoute: () => rootRoute,
  path: '/profile/$userId/followers',
  component: FollowersPage,
});

function FollowUserButton({ userId }: { userId: string }) {
  const { user: currentUser, isAuthenticated } = useAuth();
  const { data: followStatus } = useFollowStatus(userId, isAuthenticated && currentUser?.id !== userId);
  const followMutation = useFollow(userId);
  const unfollowMutation = useUnfollow(userId);

  if (!isAuthenticated || currentUser?.id === userId) return null;

  const isFollowing = followStatus?.following ?? false;
  const isMutating = followMutation.isPending || unfollowMutation.isPending;

  return (
    <button
      onClick={() => isFollowing ? unfollowMutation.mutate() : followMutation.mutate()}
      disabled={isMutating}
      className={`px-4 py-1.5 rounded-lg text-xs font-medium flex items-center gap-1.5 transition ${
        isFollowing
          ? 'bg-gray-100 text-gray-600 border border-gray-200 hover:bg-red-50 hover:text-red-600'
          : 'bg-wanderlust-primary text-white hover:bg-brand-800'
      }`}
    >
      {isMutating ? <Loader2 size={14} className="animate-spin" /> :
       isFollowing ? <><UserCheck size={14} /> Following</> : <><UserPlus size={14} /> Follow</>}
    </button>
  );
}

function UserListItem({ user }: { user: User }) {
  return (
    <div className="flex items-center justify-between py-3 px-1">
      <Link to={`/profile/${user.id}`} className="flex items-center gap-3 min-w-0 flex-1">
        <div className="w-11 h-11 rounded-full bg-brand-100 flex items-center justify-center overflow-hidden flex-shrink-0">
          {user.avatarUrl ? (
            <img src={user.avatarUrl} alt="" className="w-full h-full object-cover" />
          ) : (
            <span className="text-sm font-bold text-brand-700">{user.displayName.charAt(0).toUpperCase()}</span>
          )}
        </div>
        <div className="min-w-0">
          <p className="text-sm font-semibold truncate">{user.displayName}</p>
          <p className="text-xs text-gray-500 truncate">@{user.username}</p>
        </div>
      </Link>
      <FollowUserButton userId={user.id} />
    </div>
  );
}

function FollowersPage() {
  const { userId } = Route.useParams();
  const { data, isLoading, fetchNextPage, hasNextPage, isFetchingNextPage } = useFollowers(userId);
  const users = data?.pages.flatMap(p => p.content) ?? [];

  return (
    <div className="max-w-lg mx-auto px-4 py-6 pb-20">
      <div className="flex items-center gap-3 mb-6">
        <Link to={`/profile/${userId}`} className="text-gray-500 hover:text-gray-700">
          <ArrowLeft size={20} />
        </Link>
        <h1 className="text-xl font-bold">Followers</h1>
      </div>

      {isLoading ? (
        <div className="flex justify-center py-12">
          <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-wanderlust-primary" />
        </div>
      ) : users.length === 0 ? (
        <p className="text-center text-gray-500 py-12">No followers yet</p>
      ) : (
        <div className="divide-y divide-gray-100">
          {users.map(user => <UserListItem key={user.id} user={user} />)}
        </div>
      )}

      {hasNextPage && (
        <button onClick={() => fetchNextPage()} disabled={isFetchingNextPage}
          className="w-full py-3 text-sm text-wanderlust-primary font-medium hover:bg-brand-50 rounded-lg mt-2">
          {isFetchingNextPage ? 'Loading...' : 'Load more'}
        </button>
      )}
    </div>
  );
}
