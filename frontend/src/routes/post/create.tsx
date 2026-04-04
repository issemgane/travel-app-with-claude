import { createRoute, useNavigate } from '@tanstack/react-router';
import { Route as rootRoute } from '../__root';
import { useCreatePost } from '@/hooks/usePosts';
import { useState, useRef, useCallback, useEffect } from 'react';
import type { PostCategory, CreatePostRequest } from '@/types';
import { MapPin, X, Camera, ArrowRight, ArrowLeft, ImagePlus, Hash } from 'lucide-react';
import { MapContainer, TileLayer, Marker, useMapEvents } from 'react-leaflet';
import L from 'leaflet';
import 'leaflet/dist/leaflet.css';

// Fix leaflet default marker icon
delete (L.Icon.Default.prototype as any)._getIconUrl;
L.Icon.Default.mergeOptions({
  iconRetinaUrl: 'https://cdnjs.cloudflare.com/ajax/libs/leaflet/1.9.4/images/marker-icon-2x.png',
  iconUrl: 'https://cdnjs.cloudflare.com/ajax/libs/leaflet/1.9.4/images/marker-icon.png',
  shadowUrl: 'https://cdnjs.cloudflare.com/ajax/libs/leaflet/1.9.4/images/marker-shadow.png',
});

export const Route = createRoute({
  getParentRoute: () => rootRoute,
  path: '/post/create',
  component: CreatePostPage,
});

const CATEGORIES: { value: PostCategory; label: string; emoji: string }[] = [
  { value: 'SPOT', label: 'Spot', emoji: '📍' },
  { value: 'FOOD', label: 'Food', emoji: '🍜' },
  { value: 'STAY', label: 'Stay', emoji: '🏨' },
  { value: 'ACTIVITY', label: 'Activity', emoji: '🏄' },
  { value: 'TIP', label: 'Tip', emoji: '💡' },
  { value: 'WARNING', label: 'Warning', emoji: '⚠️' },
];

const COUNTRIES = [
  { code: 'AF', name: 'Afghanistan' }, { code: 'AL', name: 'Albania' }, { code: 'DZ', name: 'Algeria' },
  { code: 'AR', name: 'Argentina' }, { code: 'AU', name: 'Australia' }, { code: 'AT', name: 'Austria' },
  { code: 'BE', name: 'Belgium' }, { code: 'BR', name: 'Brazil' }, { code: 'BG', name: 'Bulgaria' },
  { code: 'KH', name: 'Cambodia' }, { code: 'CA', name: 'Canada' }, { code: 'CL', name: 'Chile' },
  { code: 'CN', name: 'China' }, { code: 'CO', name: 'Colombia' }, { code: 'HR', name: 'Croatia' },
  { code: 'CU', name: 'Cuba' }, { code: 'CZ', name: 'Czech Republic' }, { code: 'DK', name: 'Denmark' },
  { code: 'EC', name: 'Ecuador' }, { code: 'EG', name: 'Egypt' }, { code: 'FI', name: 'Finland' },
  { code: 'FR', name: 'France' }, { code: 'GE', name: 'Georgia' }, { code: 'DE', name: 'Germany' },
  { code: 'GR', name: 'Greece' }, { code: 'HU', name: 'Hungary' }, { code: 'IS', name: 'Iceland' },
  { code: 'IN', name: 'India' }, { code: 'ID', name: 'Indonesia' }, { code: 'IR', name: 'Iran' },
  { code: 'IE', name: 'Ireland' }, { code: 'IL', name: 'Israel' }, { code: 'IT', name: 'Italy' },
  { code: 'JP', name: 'Japan' }, { code: 'JO', name: 'Jordan' }, { code: 'KE', name: 'Kenya' },
  { code: 'KR', name: 'South Korea' }, { code: 'LB', name: 'Lebanon' }, { code: 'MY', name: 'Malaysia' },
  { code: 'MV', name: 'Maldives' }, { code: 'MX', name: 'Mexico' }, { code: 'MA', name: 'Morocco' },
  { code: 'MM', name: 'Myanmar' }, { code: 'NP', name: 'Nepal' }, { code: 'NL', name: 'Netherlands' },
  { code: 'NZ', name: 'New Zealand' }, { code: 'NO', name: 'Norway' }, { code: 'OM', name: 'Oman' },
  { code: 'PE', name: 'Peru' }, { code: 'PH', name: 'Philippines' }, { code: 'PL', name: 'Poland' },
  { code: 'PT', name: 'Portugal' }, { code: 'QA', name: 'Qatar' }, { code: 'RO', name: 'Romania' },
  { code: 'RU', name: 'Russia' }, { code: 'SA', name: 'Saudi Arabia' }, { code: 'SG', name: 'Singapore' },
  { code: 'ZA', name: 'South Africa' }, { code: 'ES', name: 'Spain' }, { code: 'LK', name: 'Sri Lanka' },
  { code: 'SE', name: 'Sweden' }, { code: 'CH', name: 'Switzerland' }, { code: 'TW', name: 'Taiwan' },
  { code: 'TZ', name: 'Tanzania' }, { code: 'TH', name: 'Thailand' }, { code: 'TR', name: 'Turkey' },
  { code: 'AE', name: 'UAE' }, { code: 'GB', name: 'United Kingdom' }, { code: 'US', name: 'United States' },
  { code: 'UY', name: 'Uruguay' }, { code: 'UZ', name: 'Uzbekistan' }, { code: 'VN', name: 'Vietnam' },
  { code: 'PF', name: 'French Polynesia' },
];

function MapPicker({ position, onPositionChange }: {
  position: [number, number] | null;
  onPositionChange: (lat: number, lng: number) => void;
}) {
  function ClickHandler() {
    useMapEvents({
      click(e) {
        onPositionChange(e.latlng.lat, e.latlng.lng);
      },
    });
    return null;
  }

  return (
    <MapContainer
      center={position ?? [30, 10]}
      zoom={position ? 10 : 2}
      className="h-full w-full rounded-xl"
      style={{ minHeight: '250px' }}
    >
      <TileLayer
        attribution='&copy; <a href="https://www.openstreetmap.org/copyright">OSM</a>'
        url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
      />
      <ClickHandler />
      {position && <Marker position={position} />}
    </MapContainer>
  );
}

function CreatePostPage() {
  const navigate = useNavigate();
  const createPost = useCreatePost();
  const fileInputRef = useRef<HTMLInputElement>(null);

  const [step, setStep] = useState(1);

  // Step 1 state
  const [images, setImages] = useState<{ file: File; preview: string }[]>([]);
  const [content, setContent] = useState('');
  const [category, setCategory] = useState<PostCategory | null>(null);

  // Step 2 state
  const [mapPosition, setMapPosition] = useState<[number, number] | null>(null);
  const [countrySearch, setCountrySearch] = useState('');
  const [countryCode, setCountryCode] = useState('');
  const [showCountryDropdown, setShowCountryDropdown] = useState(false);
  const [cityName, setCityName] = useState('');
  const [tags, setTags] = useState('');

  const filteredCountries = countrySearch.length > 0
    ? COUNTRIES.filter(c => c.name.toLowerCase().includes(countrySearch.toLowerCase())).slice(0, 8)
    : [];

  const handleFileSelect = (e: React.ChangeEvent<HTMLInputElement>) => {
    const files = Array.from(e.target.files ?? []);
    const newImages = files.map(file => ({
      file,
      preview: URL.createObjectURL(file),
    }));
    setImages(prev => [...prev, ...newImages].slice(0, 10));
    if (fileInputRef.current) fileInputRef.current.value = '';
  };

  const removeImage = (index: number) => {
    setImages(prev => {
      const removed = prev[index];
      URL.revokeObjectURL(removed.preview);
      return prev.filter((_, i) => i !== index);
    });
  };

  useEffect(() => {
    return () => { images.forEach(img => URL.revokeObjectURL(img.preview)); };
  }, []);

  const canGoStep2 = images.length > 0;
  const canSubmit = mapPosition !== null && countryCode !== '' && cityName.trim() !== '';

  const selectCountry = (code: string, name: string) => {
    setCountryCode(code);
    setCountrySearch(name);
    setShowCountryDropdown(false);
  };

  const handleSubmit = async () => {
    if (!mapPosition || !countryCode || !cityName.trim()) return;

    // For now, use object URLs as media URLs
    // In production you'd upload to S3/Cloudinary first
    const mediaUrls: string[] = [];
    for (const img of images) {
      // Convert to base64 data URL for the API
      const reader = new FileReader();
      const dataUrl = await new Promise<string>((resolve) => {
        reader.onload = () => resolve(reader.result as string);
        reader.readAsDataURL(img.file);
      });
      mediaUrls.push(dataUrl);
    }

    const placeName = cityName.trim();

    const data: CreatePostRequest = {
      content: content || placeName,
      category: category ?? undefined,
      latitude: mapPosition[0],
      longitude: mapPosition[1],
      placeName,
      countryCode: countryCode.toUpperCase(),
      tags: tags ? tags.split(',').map(t => t.trim()).filter(Boolean) : undefined,
      mediaItems: mediaUrls.map(url => ({ mediaUrl: url })),
    };

    createPost.mutate(data, {
      onSuccess: (post) => navigate({ to: `/post/${post.id}` }),
    });
  };

  return (
    <div className="max-w-2xl mx-auto px-4 py-6 pb-24">
      {/* Progress indicator */}
      <div className="flex items-center gap-3 mb-6">
        <div className={`flex items-center justify-center w-8 h-8 rounded-full text-sm font-bold ${step >= 1 ? 'bg-wanderlust-primary text-white' : 'bg-gray-200 text-gray-500'}`}>1</div>
        <div className={`flex-1 h-1 rounded ${step >= 2 ? 'bg-wanderlust-primary' : 'bg-gray-200'}`} />
        <div className={`flex items-center justify-center w-8 h-8 rounded-full text-sm font-bold ${step >= 2 ? 'bg-wanderlust-primary text-white' : 'bg-gray-200 text-gray-500'}`}>2</div>
      </div>

      {step === 1 && (
        <div className="space-y-5">
          <h1 className="text-xl font-bold">Share your experience</h1>

          {/* Photo Upload */}
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-2">
              <Camera size={14} className="inline mr-1" /> Photos *
            </label>

            {/* Image grid */}
            <div className="grid grid-cols-3 gap-2 mb-2">
              {images.map((img, i) => (
                <div key={i} className="relative aspect-square rounded-xl overflow-hidden bg-gray-100">
                  <img src={img.preview} alt="" className="w-full h-full object-cover" />
                  <button type="button" onClick={() => removeImage(i)}
                    className="absolute top-1.5 right-1.5 bg-black/60 rounded-full p-1 hover:bg-black/80 transition">
                    <X size={14} className="text-white" />
                  </button>
                </div>
              ))}
              {images.length < 10 && (
                <button type="button" onClick={() => fileInputRef.current?.click()}
                  className="aspect-square rounded-xl border-2 border-dashed border-gray-300 flex flex-col items-center justify-center text-gray-400 hover:border-brand-400 hover:text-brand-500 transition">
                  <ImagePlus size={24} />
                  <span className="text-xs mt-1">Add</span>
                </button>
              )}
            </div>
            <input ref={fileInputRef} type="file" accept="image/*" multiple onChange={handleFileSelect} className="hidden" />
            {images.length === 0 && (
              <p className="text-xs text-red-400">At least 1 photo required</p>
            )}
          </div>

          {/* Story / Caption */}
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">Your story</label>
            <textarea value={content} onChange={e => setContent(e.target.value)} rows={3}
              placeholder="What made this place special?"
              className="w-full border border-gray-200 rounded-xl px-3 py-2.5 text-sm focus:outline-none focus:ring-2 focus:ring-brand-300 resize-none" />
          </div>

          {/* Category (optional) */}
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-2">Category <span className="text-gray-400 font-normal">(optional)</span></label>
            <div className="flex flex-wrap gap-2">
              {CATEGORIES.map(cat => (
                <button key={cat.value} type="button" onClick={() => setCategory(prev => prev === cat.value ? null : cat.value)}
                  className={`px-3 py-1.5 rounded-full text-sm font-medium border transition ${
                    category === cat.value
                      ? 'bg-wanderlust-primary text-white border-wanderlust-primary'
                      : 'bg-white text-gray-600 border-gray-200 hover:border-brand-300'
                  }`}>
                  {cat.emoji} {cat.label}
                </button>
              ))}
            </div>
          </div>

          {/* Next button */}
          <button type="button" onClick={() => setStep(2)} disabled={!canGoStep2}
            className="w-full bg-wanderlust-primary text-white py-3 rounded-xl font-medium hover:bg-brand-800 disabled:opacity-40 disabled:cursor-not-allowed flex items-center justify-center gap-2">
            Next: Location <ArrowRight size={18} />
          </button>
        </div>
      )}

      {step === 2 && (
        <div className="space-y-5">
          <button type="button" onClick={() => setStep(1)} className="flex items-center gap-1 text-sm text-gray-500 hover:text-gray-700 mb-2">
            <ArrowLeft size={16} /> Back
          </button>
          <h1 className="text-xl font-bold">Where was this?</h1>

          {/* Map */}
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-2">
              <MapPin size={14} className="inline mr-1" /> Tap the map to set location *
            </label>
            <div className="h-64 rounded-xl overflow-hidden border border-gray-200">
              <MapPicker position={mapPosition} onPositionChange={(lat, lng) => setMapPosition([lat, lng])} />
            </div>
            {mapPosition && (
              <p className="text-xs text-gray-400 mt-1">
                {mapPosition[0].toFixed(4)}, {mapPosition[1].toFixed(4)}
              </p>
            )}
            {!mapPosition && (
              <p className="text-xs text-red-400 mt-1">Tap the map to place your pin</p>
            )}
          </div>

          {/* Country autocomplete */}
          <div className="relative">
            <label className="block text-sm font-medium text-gray-700 mb-1">Country *</label>
            <input
              type="text"
              value={countrySearch}
              onChange={e => { setCountrySearch(e.target.value); setShowCountryDropdown(true); setCountryCode(''); }}
              onFocus={() => setShowCountryDropdown(true)}
              placeholder="Start typing a country..."
              className="w-full border border-gray-200 rounded-xl px-3 py-2.5 text-sm focus:outline-none focus:ring-2 focus:ring-brand-300"
            />
            {showCountryDropdown && filteredCountries.length > 0 && (
              <div className="absolute z-50 w-full mt-1 bg-white border border-gray-200 rounded-xl shadow-lg max-h-48 overflow-y-auto">
                {filteredCountries.map(c => (
                  <button key={c.code} type="button"
                    onClick={() => selectCountry(c.code, c.name)}
                    className="w-full text-left px-3 py-2 text-sm hover:bg-brand-50 transition">
                    {c.name}
                  </button>
                ))}
              </div>
            )}
          </div>

          {/* City / Village */}
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">City / Place name *</label>
            <input type="text" value={cityName} onChange={e => setCityName(e.target.value)}
              placeholder="e.g., Santorini, Kyoto, Machu Picchu"
              className="w-full border border-gray-200 rounded-xl px-3 py-2.5 text-sm focus:outline-none focus:ring-2 focus:ring-brand-300" />
          </div>

          {/* Tags */}
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">
              <Hash size={14} className="inline mr-1" /> Tags (optional)
            </label>
            <input type="text" value={tags} onChange={e => setTags(e.target.value)}
              placeholder="sunset, hiking, street food"
              className="w-full border border-gray-200 rounded-xl px-3 py-2.5 text-sm focus:outline-none focus:ring-2 focus:ring-brand-300" />
          </div>

          {/* Submit */}
          <button type="button" onClick={handleSubmit}
            disabled={!canSubmit || createPost.isPending}
            className="w-full bg-wanderlust-primary text-white py-3 rounded-xl font-medium hover:bg-brand-800 disabled:opacity-40 disabled:cursor-not-allowed">
            {createPost.isPending ? 'Publishing...' : 'Publish'}
          </button>
        </div>
      )}
    </div>
  );
}
