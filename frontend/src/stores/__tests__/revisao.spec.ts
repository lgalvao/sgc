import { beforeEach, describe, expect, it, vi } from 'vitest';
import { setActivePinia, createPinia } from 'pinia';
import { useRevisaoStore, TipoMudanca } from '../revisao';
import { useApi } from '@/composables/useApi';
import { useMapasStore } from '../mapas';

vi.mock('@/composables/useApi', () => ({
    useApi: vi.fn(),
}));

vi.mock('../mapas', () => ({
    useMapasStore: vi.fn(),
}));

const mockedUseApi = vi.mocked(useApi);
const mockedUseMapasStore = vi.mocked(useMapasStore);

describe('useRevisaoStore', () => {
    beforeEach(() => {
        setActivePinia(createPinia());
        vi.clearAllMocks();
    });

    describe('API Actions', () => {
        it('fetchMudancas deve buscar mudanças com sucesso', async () => {
            const mockMudancas = [{ id: 1, tipo: TipoMudanca.AtividadeAdicionada }];
            mockedUseApi.mockReturnValue({
                get: vi.fn().mockResolvedValue({ data: mockMudancas }),
                post: vi.fn(),
                put: vi.fn(),
                del: vi.fn(),
            });

            const store = useRevisaoStore();
            await store.fetchMudancas(1);

            expect(store.items).toEqual(mockMudancas);
            expect(mockedUseApi().get).toHaveBeenCalledWith('/api/revisoes/1/mudancas');
        });

        it('registrarMudanca deve postar a mudança e recarregar', async () => {
            const postMock = vi.fn().mockResolvedValue({});
            const getMock = vi.fn().mockResolvedValue({ data: [] });
            mockedUseApi.mockReturnValue({ get: getMock, post: postMock, put: vi.fn(), del: vi.fn() });

            const store = useRevisaoStore();
            const novaMudanca = { tipo: TipoMudanca.AtividadeRemovida };
            await store.registrarMudanca(1, novaMudanca);

            expect(postMock).toHaveBeenCalledWith('/api/revisoes/1/mudancas', novaMudanca);
            expect(getMock).toHaveBeenCalledWith('/api/revisoes/1/mudancas');
        });

        it('limparMudancas deve deletar e limpar o estado', async () => {
            const delMock = vi.fn().mockResolvedValue({});
            mockedUseApi.mockReturnValue({ get: vi.fn(), post: vi.fn(), put: vi.fn(), del: delMock });

            const store = useRevisaoStore();
            store.items = [{ id: 1, tipo: TipoMudanca.AtividadeAdicionada }];
            await store.limparMudancas(1);

            expect(delMock).toHaveBeenCalledWith('/api/revisoes/1/mudancas');
            expect(store.items).toEqual([]);
        });
    });

    describe('UI-related Actions', () => {
        it('setMudancasParaImpacto deve atualizar o estado', () => {
            const store = useRevisaoStore();
            const mudancas = [{ id: 1, tipo: TipoMudanca.ConhecimentoAlterado }];
            store.setMudancasParaImpacto(mudancas as any);
            expect(store.mudancasParaImpacto).toEqual(mudancas);
        });

        it('obterIdsCompetenciasImpactadas deve retornar IDs corretos', () => {
            const mockMapa = {
                competencias: [
                    { id: 101, atividadesAssociadas: [1] },
                    { id: 102, atividadesAssociadas: [2] },
                    { id: 103, atividadesAssociadas: [1, 3] },
                ],
            };
            mockedUseMapasStore.mockReturnValue({
                getMapaByUnidadeId: vi.fn().mockReturnValue(mockMapa),
            } as any);

            const store = useRevisaoStore();
            const ids = store.obterIdsCompetenciasImpactadas(1, 'TEST', 123);
            expect(ids).toEqual([101, 103]);
        });
    });
});