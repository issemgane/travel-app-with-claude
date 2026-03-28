import { useQuery, useInfiniteQuery } from '@tanstack/react-query';
import { api } from '@/lib/api';

export function useMapPosts(bounds: { swLat: number; swLng: number; neLat: number; neLng: number } | null) {
  return useInfiniteQuery({
    queryKey: ['map', bounds],
    queryFn: ({ pageParam = 0 }) =>
      api.getMapPosts(bounds!.swLat, bounds!.swLng, bounds!.neLat, bounds!.neLng, pageParam),
    getNextPageParam: (lastPage) => (lastPage.last ? undefined : lastPage.page + 1),
    initialPageParam: 0,
    enabled: !!bounds,
  });
}

export function useTrending() {
  return useInfiniteQuery({
    queryKey: ['trending'],
    queryFn: ({ pageParam = 0 }) => api.getTrending(pageParam),
    getNextPageParam: (lastPage) => (lastPage.last ? undefined : lastPage.page + 1),
    initialPageParam: 0,
  });
}

export function useDestination(countryCode: string) {
  return useQuery({
    queryKey: ['destination', countryCode],
    queryFn: () => api.getDestination(countryCode),
    enabled: !!countryCode,
  });
}
