import { createRoute } from '@tanstack/react-router';
import { Route as rootRoute } from '../__root';
import { usePost } from '@/hooks/usePosts';
import { useComments, useAddComment, useQuestions } from '@/hooks/useInteractions';
import { CategoryBadge } from '@/components/post/CategoryBadge';
import { Heart, MessageCircle, MapPin, Clock, DollarSign, Send } from 'lucide-react';
import { useState } from 'react';
import { useAuth } from '@/lib/auth';

export const Route = createRoute({
  getParentRoute: () => rootRoute,
  path: '/post/$postId',
  component: PostDetailPage,
});

function PostDetailPage() {
  const { postId } = Route.useParams();
  const { data: post, isLoading } = usePost(postId);
  const { data: commentsData } = useComments(postId);
  const { data: questionsData } = useQuestions(postId);
  const addComment = useAddComment(postId);
  const { isAuthenticated } = useAuth();
  const [commentText, setCommentText] = useState('');
  const [activeTab, setActiveTab] = useState<'comments' | 'qa'>('comments');

  if (isLoading || !post) {
    return (
      <div className="flex justify-center items-center py-20">
        <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-wanderlust-primary" />
      </div>
    );
  }

  const comments = commentsData?.pages.flatMap((p) => p.content) ?? [];
  const questions = questionsData?.pages.flatMap((p) => p.content) ?? [];

  const handleSubmitComment = () => {
    if (!commentText.trim()) return;
    addComment.mutate(
      { content: commentText, isQuestion: activeTab === 'qa' },
      { onSuccess: () => setCommentText('') }
    );
  };

  return (
    <div className="max-w-3xl mx-auto px-4 py-6 pb-20 md:pb-6">
      {/* Media */}
      {post.media.length > 0 && (
        <div className="aspect-[16/9] bg-gray-100 rounded-xl overflow-hidden mb-4">
          <img src={post.media[0].mediaUrl} alt={post.placeName} className="w-full h-full object-cover" />
        </div>
      )}

      {/* Header */}
      <div className="flex items-center justify-between mb-4">
        <div>
          <h1 className="text-xl font-bold">{post.placeName}</h1>
          <p className="text-sm text-gray-500 flex items-center gap-1">
            <MapPin size={14} /> {post.countryCode}
          </p>
        </div>
        <CategoryBadge category={post.category} />
      </div>

      {/* Metadata */}
      <div className="flex items-center gap-4 text-sm text-gray-500 mb-4 flex-wrap">
        {post.costLevel && (
          <span className="flex items-center gap-1">
            <DollarSign size={14} />
            {'$'.repeat(post.costLevel)}
          </span>
        )}
        {post.bestSeason && <span>Best: {post.bestSeason}</span>}
        {post.durationSuggested && (
          <span className="flex items-center gap-1"><Clock size={14} />{post.durationSuggested}</span>
        )}
      </div>

      {/* Content */}
      <p className="text-gray-800 mb-4 leading-relaxed">{post.content}</p>

      {post.tags.length > 0 && (
        <div className="flex flex-wrap gap-1 mb-4">
          {post.tags.map((tag) => (
            <span key={tag} className="text-xs text-brand-600 bg-brand-50 px-2 py-0.5 rounded-full">#{tag}</span>
          ))}
        </div>
      )}

      {/* Stats */}
      <div className="flex items-center gap-4 py-3 border-y border-gray-100 mb-4">
        <button className="flex items-center gap-1 text-gray-600 hover:text-red-500">
          <Heart size={20} /> <span>{post.likesCount} likes</span>
        </button>
        <span className="flex items-center gap-1 text-gray-600">
          <MessageCircle size={20} /> {post.commentsCount} comments
        </span>
      </div>

      {/* Comments / Q&A Tabs */}
      <div className="flex gap-4 mb-4 border-b border-gray-100">
        <button
          onClick={() => setActiveTab('comments')}
          className={`pb-2 text-sm font-medium ${activeTab === 'comments' ? 'text-wanderlust-primary border-b-2 border-wanderlust-primary' : 'text-gray-500'}`}
        >
          Comments ({comments.length})
        </button>
        <button
          onClick={() => setActiveTab('qa')}
          className={`pb-2 text-sm font-medium ${activeTab === 'qa' ? 'text-wanderlust-primary border-b-2 border-wanderlust-primary' : 'text-gray-500'}`}
        >
          Q&A ({questions.length})
        </button>
      </div>

      {/* Comment List */}
      <div className="space-y-3 mb-4">
        {(activeTab === 'comments' ? comments : questions).map((comment) => (
          <div key={comment.id} className="flex gap-3">
            <div className="w-8 h-8 rounded-full bg-brand-100 flex items-center justify-center flex-shrink-0">
              <span className="text-xs font-medium text-brand-700">
                {comment.displayName.charAt(0).toUpperCase()}
              </span>
            </div>
            <div>
              <p className="text-sm">
                <span className="font-semibold">{comment.username}</span>{' '}
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
      {isAuthenticated && (
        <div className="flex items-center gap-2">
          <input
            type="text"
            value={commentText}
            onChange={(e) => setCommentText(e.target.value)}
            placeholder={activeTab === 'qa' ? 'Ask a question...' : 'Add a comment...'}
            className="flex-1 border border-gray-200 rounded-lg px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-brand-300"
            onKeyDown={(e) => e.key === 'Enter' && handleSubmitComment()}
          />
          <button
            onClick={handleSubmitComment}
            disabled={addComment.isPending}
            className="p-2 text-wanderlust-primary hover:bg-brand-50 rounded-lg"
          >
            <Send size={18} />
          </button>
        </div>
      )}
    </div>
  );
}
