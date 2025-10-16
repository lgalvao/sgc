import { beforeEach, describe, expect, it, vi } from 'vitest';
import { setActivePinia, createPinia } from 'pinia';
import { useConfiguracoesStore } from '../configuracoes';
import { useApi } from '@/composables/useApi';

vi.mock('@/composables/useApi', () => ({
    useApi: vi.fn(),
}));

const mockedUseApi = vi.mocked(useApi);

describe('useConfiguracoesStore', () => {
    beforeEach(() => {
        setActivePinia(createPinia());
        vi.clearAllMocks();
    });

    it('deve ter valores padrão', () => {
        const store = useConfiguracoesStore();
        expect(store.diasInativacaoProcesso).toBe(10);
        expect(store.diasAlertaNovo).toBe(7);
    });

    describe('fetchConfiguracoes', () => {
        it('deve buscar configurações com sucesso', async () => {
            const mockConfig = { diasInativacaoProcesso: 30, diasAlertaNovo: 15 };
            mockedUseApi.mockReturnValue({
                get: vi.fn().mockResolvedValue({ data: mockConfig }),
                put: vi.fn(),
                post: vi.fn(),
                del: vi.fn(),
            });

            const store = useConfiguracoesStore();
            await store.fetchConfiguracoes();

            expect(store.diasInativacaoProcesso).toBe(30);
            expect(store.diasAlertaNovo).toBe(15);
            expect(mockedUseApi().get).toHaveBeenCalledWith('/api/configuracoes');
        });

        it('deve lidar com erros na busca', async () => {
            mockedUseApi.mockReturnValue({
                get: vi.fn().mockRejectedValue(new Error()),
                put: vi.fn(),
                post: vi.fn(),
                del: vi.fn(),
            });

            const store = useConfiguracoesStore();
            await store.fetchConfiguracoes();

            expect(store.error).toBe('Falha ao carregar configurações.');
        });
    });

    describe('saveConfiguracoes', () => {
        it('deve salvar as configurações com sucesso', async () => {
            const putMock = vi.fn().mockResolvedValue({});
            mockedUseApi.mockReturnValue({
                get: vi.fn(),
                put: putMock,
                post: vi.fn(),
                del: vi.fn(),
            });

            const store = useConfiguracoesStore();
            store.diasInativacaoProcesso = 50;
            store.diasAlertaNovo = 25;

            await store.saveConfiguracoes();

            expect(putMock).toHaveBeenCalledWith('/api/configuracoes', {
                diasInativacaoProcesso: 50,
                diasAlertaNovo: 25,
            });
        });

        it('deve lançar um erro em caso de falha', async () => {
            mockedUseApi.mockReturnValue({
                get: vi.fn(),
                put: vi.fn().mockRejectedValue(new Error()),
                post: vi.fn(),
                del: vi.fn(),
            });

            const store = useConfiguracoesStore();
            await expect(store.saveConfiguracoes()).rejects.toThrow('Falha ao salvar configurações.');
        });
    });

    describe('setters', () => {
        it('não deve permitir valores menores que 1', () => {
            const store = useConfiguracoesStore();
            store.setDiasInativacaoProcesso(0);
            expect(store.diasInativacaoProcesso).toBe(10);
            store.setDiasAlertaNovo(-1);
            expect(store.diasAlertaNovo).toBe(7);
        });
    });
});