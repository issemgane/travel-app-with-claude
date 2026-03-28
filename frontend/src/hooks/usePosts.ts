import { useInfiniteQuery, useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { api } from '@/lib/api';
import type { CreatePostRequest } from '@/types';

export function useFeed() {
  return useInfiniteQuery({
    queryKey: ['feed'],
    queryFn: ({ pageParam = 0 }) => api.getFeed(pageParam),
    getNextPageParam: (lastPage) => (lastPage.last ? undefined : lastPage.page + 1),
    initialPageParam: 0,
  });
}

export function usePost(id: string) {
  return useQuery({
    queryKey: ['post', id],
    queryFn: () => api.getPost(id),
    enabled: !!id,
  });
}

export function useCreatePost() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (data: CreatePostRequest) => api.createPost(data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['feed'] });
    },
  });
}

export function useDeletePost() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (id: string) => api.deletePost(id),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['feed'] });
    },
  });
}

export function useNearbyPosts(lat: number, lng: number, radius = 10000) {
  return useInfiniteQuery({
    queryKey: ['posts', 'nearby', lat, lng, radius],
    queryFn: ({ pageParam = 0 }) => api.getNearby(lat, lng, radius, pageParam),
    getNextPageParam: (lastPage) => (lastPage.last ? undefined : lastPage.page + 1),
    initialPageParam: 0,
    enabled: !!lat && !!lng,
  });
}

export function useSearchPosts(query: string) {
  return useInfiniteQuery({
    queryKey: ['posts', 'search', query],
    queryFn: ({ pageParam = 0 }) => api.searchPosts(query, pageParam),
    getNextPageParam: (lastPage) => (lastPage.last ? undefined : lastPage.page + 1),
    initialPageParam: 0,
    enabled: query.length > 2,
  });
}

export function useDestinationPosts(countryCode: string) {
  return useInfiniteQuery({
    queryKey: ['posts', 'destination', countryCode],
    queryFn: ({ pageParam = 0 }) => api.getByDestination(countryCode, pageParam),
    getNextPageParam: (lastPage) => (lastPage.last ? undefined : lastPage.page + 1),
    initialPageParam: 0,
    enabled: !!countryCode,
  });
}
