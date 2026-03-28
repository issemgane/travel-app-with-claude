import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { api } from '@/lib/api';

export function useProfile(userId: string) {
  return useQuery({
    queryKey: ['user', userId],
    queryFn: () => api.getUser(userId),
    enabled: !!userId,
  });
}

export function useUserStats(userId: string) {
  return useQuery({
    queryKey: ['user', userId, 'stats'],
    queryFn: () => api.getUserStats(userId),
    enabled: !!userId,
  });
}

export function useFollow(userId: string) {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: () => api.follow(userId),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['user', userId, 'stats'] });
    },
  });
}

export function useUnfollow(userId: string) {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: () => api.unfollow(userId),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['user', userId, 'stats'] });
    },
  });
}
