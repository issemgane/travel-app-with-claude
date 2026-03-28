import { createRoute } from '@tanstack/react-router';
import { Route as rootRoute } from '../__root';
import { DiscoveryMap } from '@/components/map/DiscoveryMap';

export const Route = createRoute({
  getParentRoute: () => rootRoute,
  path: '/explore',
  component: ExplorePage,
});

function ExplorePage() {
  return <DiscoveryMap />;
}
