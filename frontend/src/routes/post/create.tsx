import { createRoute, useNavigate } from '@tanstack/react-router';
import { Route as rootRoute } from '../__root';
import { useCreatePost } from '@/hooks/usePosts';
import { useState } from 'react';
import type { PostCategory, CreatePostRequest } from '@/types';
import { MapPin, Plus, X } from 'lucide-react';

export const Route = createRoute({
  getParentRoute: () => rootRoute,
  path: '/post/create',
  component: CreatePostPage,
});

const CATEGORIES: { value: PostCategory; label: string }[] = [
  { value: 'SPOT', label: 'Spot' },
  { value: 'FOOD', label: 'Food' },
  { value: 'STAY', label: 'Stay' },
  { value: 'ACTIVITY', label: 'Activity' },
  { value: 'TIP', label: 'Tip' },
  { value: 'WARNING', label: 'Warning' },
];

function CreatePostPage() {
  const navigate = useNavigate();
  const createPost = useCreatePost();

  const [content, setContent] = useState('');
  const [category, setCategory] = useState<PostCategory>('SPOT');
  const [placeName, setPlaceName] = useState('');
  const [countryCode, setCountryCode] = useState('');
  const [latitude, setLatitude] = useState('');
  const [longitude, setLongitude] = useState('');
  const [costLevel, setCostLevel] = useState('');
  const [bestSeason, setBestSeason] = useState('');
  const [durationSuggested, setDurationSuggested] = useState('');
  const [tags, setTags] = useState('');
  const [imageUrl, setImageUrl] = useState('');
  const [imageUrls, setImageUrls] = useState<string[]>([]);

  const addImageUrl = () => {
    if (imageUrl.trim()) {
      setImageUrls((prev) => [...prev, imageUrl.trim()]);
      setImageUrl('');
    }
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();

    const data: CreatePostRequest = {
      content,
      category,
      latitude: parseFloat(latitude),
      longitude: parseFloat(longitude),
      placeName,
      countryCode: countryCode.toUpperCase(),
      costLevel: costLevel ? parseInt(costLevel) : undefined,
      bestSeason: bestSeason || undefined,
      durationSuggested: durationSuggested || undefined,
      tags: tags ? tags.split(',').map((t) => t.trim()) : undefined,
      mediaItems: imageUrls.map((url) => ({ mediaUrl: url })),
    };

    createPost.mutate(data, {
      onSuccess: (post) => navigate({ to: `/post/${post.id}` }),
    });
  };

  return (
    <div className="max-w-2xl mx-auto px-4 py-6 pb-20">
      <h1 className="text-2xl font-bold mb-6">Create Travel Card</h1>

      <form onSubmit={handleSubmit} className="space-y-5">
        {/* Image URLs */}
        <div>
          <label className="block text-sm font-medium text-gray-700 mb-2">Photo URLs *</label>
          <p className="text-xs text-gray-500 mb-2">Paste image URLs (use Imgur, Unsplash, or any image host)</p>
          <div className="flex gap-2 mb-2">
            <input type="url" value={imageUrl} onChange={(e) => setImageUrl(e.target.value)}
              placeholder="https://example.com/photo.jpg"
              className="flex-1 border border-gray-200 rounded-lg px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-brand-300" />
            <button type="button" onClick={addImageUrl}
              className="px-3 py-2 bg-brand-100 text-brand-700 rounded-lg text-sm font-medium hover:bg-brand-200">
              <Plus size={16} />
            </button>
          </div>
          <div className="flex flex-wrap gap-2">
            {imageUrls.map((url, i) => (
              <div key={i} className="relative w-20 h-20 rounded-lg overflow-hidden bg-gray-100 border">
                <img src={url} alt="" className="w-full h-full object-cover"
                  onError={(e) => { (e.target as HTMLImageElement).src = ''; }} />
                <button type="button"
                  onClick={() => setImageUrls((prev) => prev.filter((_, j) => j !== i))}
                  className="absolute top-0.5 right-0.5 bg-black/50 rounded-full p-0.5">
                  <X size={12} className="text-white" />
                </button>
              </div>
            ))}
          </div>
        </div>

        {/* Category */}
        <div>
          <label className="block text-sm font-medium text-gray-700 mb-2">Category *</label>
          <div className="flex flex-wrap gap-2">
            {CATEGORIES.map((cat) => (
              <button key={cat.value} type="button" onClick={() => setCategory(cat.value)}
                className={`px-3 py-1.5 rounded-full text-sm font-medium border transition ${
                  category === cat.value
                    ? 'bg-wanderlust-primary text-white border-wanderlust-primary'
                    : 'bg-white text-gray-600 border-gray-200 hover:border-brand-300'
                }`}>
                {cat.label}
              </button>
            ))}
          </div>
        </div>

        {/* Location */}
        <div className="grid grid-cols-2 gap-4">
          <div className="col-span-2">
            <label className="block text-sm font-medium text-gray-700 mb-1">
              <MapPin size={14} className="inline" /> Place Name *
            </label>
            <input type="text" value={placeName} onChange={(e) => setPlaceName(e.target.value)}
              placeholder="e.g., Shibuya Crossing"
              className="w-full border border-gray-200 rounded-lg px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-brand-300"
              required />
          </div>
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">Country Code *</label>
            <input type="text" value={countryCode} onChange={(e) => setCountryCode(e.target.value)}
              placeholder="JP" maxLength={3}
              className="w-full border border-gray-200 rounded-lg px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-brand-300"
              required />
          </div>
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">Latitude *</label>
            <input type="number" step="any" value={latitude} onChange={(e) => setLatitude(e.target.value)}
              placeholder="35.6595"
              className="w-full border border-gray-200 rounded-lg px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-brand-300"
              required />
          </div>
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">Longitude *</label>
            <input type="number" step="any" value={longitude} onChange={(e) => setLongitude(e.target.value)}
              placeholder="139.7004"
              className="w-full border border-gray-200 rounded-lg px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-brand-300"
              required />
          </div>
        </div>

        {/* Content */}
        <div>
          <label className="block text-sm font-medium text-gray-700 mb-1">Your Travel Story *</label>
          <textarea value={content} onChange={(e) => setContent(e.target.value)} rows={5}
            placeholder="Share your experience — what made this place special?"
            className="w-full border border-gray-200 rounded-lg px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-brand-300"
            required />
        </div>

        {/* Optional Metadata */}
        <div className="grid grid-cols-3 gap-4">
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">Cost Level</label>
            <select value={costLevel} onChange={(e) => setCostLevel(e.target.value)}
              className="w-full border border-gray-200 rounded-lg px-3 py-2 text-sm">
              <option value="">--</option>
              <option value="1">$ Budget</option>
              <option value="2">$$ Moderate</option>
              <option value="3">$$$ Mid-range</option>
              <option value="4">$$$$ Premium</option>
              <option value="5">$$$$$ Luxury</option>
            </select>
          </div>
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">Best Season</label>
            <select value={bestSeason} onChange={(e) => setBestSeason(e.target.value)}
              className="w-full border border-gray-200 rounded-lg px-3 py-2 text-sm">
              <option value="">--</option>
              <option value="Spring">Spring</option>
              <option value="Summer">Summer</option>
              <option value="Autumn">Autumn</option>
              <option value="Winter">Winter</option>
              <option value="Year-round">Year-round</option>
            </select>
          </div>
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">Duration</label>
            <input type="text" value={durationSuggested} onChange={(e) => setDurationSuggested(e.target.value)}
              placeholder="e.g., 2 hours"
              className="w-full border border-gray-200 rounded-lg px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-brand-300" />
          </div>
        </div>

        {/* Tags */}
        <div>
          <label className="block text-sm font-medium text-gray-700 mb-1">Tags (comma-separated)</label>
          <input type="text" value={tags} onChange={(e) => setTags(e.target.value)}
            placeholder="temple, photography, sunset"
            className="w-full border border-gray-200 rounded-lg px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-brand-300" />
        </div>

        <button type="submit" disabled={createPost.isPending || imageUrls.length === 0}
          className="w-full bg-wanderlust-primary text-white py-3 rounded-lg font-medium hover:bg-brand-800 disabled:opacity-50 disabled:cursor-not-allowed">
          {createPost.isPending ? 'Publishing...' : 'Publish Travel Card'}
        </button>
      </form>
    </div>
  );
}
