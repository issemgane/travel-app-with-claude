import { useEffect, useState, useCallback } from 'react';
import { MapContainer, TileLayer, Marker, Popup, useMapEvents } from 'react-leaflet';
import { useMapPosts } from '@/hooks/useDiscovery';
import { CategoryBadge } from '@/components/post/CategoryBadge';
import { Link } from '@tanstack/react-router';

function MapBoundsHandler({ onBoundsChange }: { onBoundsChange: (bounds: { swLat: number; swLng: number; neLat: number; neLng: number }) => void }) {
  const map = useMapEvents({
    moveend: () => {
      const b = map.getBounds();
      onBoundsChange({
        swLat: b.getSouthWest().lat,
        swLng: b.getSouthWest().lng,
        neLat: b.getNorthEast().lat,
        neLng: b.getNorthEast().lng,
      });
    },
  });

  useEffect(() => {
    const b = map.getBounds();
    onBoundsChange({
      swLat: b.getSouthWest().lat,
      swLng: b.getSouthWest().lng,
      neLat: b.getNorthEast().lat,
      neLng: b.getNorthEast().lng,
    });
  }, [map, onBoundsChange]);

  return null;
}

export function DiscoveryMap() {
  const [bounds, setBounds] = useState<{ swLat: number; swLng: number; neLat: number; neLng: number } | null>(null);
  const { data } = useMapPosts(bounds);

  const handleBoundsChange = useCallback((newBounds: { swLat: number; swLng: number; neLat: number; neLng: number }) => {
    setBounds(newBounds);
  }, []);

  const posts = data?.pages.flatMap((page) => page.content) ?? [];

  return (
    <div className="h-[calc(100vh-3.5rem)] w-full">
      <MapContainer
        center={[20, 0]}
        zoom={3}
        className="h-full w-full"
        scrollWheelZoom={true}
      >
        <TileLayer
          attribution='&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a>'
          url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
        />
        <MapBoundsHandler onBoundsChange={handleBoundsChange} />

        {posts.map((post) => (
          <Marker key={post.id} position={[post.latitude, post.longitude]}>
            <Popup>
              <div className="w-48">
                <div className="flex items-center justify-between mb-1">
                  <span className="font-semibold text-sm">{post.placeName}</span>
                  <CategoryBadge category={post.category} />
                </div>
                {post.media[0] && (
                  <img src={post.media[0].mediaUrl} alt="" className="w-full h-24 object-cover rounded mb-1" />
                )}
                <p className="text-xs text-gray-600 line-clamp-2">{post.content}</p>
                <Link to={`/post/${post.id}`} className="text-xs text-brand-600 font-medium mt-1 block">
                  View details
                </Link>
              </div>
            </Popup>
          </Marker>
        ))}
      </MapContainer>
    </div>
  );
}
