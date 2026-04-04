import { createRootRoute, Outlet, Link } from '@tanstack/react-router';
import { useAuth } from '@/lib/auth';
import { Compass, Home, PlusSquare, User, Bookmark } from 'lucide-react';

const rootRoute = createRootRoute({
  component: RootLayout,
});

function RootLayout() {
  const { isAuthenticated, user, logout } = useAuth();

  return (
    <div className="min-h-screen bg-gray-50">
      {/* Top Navbar */}
      <nav className="sticky top-0 z-50 bg-white border-b border-gray-200 px-4 h-14 flex items-center justify-between">
        <Link to="/" className="text-xl font-bold text-wanderlust-primary">
          Wanderlust
        </Link>

        <div className="hidden md:flex items-center gap-6">
          <Link to="/" className="text-gray-600 hover:text-wanderlust-primary flex items-center gap-1">
            <Home size={20} /> Feed
          </Link>
          <Link to="/explore" className="text-gray-600 hover:text-wanderlust-primary flex items-center gap-1">
            <Compass size={20} /> Explore
          </Link>
          {isAuthenticated && (
            <>
              <Link to="/post/create" className="text-gray-600 hover:text-wanderlust-primary flex items-center gap-1">
                <PlusSquare size={20} /> Create
              </Link>
              <Link to="/bookmarks" className="text-gray-600 hover:text-wanderlust-primary flex items-center gap-1">
                <Bookmark size={20} /> Saved
              </Link>
            </>
          )}
        </div>

        <div className="flex items-center gap-3">
          {isAuthenticated && user ? (
            <div className="flex items-center gap-2">
              <Link to={`/profile/${user.id}`} className="flex items-center gap-2 text-sm text-gray-700 hover:text-wanderlust-primary">
                <div className="w-8 h-8 rounded-full bg-brand-200 flex items-center justify-center overflow-hidden">
                  {user.avatarUrl ? (
                    <img src={user.avatarUrl} alt="" className="w-8 h-8 rounded-full object-cover" />
                  ) : (
                    <User size={16} />
                  )}
                </div>
                <span className="hidden md:inline">{user.displayName}</span>
              </Link>
              <button onClick={logout} className="text-sm text-gray-500 hover:text-gray-700 ml-2">
                Logout
              </button>
            </div>
          ) : (
            <div className="flex items-center gap-2">
              <Link to="/auth/login" className="text-sm text-gray-600 hover:text-wanderlust-primary font-medium">
                Sign In
              </Link>
              <Link to="/auth/register"
                className="bg-wanderlust-primary text-white px-4 py-2 rounded-lg text-sm font-medium hover:bg-brand-800">
                Sign Up
              </Link>
            </div>
          )}
        </div>
      </nav>

      <main className="max-w-7xl mx-auto">
        <Outlet />
      </main>

      {/* Mobile Bottom Nav - Order: Feed, Explore, Create, Saved */}
      <nav className="md:hidden fixed bottom-0 left-0 right-0 bg-white border-t border-gray-200 flex justify-around py-2 z-50">
        <Link to="/" className="flex flex-col items-center text-xs text-gray-600">
          <Home size={22} /><span>Feed</span>
        </Link>
        <Link to="/explore" className="flex flex-col items-center text-xs text-gray-600">
          <Compass size={22} /><span>Explore</span>
        </Link>
        {isAuthenticated && (
          <Link to="/post/create" className="flex flex-col items-center text-xs text-gray-600">
            <PlusSquare size={22} /><span>Create</span>
          </Link>
        )}
        {isAuthenticated && (
          <Link to="/bookmarks" className="flex flex-col items-center text-xs text-gray-600">
            <Bookmark size={22} /><span>Saved</span>
          </Link>
        )}
        <Link to={user ? `/profile/${user.id}` : '/auth/login'} className="flex flex-col items-center text-xs text-gray-600">
          <User size={22} /><span>Profile</span>
        </Link>
      </nav>
    </div>
  );
}

// Route imports
import { Route as FeedRoute } from './index';
import { Route as ExploreRoute } from './explore/index';
import { Route as PostDetailRoute } from './post/$postId';
import { Route as CreatePostRoute } from './post/create';
import { Route as ProfileRoute } from './profile/$userId';
import { Route as LoginRoute } from './auth/login';
import { Route as RegisterRoute } from './auth/register';
import { Route as BookmarksRoute } from './bookmarks';

export const routeTree = rootRoute.addChildren([
  FeedRoute,
  ExploreRoute,
  PostDetailRoute,
  CreatePostRoute,
  ProfileRoute,
  LoginRoute,
  RegisterRoute,
  BookmarksRoute,
]);

export { rootRoute as Route };
