import { beforeEach, describe, expect, it, vi } from 'vitest';
import { setActivePinia, createPinia } from 'pinia';
import { useAlertasStore } from '../alertas';
import { useApi } from '@/composables/useApi';

// Mock do composable useApi
vi.mock('@/composables/useApi', () => ({
    useApi: vi.fn(),
}));

const mockedUseApi = vi.mocked(useApi);

const mockAlertas = [
    { id: 1, mensagem: 'Alerta 1', dataHora: '2023-01-01T10:00:00Z', lido: true, dataLeitura: '2023-01-01T11:00:00Z' },
    { id: 2, mensagem: 'Alerta 2', dataHora: '2023-01-02T10:00:00Z', lido: false, dataLeitura: null },
];

describe('useAlertasStore', () => {
    beforeEach(() => {
        setActivePinia(createPinia());
        vi.clearAllMocks();
    });

    describe('fetchAlertas', () => {
        it('deve buscar alertas com sucesso e atualizar o estado', async () => {
            mockedUseApi.mockReturnValue({
                get: vi.fn().mockResolvedValue({ data: mockAlertas }),
                post: vi.fn(),
                put: vi.fn(),
                del: vi.fn(),
            });

            const store = useAlertasStore();

            expect(store.loading).toBe(false);
            await store.fetchAlertas();

            expect(store.loading).toBe(false);
            expect(store.items.length).toBe(2);
            expect(store.items[0].mensagem).toBe('Alerta 1');
            expect(store.items[0].dataHora).toBeInstanceOf(Date); // Verifica se a data foi convertida
            expect(store.error).toBe(null);
            expect(mockedUseApi().get).toHaveBeenCalledWith('/api/alertas');
        });

        it('deve lidar com erros durante a busca', async () => {
            mockedUseApi.mockReturnValue({
                get: vi.fn().mockRejectedValue(new Error('API Error')),
                post: vi.fn(),
                put: vi.fn(),
                del: vi.fn(),
            });

            const store = useAlertasStore();
            await store.fetchAlertas();

            expect(store.loading).toBe(false);
            expect(store.error).toBe('Falha ao buscar alertas.');
            expect(store.items.length).toBe(0);
        });
    });

    describe('marcarAlertaComoLido', () => {
        it('deve chamar a API para marcar um alerta como lido', async () => {
            const postMock = vi.fn().mockResolvedValue({});
            mockedUseApi.mockReturnValue({
                get: vi.fn(),
                post: postMock,
                put: vi.fn(),
                del: vi.fn(),
            });

            const store = useAlertasStore();
            store.items = JSON.parse(JSON.stringify(mockAlertas)).map((item: any) => ({ ...item, dataHora: new Date(item.dataHora) }));

            await store.marcarAlertaComoLido(2);

            expect(postMock).toHaveBeenCalledWith('/api/alertas/2/lido', {});
            const alerta = store.items.find(a => a.id === 2);
            expect(alerta?.lido).toBe(true);
            expect(alerta?.dataLeitura).toBeInstanceOf(Date);
        });
    });

    describe('marcarTodosAlertasComoLidos', () => {
        it('deve chamar a API para marcar todos os alertas como lidos e recarregar a lista', async () => {
            const postMock = vi.fn().mockResolvedValue({});
            const getMock = vi.fn().mockResolvedValue({ data: [] });
            mockedUseApi.mockReturnValue({
                get: getMock,
                post: postMock,
                put: vi.fn(),
                del: vi.fn(),
            });

            const store = useAlertasStore();
            await store.marcarTodosAlertasComoLidos();

            expect(postMock).toHaveBeenCalledWith('/api/alertas/marcar-todos-lidos', {});
            expect(getMock).toHaveBeenCalledWith('/api/alertas');
        });
    });

    describe('getters', () => {
        it('alertasNaoLidos deve retornar apenas os alertas não lidos', () => {
            const store = useAlertasStore();
            store.items = JSON.parse(JSON.stringify(mockAlertas)); // Popula o estado para o teste

            const naoLidos = store.alertasNaoLidos;
            expect(naoLidos.length).toBe(1);
            expect(naoLidos[0].id).toBe(2);
        });
    });
});