import { beforeEach, describe, expect, it, vi } from 'vitest';
import { setActivePinia, createPinia } from 'pinia';
import { useAnalisesStore } from '../analises';
import { useApi } from '@/composables/useApi';
import { ResultadoAnalise } from '@/types/tipos';

// Mock do composable useApi
vi.mock('@/composables/useApi', () => ({
    useApi: vi.fn(),
}));

const mockedUseApi = vi.mocked(useApi);

const mockAnalises = [
    { id: 1, idSubprocesso: 1, dataHora: '2023-01-01T10:00:00Z', unidade: 'UNIDADE_A', resultado: 'ACEITE' },
    { id: 2, idSubprocesso: 1, dataHora: '2023-01-02T11:00:00Z', unidade: 'UNIDADE_B', resultado: 'DEVOLUCAO' },
];

describe('useAnalisesStore', () => {
    beforeEach(() => {
        setActivePinia(createPinia());
        vi.clearAllMocks();
    });

    describe('fetchAnalises', () => {
        it('deve buscar análises com sucesso e atualizar o estado', async () => {
            mockedUseApi.mockReturnValue({
                get: vi.fn().mockResolvedValue({ data: mockAnalises }),
                post: vi.fn(),
                put: vi.fn(),
                del: vi.fn(),
            });

            const store = useAnalisesStore();
            const idSubprocesso = 1;

            await store.fetchAnalises(idSubprocesso);

            expect(store.loading).toBe(false);
            expect(store.items.length).toBe(2);
            expect(store.items[0].idSubprocesso).toBe(idSubprocesso);
            expect(store.items[0].dataHora).toBeInstanceOf(Date);
            expect(store.error).toBe(null);
            expect(mockedUseApi().get).toHaveBeenCalledWith(`/api/subprocessos/${idSubprocesso}/analises`);
        });

        it('deve lidar com erros durante a busca', async () => {
            mockedUseApi.mockReturnValue({
                get: vi.fn().mockRejectedValue(new Error('API Error')),
                post: vi.fn(),
                put: vi.fn(),
                del: vi.fn(),
            });

            const store = useAnalisesStore();
            await store.fetchAnalises(1);

            expect(store.loading).toBe(false);
            expect(store.error).toBe('Falha ao buscar o histórico de análises.');
            expect(store.items.length).toBe(0);
        });
    });

    describe('registrarAnalise', () => {
        it('deve chamar a API para registrar uma análise e recarregar a lista', async () => {
            const postMock = vi.fn().mockResolvedValue({});
            const getMock = vi.fn().mockResolvedValue({ data: [] });
            mockedUseApi.mockReturnValue({
                get: getMock,
                post: postMock,
                put: vi.fn(),
                del: vi.fn(),
            });

            const store = useAnalisesStore();
            const idSubprocesso = 1;
            const novaAnalisePayload = {
                unidade: 'NOVA_UNIDADE',
                resultado: ResultadoAnalise.ACEITE,
                observacao: 'Tudo certo.'
            };

            await store.registrarAnalise(idSubprocesso, novaAnalisePayload);

            expect(postMock).toHaveBeenCalledWith(`/api/subprocessos/${idSubprocesso}/analises`, novaAnalisePayload);
            expect(getMock).toHaveBeenCalledWith(`/api/subprocessos/${idSubprocesso}/analises`);
        });

        it('deve lançar um erro em caso de falha no registro', async () => {
            mockedUseApi.mockReturnValue({
                get: vi.fn(),
                post: vi.fn().mockRejectedValue(new Error('Failed to post')),
                put: vi.fn(),
                del: vi.fn(),
            });

            const store = useAnalisesStore();
            const payload = { unidade: 'TEST', resultado: ResultadoAnalise.DEVOLUCAO, observacao: '' };

            await expect(store.registrarAnalise(1, payload)).rejects.toThrow('Falha ao registrar a análise.');
        });
    });

    describe('getters', () => {
        it('getAnalisesPorSubprocesso deve retornar análises filtradas por id', () => {
            const store = useAnalisesStore();
            store.items = [
                ...mockAnalises,
                { id: 3, idSubprocesso: 2, dataHora: '2023-01-03T12:00:00Z', unidade: 'UNIDADE_C', resultado: 'ACEITE' }
            ] as any;

            const analises = store.getAnalisesPorSubprocesso(1);
            expect(analises.length).toBe(2);
            expect(analises[0].id).toBe(1);

            const analisesOutroId = store.getAnalisesPorSubprocesso(2);
            expect(analisesOutroId.length).toBe(1);
            expect(analisesOutroId[0].id).toBe(3);
        });
    });
});