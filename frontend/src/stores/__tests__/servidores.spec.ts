import { beforeEach, describe, expect, it, vi } from 'vitest';
import { setActivePinia, createPinia } from 'pinia';
import { useServidoresStore } from '../servidores';
import { useApi } from '@/composables/useApi';

vi.mock('@/composables/useApi', () => ({
    useApi: vi.fn(),
}));

const mockedUseApi = vi.mocked(useApi);

const mockServidores = [
    { id: 1, nome: 'Servidor 1', unidade: 'TEST' },
    { id: 2, nome: 'Servidor 2', unidade: 'PROD' },
];

describe('useServidoresStore', () => {
    beforeEach(() => {
        setActivePinia(createPinia());
        vi.clearAllMocks();
    });

    describe('fetchServidores', () => {
        it('deve buscar servidores com sucesso', async () => {
            mockedUseApi.mockReturnValue({
                get: vi.fn().mockResolvedValue({ data: mockServidores }),
                post: vi.fn(),
                put: vi.fn(),
                del: vi.fn(),
            });

            const store = useServidoresStore();
            await store.fetchServidores();

            expect(store.items.length).toBe(2);
            expect(store.items[0].nome).toBe('Servidor 1');
            expect(mockedUseApi().get).toHaveBeenCalledWith('/api/servidores');
        });

        it('deve lidar com erros na busca', async () => {
            mockedUseApi.mockReturnValue({
                get: vi.fn().mockRejectedValue(new Error()),
                post: vi.fn(),
                put: vi.fn(),
                del: vi.fn(),
            });

            const store = useServidoresStore();
            await store.fetchServidores();

            expect(store.error).toBe('Falha ao buscar servidores.');
        });
    });

    describe('getters', () => {
        it('getServidorById deve retornar o servidor correto', () => {
            const store = useServidoresStore();
            store.items = mockServidores as any;

            const servidor = store.getServidorById(2);
            expect(servidor).toBeDefined();
            expect(servidor?.nome).toBe('Servidor 2');
        });
    });
});