import { createRoute, useNavigate } from '@tanstack/react-router';
import { Route as rootRoute } from '../__root';
import { useCreatePost } from '@/hooks/usePosts';
import { useState, useRef, useEffect } from 'react';
import type { PostCategory, CreatePostRequest } from '@/types';
import { MapPin, X, Camera, ArrowRight, ArrowLeft, ImagePlus, Hash } from 'lucide-react';
import { MapContainer, TileLayer, Marker, useMapEvents, useMap } from 'react-leaflet';
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

// Approximate center coordinates for countries
const COUNTRY_COORDS: Record<string, [number, number]> = {
  AF: [33.93, 67.71], AL: [41.15, 20.17], DZ: [28.03, 1.66], AR: [-38.42, -63.62],
  AU: [-25.27, 133.78], AT: [47.52, 14.55], BE: [50.50, 4.47], BR: [-14.24, -51.93],
  BG: [42.73, 25.49], KH: [12.57, 104.99], CA: [56.13, -106.35], CL: [-35.68, -71.54],
  CN: [35.86, 104.20], CO: [4.57, -74.30], HR: [45.10, 15.20], CU: [21.52, -77.78],
  CZ: [49.82, 15.47], DK: [56.26, 9.50], EC: [-1.83, -78.18], EG: [26.82, 30.80],
  FI: [61.92, 25.75], FR: [46.23, 2.21], GE: [42.32, 43.36], DE: [51.17, 10.45],
  GR: [39.07, 21.82], HU: [47.16, 19.50], IS: [64.96, -19.02], IN: [20.59, 78.96],
  ID: [-0.79, 113.92], IR: [32.43, 53.69], IE: [53.14, -7.69], IL: [31.05, 34.85],
  IT: [41.87, 12.57], JP: [36.20, 138.25], JO: [30.59, 36.24], KE: [-0.02, 37.91],
  KR: [35.91, 127.77], LB: [33.85, 35.86], MY: [4.21, 101.98], MV: [3.20, 73.22],
  MX: [23.63, -102.55], MA: [31.79, -7.09], MM: [21.91, 95.96], NP: [28.39, 84.12],
  NL: [52.13, 5.29], NZ: [-40.90, 174.89], NO: [60.47, 8.47], OM: [21.47, 55.98],
  PE: [-9.19, -75.02], PH: [12.88, 121.77], PL: [51.92, 19.15], PT: [39.40, -8.22],
  QA: [25.35, 51.18], RO: [45.94, 24.97], RU: [61.52, 105.32], SA: [23.89, 45.08],
  SG: [1.35, 103.82], ZA: [-30.56, 22.94], ES: [40.46, -3.75], LK: [7.87, 80.77],
  SE: [60.13, 18.64], CH: [46.82, 8.23], TW: [23.70, 120.96], TZ: [-6.37, 34.89],
  TH: [15.87, 100.99], TR: [38.96, 35.24], AE: [23.42, 53.85], GB: [55.38, -3.44],
  US: [37.09, -95.71], UY: [-32.52, -55.77], UZ: [41.38, 64.59], VN: [14.06, 108.28],
  PF: [-17.68, -149.41],
};

function MapCenterUpdater({ center, zoom }: { center: [number, number]; zoom: number }) {
  const map = useMap();
  useEffect(() => {
    map.setView(center, zoom, { animate: true });
  }, [center[0], center[1], zoom]);
  return null;
}

function MapPicker({ position, onPositionChange, center, zoom }: {
  position: [number, number] | null;
  onPositionChange: (lat: number, lng: number) => void;
  center: [number, number];
  zoom: number;
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
      center={center}
      zoom={zoom}
      className="h-full w-full rounded-xl"
      style={{ minHeight: '250px' }}
    >
      <TileLayer
        attribution='&copy; <a href="https://www.openstreetmap.org/copyright">OSM</a>'
        url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
      />
      <MapCenterUpdater center={center} zoom={zoom} />
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

          {/* Map - centers based on country/city selection */}
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-2">
              <MapPin size={14} className="inline mr-1" /> Pinpoint the exact location *
            </label>
            <div className="h-64 rounded-xl overflow-hidden border border-gray-200">
              <MapPicker
                position={mapPosition}
                onPositionChange={(lat, lng) => setMapPosition([lat, lng])}
                center={mapPosition ?? (countryCode && COUNTRY_COORDS[countryCode] ? COUNTRY_COORDS[countryCode] : [30, 10])}
                zoom={mapPosition ? 12 : countryCode ? 6 : 2}
              />
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
