import { vi } from 'vitest';

export let mockTrailCrumbs: any[] = []; // Variável para o estado dos crumbs
export const mockTrailReset = vi.fn();
export const mockTrailPopTo = vi.fn();
export const mockTrailEnsureBase = vi.fn();
export const mockTrailPush = vi.fn((crumb: any) => mockTrailCrumbs.push(crumb));

export const useNavigationTrail = vi.fn(() => ({
  get crumbs() { return mockTrailCrumbs; },
  set crumbs(newCrumbs: any[]) { mockTrailCrumbs = newCrumbs; },
  reset: mockTrailReset,
  popTo: mockTrailPopTo,
  ensureBase: mockTrailEnsureBase,
  push: mockTrailPush,
}));
