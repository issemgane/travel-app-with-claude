import type { PostCategory } from '@/types';

const CATEGORY_CONFIG: Record<PostCategory, { label: string; color: string }> = {
  SPOT: { label: 'Spot', color: 'bg-blue-100 text-blue-700' },
  FOOD: { label: 'Food', color: 'bg-orange-100 text-orange-700' },
  STAY: { label: 'Stay', color: 'bg-purple-100 text-purple-700' },
  ACTIVITY: { label: 'Activity', color: 'bg-green-100 text-green-700' },
  TIP: { label: 'Tip', color: 'bg-yellow-100 text-yellow-700' },
  WARNING: { label: 'Warning', color: 'bg-red-100 text-red-700' },
};

interface CategoryBadgeProps {
  category: PostCategory;
}

export function CategoryBadge({ category }: CategoryBadgeProps) {
  const config = CATEGORY_CONFIG[category];
  return (
    <span className={`text-xs font-medium px-2 py-1 rounded-full ${config.color}`}>
      {config.label}
    </span>
  );
}
