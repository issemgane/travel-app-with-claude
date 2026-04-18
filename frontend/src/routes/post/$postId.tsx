import { createRoute, Link, useNavigate } from '@tanstack/react-router';
import { Route as rootRoute } from '../__root';
import { usePost } from '@/hooks/usePosts';
import { useComments, useAddComment, useQuestions, useToggleLike } from '@/hooks/useInteractions';
import { useToggleBookmark } from '@/hooks/useBookmarks';
import { CategoryBadge } from '@/components/post/CategoryBadge';
import { Heart, MessageCircle, MapPin, Send, ChevronLeft, ChevronRight } from 'lucide-react';
import { useState, useEffect, useRef } from 'react';
import { useAuth } from '@/lib/auth';

export const Route = createRoute({
  getParentRoute: () => rootRoute,
  path: '/post/$postId',
  component: PostDetailPage,
});

function ImageCarousel({ images, placeName }: { images: { mediaUrl: string }[]; placeName: string }) {
  const [current, setCurrent] = useState(0);
  if (images.length === 0) return null;

  const prev = () => setCurrent(i => (i === 0 ? images.length - 1 : i - 1));
  const next = () => setCurrent(i => (i === images.length - 1 ? 0 : i + 1));

  return (
    <div className="relative aspect-[16/9] bg-gray-100 rounded-xl overflow-hidden mb-4 group">
      <img src={images[current].mediaUrl} alt={placeName} className="w-full h-full object-cover" />
      {images.length > 1 && (
        <>
          <button onClick={prev}
            className="absolute left-2 top-1/2 -translate-y-1/2 bg-black/50 hover:bg-black/70 text-white rounded-full p-1.5 opacity-0 group-hover:opacity-100 transition">
            <ChevronLeft size={20} />
          </button>
          <button onClick={next}
            className="absolute right-2 top-1/2 -translate-y-1/2 bg-black/50 hover:bg-black/70 text-white rounded-full p-1.5 opacity-0 group-hover:opacity-100 transition">
            <ChevronRight size={20} />
          </button>
          <div className="absolute bottom-3 left-1/2 -translate-x-1/2 flex gap-1.5">
            {images.map((_, i) => (
              <button key={i} onClick={() => setCurrent(i)}
                className={`w-2 h-2 rounded-full transition ${i === current ? 'bg-white' : 'bg-white/50'}`} />
            ))}
          </div>
          <div className="absolute top-3 right-3 bg-black/60 text-white text-xs px-2 py-1 rounded-full">
            {current + 1}/{images.length}
          </div>
        </>
      )}
    </div>
  );
}

function PostDetailPage() {
  const { postId } = Route.useParams();
  const navigate = useNavigate();
  const { data: post, isLoading } = usePost(postId);
  const { data: commentsData } = useComments(postId);
  const { data: questionsData } = useQuestions(postId);
  const addComment = useAddComment(postId);
  const toggleLike = useToggleLike(postId);
  const toggleBookmark = useToggleBookmark();
  const { isAuthenticated, token } = useAuth();
  const [commentText, setCommentText] = useState('');
  const [activeTab, setActiveTab] = useState<'comments' | 'qa'>('comments');
  const [liked, setLiked] = useState(false);
  const [localLikesCount, setLocalLikesCount] = useState<number | null>(null);
  const [bookmarked, setBookmarked] = useState(false);
  const userClickedRef = useRef(false);

  useEffect(() => {
    if (!token) return;
    fetch('/api/bookmarks/ids', {
      headers: { 'Authorization': `Bearer ${token}` },
    })
      .then(r => r.ok ? r.json() : [])
      .then((ids: string[]) => {
        if (!userClickedRef.current) {
          setBookmarked(ids.includes(postId));
        }
      })
      .catch(() => {});
  }, [token, postId]);

  if (isLoading || !post) {
    return (
      <div className="flex justify-center items-center py-20">
        <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-wanderlust-primary" />
      </div>
    );
  }

  const likesCount = localLikesCount ?? post.likesCount;
  const comments = commentsData?.pages.flatMap(p => p.content) ?? [];
  const questions = questionsData?.pages.flatMap(p => p.content) ?? [];

  const requireAuth = () => {
    navigate({ to: '/auth/login', search: { redirect: `/post/${postId}` } });
  };

  const handleLike = () => {
    if (!isAuthenticated) { requireAuth(); return; }
    const newLiked = !liked;
    setLiked(newLiked);
    setLocalLikesCount(newLiked ? likesCount + 1 : likesCount - 1);
    toggleLike.mutate(undefined, {
      onError: () => { setLiked(!newLiked); setLocalLikesCount(null); },
    });
  };

  const handleBookmark = () => {
    if (!isAuthenticated) { requireAuth(); return; }
    userClickedRef.current = true;
    const newBookmarked = !bookmarked;
    setBookmarked(newBookmarked);
    toggleBookmark.mutate({ postId: post.id, isBookmarked: !newBookmarked }, {
      onError: () => setBookmarked(!newBookmarked),
    });
  };

  const handleSubmitComment = () => {
    if (!commentText.trim()) return;
    addComment.mutate(
      { content: commentText, isQuestion: activeTab === 'qa' },
      { onSuccess: () => setCommentText('') }
    );
  };

  const canSubmitComment = commentText.trim().length > 0 && !addComment.isPending;

  return (
    <div className="max-w-3xl mx-auto px-4 py-6 pb-20 md:pb-6">
      {/* Image Carousel */}
      <ImageCarousel images={post.media} placeName={post.placeName} />

      {/* Author + Header */}
      <div className="flex items-center gap-3 mb-4">
        <Link to={`/profile/${post.user.id}`}>
          <div className="w-10 h-10 rounded-full bg-brand-100 flex items-center justify-center overflow-hidden hover:ring-2 hover:ring-brand-300 transition">
            {post.user.avatarUrl ? (
              <img src={post.user.avatarUrl} alt="" className="w-full h-full object-cover" />
            ) : (
              <span className="text-sm font-bold text-brand-700">{post.user.displayName.charAt(0).toUpperCase()}</span>
            )}
          </div>
        </Link>
        <div className="flex-1">
          <Link to={`/profile/${post.user.id}`} className="text-sm font-semibold hover:underline">
            {post.user.displayName}
          </Link>
          <p className="text-xs text-gray-500 flex items-center gap-1">
            <MapPin size={12} /> {post.placeName}, {post.countryCode}
          </p>
        </div>
        <CategoryBadge category={post.category} />
      </div>

      {/* Content */}
      <p className="text-gray-800 mb-4 leading-relaxed">{post.content}</p>

      {post.tags.length > 0 && (
        <div className="flex flex-wrap gap-1 mb-4">
          {post.tags.map(tag => (
            <span key={tag} className="text-xs text-brand-600 bg-brand-50 px-2 py-0.5 rounded-full">#{tag}</span>
          ))}
        </div>
      )}

      {/* Stats / Actions */}
      <div className="flex items-center gap-4 py-3 border-y border-gray-100 mb-4">
        <button onClick={handleLike}
          className={`flex items-center gap-1 transition ${liked ? 'text-red-500' : 'text-gray-600 hover:text-red-500'}`}>
          <Heart size={20} className={liked ? 'fill-red-500' : ''} />
          <span>{likesCount} likes</span>
        </button>
        <span className="flex items-center gap-1 text-gray-600">
          <MessageCircle size={20} /> {post.commentsCount} comments
        </span>
        {isAuthenticated && (
          <button onClick={handleBookmark} className="ml-auto flex items-center gap-1.5 transition">
            <svg width="22" height="22" viewBox="0 0 24 24"
              fill={bookmarked ? '#f59e0b' : 'none'}
              stroke={bookmarked ? '#f59e0b' : '#6b7280'}
              strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
              <path d="m19 21-7-4-7 4V5a2 2 0 0 1 2-2h10a2 2 0 0 1 2 2v16z"/>
            </svg>
            <span className={`text-sm font-medium ${bookmarked ? 'text-amber-500' : 'text-gray-500'}`}>
              {bookmarked ? 'Saved' : 'Save'}
            </span>
          </button>
        )}
      </div>

      {/* Comments / Q&A Tabs */}
      <div className="flex gap-4 mb-4 border-b border-gray-100">
        <button onClick={() => setActiveTab('comments')}
          className={`pb-2 text-sm font-medium ${activeTab === 'comments' ? 'text-wanderlust-primary border-b-2 border-wanderlust-primary' : 'text-gray-500'}`}>
          Comments ({comments.length})
        </button>
        <button onClick={() => setActiveTab('qa')}
          className={`pb-2 text-sm font-medium ${activeTab === 'qa' ? 'text-wanderlust-primary border-b-2 border-wanderlust-primary' : 'text-gray-500'}`}>
          Q&A ({questions.length})
        </button>
      </div>

      {/* Comment List */}
      <div className="space-y-3 mb-4">
        {(activeTab === 'comments' ? comments : questions).map(comment => (
          <div key={comment.id} className="flex gap-3">
            <div className="w-8 h-8 rounded-full bg-brand-100 flex items-center justify-center flex-shrink-0">
              {comment.avatarUrl ? (
                <img src={comment.avatarUrl} alt="" className="w-full h-full rounded-full object-cover" />
              ) : (
                <span className="text-xs font-medium text-brand-700">
                  {comment.displayName.charAt(0).toUpperCase()}
                </span>
              )}
            </div>
            <div>
              <p className="text-sm">
                <Link to={`/profile/${comment.userId}`} className="font-semibold hover:underline">{comment.username}</Link>{' '}
                {comment.content}
              </p>
              <p className="text-xs text-gray-400 mt-1">
                {new Date(comment.createdAt).toLocaleDateString()}
                {comment.isQuestion && <span className="ml-2 text-brand-600 font-medium">Question</span>}
              </p>
            </div>
          </div>
        ))}
      </div>

      {/* Add Comment */}
      {isAuthenticated ? (
        <div className="flex items-center gap-2">
          <input type="text" value={commentText}
            onChange={e => setCommentText(e.target.value)}
            placeholder={activeTab === 'qa' ? 'Ask a question...' : 'Add a comment...'}
            className="flex-1 border border-gray-200 rounded-lg px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-brand-300"
            onKeyDown={e => e.key === 'Enter' && canSubmitComment && handleSubmitComment()} />
          <button onClick={handleSubmitComment} disabled={!canSubmitComment}
            className="p-2 text-wanderlust-primary hover:bg-brand-50 rounded-lg disabled:opacity-30 disabled:cursor-not-allowed transition">
            <Send size={18} />
          </button>
        </div>
      ) : (
        <div className="text-center py-3">
          <Link to="/auth/login" className="text-sm text-wanderlust-primary font-medium hover:underline">
            Sign in to comment
          </Link>
        </div>
      )}
    </div>
  );
}
