import {vi} from 'vitest';

export const mockPesquisarUnidade = vi.fn((sigla: string) => ({sigla, nome: `Unidade ${sigla}`}));
vi.fn(() => ({
    pesquisarUnidade: mockPesquisarUnidade,
}));
