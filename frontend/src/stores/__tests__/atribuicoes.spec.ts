import { beforeEach, describe, expect, it, vi } from 'vitest';
import { setActivePinia, createPinia } from 'pinia';
import { useAtribuicaoTemporariaStore } from '../atribuicoes';
import { useApi } from '@/composables/useApi';

vi.mock('@/composables/useApi', () => ({
    useApi: vi.fn(),
}));

const mockedUseApi = vi.mocked(useApi);

const mockAtribuicoes = [
    { id: 1, idServidor: 1, unidade: 'COSIS', dataInicio: '2023-01-01T00:00:00Z', dataTermino: '2023-01-31T00:00:00Z' },
    { id: 2, idServidor: 2, unidade: 'SESEL', dataInicio: '2023-02-01T00:00:00Z', dataTermino: '2023-02-28T00:00:00Z' },
];

describe('useAtribuicaoTemporariaStore', () => {
    beforeEach(() => {
        setActivePinia(createPinia());
        vi.clearAllMocks();
    });

    describe('fetchAtribuicoes', () => {
        it('deve buscar atribuições com sucesso', async () => {
            mockedUseApi.mockReturnValue({
                get: vi.fn().mockResolvedValue({ data: mockAtribuicoes }),
                post: vi.fn(),
                put: vi.fn(),
                del: vi.fn(),
            });

            const store = useAtribuicaoTemporariaStore();
            await store.fetchAtribuicoes();

            expect(store.loading).toBe(false);
            expect(store.items.length).toBe(2);
            expect(store.items[0].unidade).toBe('COSIS');
            expect(store.items[0].dataInicio).toBeInstanceOf(Date);
            expect(mockedUseApi().get).toHaveBeenCalledWith('/api/atribuicoes');
        });

        it('deve lidar com erros na busca', async () => {
            mockedUseApi.mockReturnValue({
                get: vi.fn().mockRejectedValue(new Error()),
                post: vi.fn(),
                put: vi.fn(),
                del: vi.fn(),
            });

            const store = useAtribuicaoTemporariaStore();
            await store.fetchAtribuicoes();

            expect(store.error).toBe('Falha ao buscar atribuições.');
        });
    });

    describe('criarAtribuicao', () => {
        it('deve criar uma atribuição e recarregar a lista', async () => {
            const postMock = vi.fn().mockResolvedValue({});
            const getMock = vi.fn().mockResolvedValue({ data: [] });
            mockedUseApi.mockReturnValue({
                get: getMock,
                post: postMock,
                put: vi.fn(),
                del: vi.fn(),
            });

            const store = useAtribuicaoTemporariaStore();
            const novaAtribuicao = { idServidor: 3, unidade: 'TESTE' };
            await store.criarAtribuicao(novaAtribuicao as any);

            expect(postMock).toHaveBeenCalledWith('/api/atribuicoes', novaAtribuicao);
            expect(getMock).toHaveBeenCalledWith('/api/atribuicoes');
        });

        it('deve lançar um erro em caso de falha', async () => {
            mockedUseApi.mockReturnValue({
                get: vi.fn(),
                post: vi.fn().mockRejectedValue(new Error()),
                put: vi.fn(),
                del: vi.fn(),
            });

            const store = useAtribuicaoTemporariaStore();
            await expect(store.criarAtribuicao({} as any)).rejects.toThrow('Falha ao criar atribuição.');
        });
    });

    describe('getters', () => {
        it('deve filtrar atribuições por servidor e unidade', () => {
            const store = useAtribuicaoTemporariaStore();
            store.items = mockAtribuicoes as any;

            expect(store.getAtribuicoesPorServidor(1).length).toBe(1);
            expect(store.getAtribuicoesPorServidor(1)[0].unidade).toBe('COSIS');
            expect(store.getAtribuicoesPorUnidade('SESEL').length).toBe(1);
            expect(store.getAtribuicoesPorUnidade('SESEL')[0].idServidor).toBe(2);
        });
    });
});