import { vi } from 'vitest';

export const mockPesquisarUnidade = vi.fn((sigla: string) => ({ sigla, nome: `Unidade ${sigla}` }));

export const useUnidadesStore = vi.fn(() => ({
  pesquisarUnidade: mockPesquisarUnidade,
}));
