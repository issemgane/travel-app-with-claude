import { useMutation, useQueryClient, useInfiniteQuery } from '@tanstack/react-query';
import { api } from '@/lib/api';

export function useToggleLike(postId: string) {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: () => api.toggleLike(postId),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['post', postId] });
      queryClient.invalidateQueries({ queryKey: ['feed'] });
    },
  });
}

export function useComments(postId: string) {
  return useInfiniteQuery({
    queryKey: ['comments', postId],
    queryFn: ({ pageParam = 0 }) => api.getComments(postId, pageParam),
    getNextPageParam: (lastPage) => (lastPage.last ? undefined : lastPage.page + 1),
    initialPageParam: 0,
    enabled: !!postId,
  });
}

export function useAddComment(postId: string) {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (data: { content: string; isQuestion?: boolean; parentId?: string }) =>
      api.addComment(postId, data.content, data.isQuestion, data.parentId),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['comments', postId] });
      queryClient.invalidateQueries({ queryKey: ['post', postId] });
    },
  });
}

export function useQuestions(postId: string) {
  return useInfiniteQuery({
    queryKey: ['questions', postId],
    queryFn: ({ pageParam = 0 }) => api.getQuestions(postId, pageParam),
    getNextPageParam: (lastPage) => (lastPage.last ? undefined : lastPage.page + 1),
    initialPageParam: 0,
    enabled: !!postId,
  });
}
