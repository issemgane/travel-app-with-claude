import { useQuery, useMutation, useQueryClient, useInfiniteQuery } from '@tanstack/react-query';
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

export function useFollowStatus(userId: string, enabled: boolean) {
  return useQuery({
    queryKey: ['follow', userId],
    queryFn: () => api.checkFollow(userId),
    enabled: enabled && !!userId,
  });
}

export function useFollow(userId: string) {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: () => api.follow(userId),
    onMutate: async () => {
      await queryClient.cancelQueries({ queryKey: ['follow', userId] });
      await queryClient.cancelQueries({ queryKey: ['user', userId, 'stats'] });

      const prevFollow = queryClient.getQueryData(['follow', userId]);
      const prevStats = queryClient.getQueryData(['user', userId, 'stats']);

      queryClient.setQueryData(['follow', userId], { following: true });
      queryClient.setQueryData(['user', userId, 'stats'], (old: any) =>
        old ? { ...old, followersCount: old.followersCount + 1 } : old
      );

      return { prevFollow, prevStats };
    },
    onError: (_err, _vars, context) => {
      queryClient.setQueryData(['follow', userId], context?.prevFollow);
      queryClient.setQueryData(['user', userId, 'stats'], context?.prevStats);
    },
    onSettled: () => {
      queryClient.invalidateQueries({ queryKey: ['follow', userId] });
      queryClient.invalidateQueries({ queryKey: ['user', userId, 'stats'] });
      queryClient.invalidateQueries({ queryKey: ['feed'] });
    },
  });
}

export function useUnfollow(userId: string) {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: () => api.unfollow(userId),
    onMutate: async () => {
      await queryClient.cancelQueries({ queryKey: ['follow', userId] });
      await queryClient.cancelQueries({ queryKey: ['user', userId, 'stats'] });

      const prevFollow = queryClient.getQueryData(['follow', userId]);
      const prevStats = queryClient.getQueryData(['user', userId, 'stats']);

      queryClient.setQueryData(['follow', userId], { following: false });
      queryClient.setQueryData(['user', userId, 'stats'], (old: any) =>
        old ? { ...old, followersCount: Math.max(0, old.followersCount - 1) } : old
      );

      return { prevFollow, prevStats };
    },
    onError: (_err, _vars, context) => {
      queryClient.setQueryData(['follow', userId], context?.prevFollow);
      queryClient.setQueryData(['user', userId, 'stats'], context?.prevStats);
    },
    onSettled: () => {
      queryClient.invalidateQueries({ queryKey: ['follow', userId] });
      queryClient.invalidateQueries({ queryKey: ['user', userId, 'stats'] });
      queryClient.invalidateQueries({ queryKey: ['feed'] });
    },
  });
}

export function useFollowers(userId: string) {
  return useInfiniteQuery({
    queryKey: ['user', userId, 'followers'],
    queryFn: ({ pageParam = 0 }) => api.getFollowers(userId, pageParam),
    getNextPageParam: (lastPage) => lastPage.last ? undefined : lastPage.page + 1,
    initialPageParam: 0,
    enabled: !!userId,
  });
}

export function useFollowing(userId: string) {
  return useInfiniteQuery({
    queryKey: ['user', userId, 'following'],
    queryFn: ({ pageParam = 0 }) => api.getFollowing(userId, pageParam),
    getNextPageParam: (lastPage) => lastPage.last ? undefined : lastPage.page + 1,
    initialPageParam: 0,
    enabled: !!userId,
  });
}
