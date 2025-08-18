import { vi } from 'vitest';

export const mockRouterPush = vi.fn();
export const mockRouterResolve = vi.fn((to: any) => ({
  fullPath: typeof to === 'string' ? to : `/mock-path/${to.name}`
}));
export const mockUseRoute = vi.fn(() => ({
  path: '/mock-path', // Adicionar um valor padrão para path
  fullPath: '/mock-path', // Adicionar fullPath
  params: {},
  query: {},
  meta: {},
}));
export const mockUseRouter = vi.fn(() => ({
  push: mockRouterPush,
  resolve: mockRouterResolve,
}));

// Exportar as funções para que possam ser importadas e redefinidas nos testes
export const useRoute = mockUseRoute;
export const useRouter = mockUseRouter;