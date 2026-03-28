import { createRoute } from '@tanstack/react-router';
import { Route as rootRoute } from '../__root';
import { useProfile, useUserStats } from '@/hooks/useProfile';
import { useAuth } from '@/lib/auth';
import { MapPin, Globe, Calendar } from 'lucide-react';

export const Route = createRoute({
  getParentRoute: () => rootRoute,
  path: '/profile/$userId',
  component: ProfilePage,
});

function ProfilePage() {
  const { userId } = Route.useParams();
  const { data: user, isLoading } = useProfile(userId);
  const { data: stats } = useUserStats(userId);
  const { user: currentUser } = useAuth();

  if (isLoading || !user) {
    return (
      <div className="flex justify-center items-center py-20">
        <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-wanderlust-primary" />
      </div>
    );
  }

  const isOwnProfile = currentUser?.id === user.id;

  return (
    <div className="max-w-3xl mx-auto px-4 py-6 pb-20">
      {/* Profile Header */}
      <div className="flex items-start gap-6 mb-6">
        <div className="w-24 h-24 rounded-full bg-brand-100 flex items-center justify-center overflow-hidden flex-shrink-0">
          {user.avatarUrl ? (
            <img src={user.avatarUrl} alt="" className="w-full h-full object-cover" />
          ) : (
            <span className="text-3xl font-bold text-brand-700">
              {user.displayName.charAt(0).toUpperCase()}
            </span>
          )}
        </div>
        <div className="flex-1">
          <div className="flex items-center gap-3 mb-1">
            <h1 className="text-xl font-bold">{user.displayName}</h1>
            {user.travelStyle && (
              <span className="text-xs bg-wanderlust-secondary/10 text-wanderlust-secondary px-2 py-0.5 rounded-full font-medium">
                {user.travelStyle.replace('_', ' ')}
              </span>
            )}
          </div>
          <p className="text-sm text-gray-500 mb-2">@{user.username}</p>
          {user.bio && <p className="text-sm text-gray-700 mb-3">{user.bio}</p>}

          {!isOwnProfile && (
            <button className="bg-wanderlust-primary text-white px-6 py-2 rounded-lg text-sm font-medium hover:bg-brand-800">
              Follow
            </button>
          )}
          {isOwnProfile && (
            <button className="border border-gray-200 text-gray-700 px-6 py-2 rounded-lg text-sm font-medium hover:bg-gray-50">
              Edit Profile
            </button>
          )}
        </div>
      </div>

      {/* Stats */}
      {stats && (
        <div className="grid grid-cols-4 gap-4 mb-6 py-4 border-y border-gray-100">
          <div className="text-center">
            <p className="text-lg font-bold">{stats.postsCount}</p>
            <p className="text-xs text-gray-500">Posts</p>
          </div>
          <div className="text-center">
            <p className="text-lg font-bold">{stats.followersCount}</p>
            <p className="text-xs text-gray-500">Followers</p>
          </div>
          <div className="text-center">
            <p className="text-lg font-bold">{stats.followingCount}</p>
            <p className="text-xs text-gray-500">Following</p>
          </div>
          <div className="text-center">
            <p className="text-lg font-bold flex items-center justify-center gap-1">
              <Globe size={16} /> {stats.countriesVisited}
            </p>
            <p className="text-xs text-gray-500">Countries</p>
          </div>
        </div>
      )}

      {/* Member since */}
      <p className="text-xs text-gray-400 flex items-center gap-1">
        <Calendar size={12} /> Member since {new Date(user.createdAt).toLocaleDateString('en-US', { month: 'long', year: 'numeric' })}
      </p>
    </div>
  );
}
