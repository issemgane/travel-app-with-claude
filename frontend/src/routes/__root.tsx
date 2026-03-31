import { createRootRoute, Outlet, Link } from '@tanstack/react-router';
import { useAuth } from '@/lib/auth';
import { Compass, Home, PlusSquare, User, Bookmark, Globe } from 'lucide-react';

// Root layout
const rootRoute = createRootRoute({
  component: RootLayout,
});

function RootLayout() {
  const { isAuthenticated, user, logout } = useAuth();

  return (
    <div className="min-h-screen bg-white">
      {/* Top Navbar */}
      <nav className="sticky top-0 z-50 bg-white border-b border-gray-200 px-4 md:px-6 h-14 flex items-center justify-between">
        <Link to="/" className="flex items-center gap-2">
          <Globe size={24} className="text-wanderlust-primary" />
          <span className="text-lg font-bold text-gray-900">Wanderlust</span>
        </Link>

        <div className="hidden md:flex items-center gap-1">
          {isAuthenticated && (
            <>
              <Link to="/feed" className="text-sm font-medium text-gray-600 hover:text-gray-900 px-3 py-2 rounded-lg hover:bg-gray-50 transition">
                Feed
              </Link>
              <Link to="/post/create" className="text-sm font-medium text-gray-600 hover:text-gray-900 px-3 py-2 rounded-lg hover:bg-gray-50 transition">
                Create
              </Link>
              <Link to="/bookmarks" className="text-sm font-medium text-gray-600 hover:text-gray-900 px-3 py-2 rounded-lg hover:bg-gray-50 transition">
                Saved
              </Link>
            </>
          )}
        </div>

        <div className="flex items-center gap-2">
          {isAuthenticated && user ? (
            <div className="flex items-center gap-2">
              <Link to={`/profile/${user.id}`} className="flex items-center gap-2 text-sm text-gray-700 hover:text-gray-900 px-2 py-1.5 rounded-lg hover:bg-gray-50 transition">
                <div className="w-8 h-8 rounded-full bg-brand-100 flex items-center justify-center overflow-hidden">
                  {user.avatarUrl ? (
                    <img src={user.avatarUrl} alt="" className="w-8 h-8 rounded-full object-cover" />
                  ) : (
                    <User size={16} className="text-brand-600" />
                  )}
                </div>
                <span className="hidden md:inline font-medium">{user.displayName}</span>
              </Link>
              <button onClick={logout} className="text-sm text-gray-500 hover:text-gray-700 px-3 py-2 rounded-lg hover:bg-gray-50 transition">
                Logout
              </button>
            </div>
          ) : (
            <div className="flex items-center gap-2">
              <Link to="/auth/login"
                className="text-sm font-medium text-gray-700 hover:text-gray-900 px-3 py-2 rounded-lg hover:bg-gray-50 transition">
                Log in
              </Link>
              <Link to="/auth/register"
                className="bg-wanderlust-primary text-white px-5 py-2 rounded-full text-sm font-semibold hover:bg-brand-800 transition shadow-sm">
                Sign up
              </Link>
            </div>
          )}
        </div>
      </nav>

      <main>
        <Outlet />
      </main>

      {/* Mobile Bottom Nav */}
      <nav className="md:hidden fixed bottom-0 left-0 right-0 bg-white border-t border-gray-200 flex justify-around py-2 z-50">
        <Link to="/" className="flex flex-col items-center text-xs text-gray-500 hover:text-wanderlust-primary">
          <Compass size={22} /><span>Explore</span>
        </Link>
        {isAuthenticated && (
          <Link to="/feed" className="flex flex-col items-center text-xs text-gray-500 hover:text-wanderlust-primary">
            <Home size={22} /><span>Feed</span>
          </Link>
        )}
        {isAuthenticated && (
          <Link to="/post/create" className="flex flex-col items-center text-xs text-gray-500 hover:text-wanderlust-primary">
            <PlusSquare size={22} /><span>Create</span>
          </Link>
        )}
        {isAuthenticated && (
          <Link to="/bookmarks" className="flex flex-col items-center text-xs text-gray-500 hover:text-wanderlust-primary">
            <Bookmark size={22} /><span>Saved</span>
          </Link>
        )}
        <Link to={user ? `/profile/${user.id}` : '/auth/login'} className="flex flex-col items-center text-xs text-gray-500 hover:text-wanderlust-primary">
          <User size={22} /><span>{isAuthenticated ? 'Profile' : 'Sign in'}</span>
        </Link>
      </nav>
    </div>
  );
}

// Import page components inline to keep it simple
import { Route as FeedRoute } from './index';
import { Route as ExploreRoute } from './explore/index';
import { Route as PostDetailRoute } from './post/$postId';
import { Route as CreatePostRoute } from './post/create';
import { Route as ProfileRoute } from './profile/$userId';
import { Route as LoginRoute } from './auth/login';
import { Route as RegisterRoute } from './auth/register';

export const routeTree = rootRoute.addChildren([
  FeedRoute,
  ExploreRoute,
  PostDetailRoute,
  CreatePostRoute,
  ProfileRoute,
  LoginRoute,
  RegisterRoute,
]);

export { rootRoute as Route };
