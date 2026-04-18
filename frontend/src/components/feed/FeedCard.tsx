import { Link, useNavigate } from '@tanstack/react-router';
import { Heart, MessageCircle, Bookmark, MapPin } from 'lucide-react';
import type { TravelPost } from '@/types';
import { CategoryBadge } from '@/components/post/CategoryBadge';
import { useToggleLike } from '@/hooks/useInteractions';
import { useToggleBookmark } from '@/hooks/useBookmarks';
import { useAuth } from '@/lib/auth';
import { useState, useEffect } from 'react';

interface FeedCardProps {
  post: TravelPost;
}

export function FeedCard({ post }: FeedCardProps) {
  const { isAuthenticated, token } = useAuth();
  const navigate = useNavigate();
  const toggleLike = useToggleLike(post.id);
  const toggleBookmark = useToggleBookmark();
  const [liked, setLiked] = useState(false);
  const [likesCount, setLikesCount] = useState(post.likesCount);
  const [bookmarked, setBookmarked] = useState(false);

  useEffect(() => {
    if (!token) return;
    fetch('/api/bookmarks/ids', {
      headers: { 'Authorization': `Bearer ${token}` },
    })
      .then(r => r.ok ? r.json() : [])
      .then((ids: string[]) => setBookmarked(ids.includes(post.id)))
      .catch(() => {});
  }, [token, post.id]);

  const requireAuth = (e: React.MouseEvent) => {
    e.preventDefault();
    e.stopPropagation();
    navigate({ to: '/auth/login', search: { redirect: `/post/${post.id}` } });
  };

  const handleLike = (e: React.MouseEvent) => {
    e.preventDefault();
    e.stopPropagation();
    if (!isAuthenticated) { requireAuth(e); return; }
    const newLiked = !liked;
    setLiked(newLiked);
    setLikesCount(prev => newLiked ? prev + 1 : prev - 1);
    toggleLike.mutate(undefined, {
      onError: () => { setLiked(!newLiked); setLikesCount(prev => newLiked ? prev - 1 : prev + 1); },
    });
  };

  const handleBookmark = (e: React.MouseEvent) => {
    e.preventDefault();
    e.stopPropagation();
    if (!isAuthenticated) return;
    const newBookmarked = !bookmarked;
    setBookmarked(newBookmarked);
    toggleBookmark.mutate({ postId: post.id, isBookmarked: !newBookmarked }, {
      onError: () => setBookmarked(!newBookmarked),
    });
  };

  return (
    <article className="bg-white rounded-xl shadow-sm border border-gray-100 overflow-hidden">
      {/* Header */}
      <div className="flex items-center justify-between px-4 py-3">
        <Link to={`/profile/${post.user.id}`} className="flex items-center gap-3">
          <div className="w-10 h-10 rounded-full bg-brand-100 flex items-center justify-center overflow-hidden">
            {post.user.avatarUrl ? (
              <img src={post.user.avatarUrl} alt="" className="w-full h-full object-cover" />
            ) : (
              <span className="text-sm font-medium text-brand-700">
                {post.user.displayName.charAt(0).toUpperCase()}
              </span>
            )}
          </div>
          <div>
            <p className="text-sm font-semibold">{post.user.displayName}</p>
            <p className="text-xs text-gray-500 flex items-center gap-1">
              <MapPin size={12} /> {post.placeName}, {post.countryCode}
            </p>
          </div>
        </Link>
        <CategoryBadge category={post.category} />
      </div>

      {/* Media */}
      <Link to={`/post/${post.id}`}>
        {post.media.length > 0 && (
          <div className="aspect-[4/3] bg-gray-100 relative overflow-hidden">
            <img src={post.media[0].mediaUrl} alt={post.placeName}
              className="w-full h-full object-cover" loading="lazy" />
            {post.media.length > 1 && (
              <div className="absolute top-3 right-3 bg-black/60 text-white text-xs px-2 py-1 rounded-full">
                1/{post.media.length}
              </div>
            )}
          </div>
        )}
      </Link>

      {/* Content */}
      <div className="px-4 py-3">
        <p className="text-sm text-gray-800 line-clamp-3">{post.content}</p>
        {post.tags.length > 0 && (
          <div className="flex flex-wrap gap-1 mt-2">
            {post.tags.slice(0, 5).map(tag => (
              <span key={tag} className="text-xs text-brand-600 bg-brand-50 px-2 py-0.5 rounded-full">#{tag}</span>
            ))}
          </div>
        )}
      </div>

      {/* Actions */}
      <div className="px-4 py-3 flex items-center justify-between border-t border-gray-50">
        <div className="flex items-center gap-4">
          <button onClick={handleLike}
            className={`flex items-center gap-1 transition ${liked ? 'text-red-500' : 'text-gray-600 hover:text-red-500'}`}>
            <Heart size={20} className={liked ? 'fill-red-500' : ''} />
            <span className="text-sm">{likesCount}</span>
          </button>
          <Link to={`/post/${post.id}`} className="flex items-center gap-1 text-gray-600 hover:text-brand-600 transition">
            <MessageCircle size={20} />
            <span className="text-sm">{post.commentsCount}</span>
          </Link>
        </div>
        {isAuthenticated && (
          <button onClick={handleBookmark} className="transition">
            <Bookmark
              size={20}
              style={{ fill: bookmarked ? '#f59e0b' : 'none', color: bookmarked ? '#f59e0b' : '#6b7280' }}
            />
          </button>
        )}
      </div>
    </article>
  );
}
