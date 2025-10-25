import { createPinia, setActivePinia } from 'pinia';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import { useAtividadesStore } from '../atividades';
import * as mapaService from '@/services/mapaService';
import { useNotificacoesStore } from '../notificacoes';

vi.mock('@/services/mapaService');
vi.mock('../notificacoes');

describe('useAtividadesStore', () => {
    let store: ReturnType<typeof useAtividadesStore>;
    let mockNotificacoesStore: ReturnType<typeof useNotificacoesStore>;

    beforeEach(() => {
        setActivePinia(createPinia());
        store = useAtividadesStore();
        mockNotificacoesStore = {
            sucesso: vi.fn(),
            erro: vi.fn(),
        } as any;
        (useNotificacoesStore as vi.Mock).mockReturnValue(mockNotificacoesStore);
        vi.clearAllMocks();
    });

    describe('fetchAtividadesParaSubprocesso', () => {
        it('deve buscar e mapear as atividades', async () => {
            const mockMapaVisualizacao = {
                competencias: [
                    {
                        atividades: [
                            { codigo: 1, descricao: 'Atividade Teste', conhecimentos: [] }
                        ]
                    }
                ]
            };
            const spy = vi.spyOn(mapaService, 'obterMapaVisualizacao').mockResolvedValue(mockMapaVisualizacao as any);
            await store.fetchAtividadesParaSubprocesso(1);
            expect(spy).toHaveBeenCalledWith(1);
            const expectedAtividades = [{ codigo: 1, descricao: 'Atividade Teste', conhecimentos: [] }];
            expect(store.atividadesPorSubprocesso.get(1)).toEqual(expectedAtividades);
        });

        it('deve lidar com erros', async () => {
            vi.spyOn(mapaService, 'obterMapaVisualizacao').mockRejectedValue(new Error('Erro'));
            await store.fetchAtividadesParaSubprocesso(1);
            expect(mockNotificacoesStore.erro).toHaveBeenCalled();
        });
    });
});
