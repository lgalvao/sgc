import {createPinia, setActivePinia} from 'pinia';
import {beforeEach, describe, expect, it, type Mocked, vi} from 'vitest';
import {useAtividadesStore} from '../atividades';
import * as mapaService from '@/services/mapaService';
import * as atividadeService from '@/services/atividadeService';
import * as subprocessoService from '@/services/subprocessoService';
import {useNotificacoesStore} from '../notificacoes';

vi.mock('@/services/mapaService');
vi.mock('@/services/atividadeService');
vi.mock('@/services/subprocessoService');
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
        (useNotificacoesStore as Mocked<any>).mockReturnValue(mockNotificacoesStore);
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

    describe('adicionarAtividade', () => {
        it('deve adicionar uma atividade', async () => {
            const novaAtividade = { codigo: 2, descricao: 'Nova Atividade', conhecimentos: [] };
            vi.spyOn(atividadeService, 'criarAtividade').mockResolvedValue(novaAtividade);
            vi.spyOn(mapaService, 'obterMapaVisualizacao').mockResolvedValue({ competencias: [{ atividades: [novaAtividade] }] } as any);

            await store.adicionarAtividade(1, { descricao: 'Nova Atividade' });

            expect(atividadeService.criarAtividade).toHaveBeenCalledWith({ descricao: 'Nova Atividade' }, 1);
            expect(mockNotificacoesStore.sucesso).toHaveBeenCalled();
            expect(store.atividadesPorSubprocesso.get(1)).toEqual([novaAtividade]);
        });

        it('deve lidar com erros ao adicionar atividade', async () => {
            vi.spyOn(atividadeService, 'criarAtividade').mockRejectedValue(new Error('Erro'));
            await store.adicionarAtividade(1, { descricao: 'Nova Atividade' });
            expect(mockNotificacoesStore.erro).toHaveBeenCalled();
        });
    });

    describe('removerAtividade', () => {
        it('deve remover uma atividade', async () => {
            store.atividadesPorSubprocesso.set(1, [{ codigo: 1, descricao: 'Atividade Teste', conhecimentos: [] }]);
            vi.spyOn(atividadeService, 'excluirAtividade').mockResolvedValue(undefined);

            await store.removerAtividade(1, 1);

            expect(atividadeService.excluirAtividade).toHaveBeenCalledWith(1);
            expect(mockNotificacoesStore.sucesso).toHaveBeenCalled();
            expect(store.atividadesPorSubprocesso.get(1)).toEqual([]);
        });

        it('deve lidar com erros ao remover atividade', async () => {
            vi.spyOn(atividadeService, 'excluirAtividade').mockRejectedValue(new Error('Erro'));
            await store.removerAtividade(1, 1);
            expect(mockNotificacoesStore.erro).toHaveBeenCalled();
        });
    });

    describe('adicionarConhecimento', () => {
        it('deve adicionar um conhecimento', async () => {
            store.atividadesPorSubprocesso.set(1, [{ codigo: 1, descricao: 'Atividade Teste', conhecimentos: [] }]);
            const novoConhecimento = { id: 1, descricao: 'Novo Conhecimento' };
            vi.spyOn(atividadeService, 'criarConhecimento').mockResolvedValue(novoConhecimento);

            await store.adicionarConhecimento(1, 1, { descricao: 'Novo Conhecimento' });

            expect(atividadeService.criarConhecimento).toHaveBeenCalledWith(1, { descricao: 'Novo Conhecimento' });
            expect(mockNotificacoesStore.sucesso).toHaveBeenCalled();
            expect(store.atividadesPorSubprocesso.get(1)[0].conhecimentos).toEqual([novoConhecimento]);
        });
    });

    describe('removerConhecimento', () => {
        it('deve remover um conhecimento', async () => {
            store.atividadesPorSubprocesso.set(1, [{ codigo: 1, descricao: 'Atividade Teste', conhecimentos: [{ id: 1, descricao: 'Conhecimento Teste' }] }]);
            vi.spyOn(atividadeService, 'excluirConhecimento').mockResolvedValue(undefined);

            await store.removerConhecimento(1, 1, 1);

            expect(atividadeService.excluirConhecimento).toHaveBeenCalledWith(1, 1);
            expect(mockNotificacoesStore.sucesso).toHaveBeenCalled();
            expect(store.atividadesPorSubprocesso.get(1)[0].conhecimentos).toEqual([]);
        });
    });

    describe('importarAtividades', () => {
        it('deve importar atividades', async () => {
            vi.spyOn(subprocessoService, 'importarAtividades').mockResolvedValue(undefined);
            vi.spyOn(mapaService, 'obterMapaVisualizacao').mockResolvedValue({ competencias: [] } as any);

            await store.importarAtividades(1, 2);

            expect(subprocessoService.importarAtividades).toHaveBeenCalledWith(1, 2);
            expect(mockNotificacoesStore.sucesso).toHaveBeenCalled();
        });
    });

    describe('atualizarAtividade', () => {
        it('deve atualizar uma atividade', async () => {
            const atividadeAtualizada = { codigo: 1, descricao: 'Atividade Atualizada', conhecimentos: [] };
            store.atividadesPorSubprocesso.set(1, [{ codigo: 1, descricao: 'Atividade Teste', conhecimentos: [] }]);
            vi.spyOn(atividadeService, 'atualizarAtividade').mockResolvedValue(atividadeAtualizada);

            await store.atualizarAtividade(1, 1, atividadeAtualizada);

            expect(atividadeService.atualizarAtividade).toHaveBeenCalledWith(1, atividadeAtualizada);
            expect(mockNotificacoesStore.sucesso).toHaveBeenCalled();
            expect(store.atividadesPorSubprocesso.get(1)).toEqual([atividadeAtualizada]);
        });
    });

    describe('atualizarConhecimento', () => {
        it('deve atualizar um conhecimento', async () => {
            const conhecimentoAtualizado = { id: 1, descricao: 'Conhecimento Atualizado' };
            store.atividadesPorSubprocesso.set(1, [{ codigo: 1, descricao: 'Atividade Teste', conhecimentos: [{ id: 1, descricao: 'Conhecimento Teste' }] }]);
            vi.spyOn(atividadeService, 'atualizarConhecimento').mockResolvedValue(conhecimentoAtualizado);

            await store.atualizarConhecimento(1, 1, 1, conhecimentoAtualizado);

            expect(atividadeService.atualizarConhecimento).toHaveBeenCalledWith(1, 1, conhecimentoAtualizado);
            expect(mockNotificacoesStore.sucesso).toHaveBeenCalled();
            expect(store.atividadesPorSubprocesso.get(1)[0].conhecimentos).toEqual([conhecimentoAtualizado]);
        });
    });
});
