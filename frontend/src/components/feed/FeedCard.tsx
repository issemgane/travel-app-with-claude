import { Link } from '@tanstack/react-router';
import { Heart, MessageCircle, Bookmark, MapPin, Clock, DollarSign } from 'lucide-react';
import type { TravelPost } from '@/types';
import { CategoryBadge } from '@/components/post/CategoryBadge';

interface FeedCardProps {
  post: TravelPost;
}

export function FeedCard({ post }: FeedCardProps) {
  const costDots = post.costLevel
    ? Array.from({ length: 5 }, (_, i) => i < post.costLevel!).map((active, i) => (
        <span key={i} className={`text-sm ${active ? 'text-wanderlust-secondary' : 'text-gray-300'}`}>$</span>
      ))
    : null;

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
      {post.media.length > 0 && (
        <div className="aspect-[4/3] bg-gray-100 relative overflow-hidden">
          <img
            src={post.media[0].mediaUrl}
            alt={post.placeName}
            className="w-full h-full object-cover"
            loading="lazy"
          />
          {post.media.length > 1 && (
            <div className="absolute top-3 right-3 bg-black/60 text-white text-xs px-2 py-1 rounded-full">
              1/{post.media.length}
            </div>
          )}
        </div>
      )}

      {/* Metadata Bar */}
      <div className="px-4 py-2 flex items-center gap-4 text-xs text-gray-500 border-b border-gray-50">
        {costDots && <div className="flex items-center gap-0.5"><DollarSign size={12} />{costDots}</div>}
        {post.bestSeason && <span>{post.bestSeason}</span>}
        {post.durationSuggested && (
          <span className="flex items-center gap-1"><Clock size={12} />{post.durationSuggested}</span>
        )}
      </div>

      {/* Content */}
      <div className="px-4 py-3">
        <p className="text-sm text-gray-800 line-clamp-3">{post.content}</p>
        {post.tags.length > 0 && (
          <div className="flex flex-wrap gap-1 mt-2">
            {post.tags.slice(0, 5).map((tag) => (
              <span key={tag} className="text-xs text-brand-600 bg-brand-50 px-2 py-0.5 rounded-full">
                #{tag}
              </span>
            ))}
          </div>
        )}
      </div>

      {/* Actions */}
      <div className="px-4 py-3 flex items-center justify-between border-t border-gray-50">
        <div className="flex items-center gap-4">
          <button className="flex items-center gap-1 text-gray-600 hover:text-red-500 transition">
            <Heart size={20} />
            <span className="text-sm">{post.likesCount}</span>
          </button>
          <Link to={`/post/${post.id}`} className="flex items-center gap-1 text-gray-600 hover:text-brand-600 transition">
            <MessageCircle size={20} />
            <span className="text-sm">{post.commentsCount}</span>
          </Link>
        </div>
        <button className="text-gray-600 hover:text-wanderlust-secondary transition">
          <Bookmark size={20} />
        </button>
      </div>
    </article>
  );
}
