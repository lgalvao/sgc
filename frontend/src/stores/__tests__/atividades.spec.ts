import { setActivePinia, createPinia } from 'pinia';
import { describe, it, expect, beforeEach, vi } from 'vitest';
import { useAtividadesStore } from '../atividades';
import * as subprocessoService from '@/services/subprocessoService';
import * as atividadeService from '@/services/atividadeService';
import type { MapaVisualizacao, CriarAtividadeRequest, CriarConhecimentoRequest, Atividade, Conhecimento } from '@/types/tipos';

// Mocking services
vi.mock('@/services/subprocessoService');
vi.mock('@/services/atividadeService');

// Centralized Mock for the Store
const mockNotificacoesStore = {
    erro: vi.fn(),
    sucesso: vi.fn(),
};
vi.mock('../notificacoes', () => ({
    useNotificacoesStore: vi.fn(() => mockNotificacoesStore),
}));

const mockMapa: MapaVisualizacao = {
    codigo: 1,
    descricao: 'Mapa Teste',
    competencias: [
        {
            codigo: 10,
            descricao: 'Competencia 1',
            atividades: [
                {
                    codigo: 100,
                    descricao: 'Atividade 1',
                    conhecimentos: [{ id: 1000, descricao: 'Conhecimento 1' }],
                },
            ],
        },
        {
            codigo: 20,
            descricao: 'Competencia 2',
            atividades: [
                {
                    codigo: 200,
                    descricao: 'Atividade 2',
                    conhecimentos: [],
                },
            ],
        },
    ],
};

describe('useAtividadesStore', () => {
    beforeEach(() => {
        setActivePinia(createPinia());
        vi.clearAllMocks();
    });

    it('fetchAtividadesParaSubprocesso should fetch and map activities', async () => {
        const store = useAtividadesStore();
        const idSubprocesso = 1;
        vi.spyOn(subprocessoService, 'obterMapaVisualizacao').mockResolvedValue(mockMapa);
        await store.fetchAtividadesParaSubprocesso(idSubprocesso);
        expect(subprocessoService.obterMapaVisualizacao).toHaveBeenCalledWith(idSubprocesso);
        const atividades = store.getAtividadesPorSubprocesso(idSubprocesso);
        expect(atividades).toHaveLength(2);
        expect(atividades[0].descricao).toBe('Atividade 1');
        expect(atividades[0].conhecimentos).toHaveLength(1);
    });

    it('adicionarAtividade should call service and reload data', async () => {
        const store = useAtividadesStore();
        const idSubprocesso = 1;
        const request: CriarAtividadeRequest = { descricao: 'Nova Atividade' };
        const novaAtividade: Atividade = { codigo: 300, descricao: 'Nova Atividade', conhecimentos: [] };
        vi.spyOn(atividadeService, 'criarAtividade').mockResolvedValue(novaAtividade);
        const fetchSpy = vi.spyOn(store, 'fetchAtividadesParaSubprocesso').mockResolvedValue();
        await store.adicionarAtividade(idSubprocesso, request);
        expect(atividadeService.criarAtividade).toHaveBeenCalledWith(request, idSubprocesso);
        expect(fetchSpy).toHaveBeenCalledWith(idSubprocesso);
        expect(mockNotificacoesStore.sucesso).toHaveBeenCalledWith('Atividade adicionada', 'A nova atividade foi adicionada com sucesso.');
    });

    it('removerAtividade should call service and update state', async () => {
        const store = useAtividadesStore();
        const idSubprocesso = 1;
        const idAtividade = 100;
        store.atividadesPorSubprocesso.set(idSubprocesso, [
            { codigo: 100, descricao: 'Atividade 1', conhecimentos: [] },
            { codigo: 200, descricao: 'Atividade 2', conhecimentos: [] },
        ]);
        vi.spyOn(atividadeService, 'excluirAtividade').mockResolvedValue();
        await store.removerAtividade(idSubprocesso, idAtividade);
        expect(atividadeService.excluirAtividade).toHaveBeenCalledWith(idAtividade);
        const atividades = store.getAtividadesPorSubprocesso(idSubprocesso);
        expect(atividades).toHaveLength(1);
        expect(atividades[0].codigo).toBe(200);
        expect(mockNotificacoesStore.sucesso).toHaveBeenCalledWith('Atividade removida', 'A atividade foi removida com sucesso.');
    });

    it('adicionarConhecimento should call service and update state', async () => {
        const store = useAtividadesStore();
        const idSubprocesso = 1;
        const idAtividade = 100;
        const request: CriarConhecimentoRequest = { descricao: 'Novo Conhecimento' };
        const novoConhecimento: Conhecimento = { id: 2000, descricao: 'Novo Conhecimento' };
        store.atividadesPorSubprocesso.set(idSubprocesso, [
            { codigo: 100, descricao: 'Atividade 1', conhecimentos: [] },
        ]);
        vi.spyOn(atividadeService, 'criarConhecimento').mockResolvedValue(novoConhecimento);
        await store.adicionarConhecimento(idSubprocesso, idAtividade, request);
        expect(atividadeService.criarConhecimento).toHaveBeenCalledWith(idAtividade, request);
        const atividades = store.getAtividadesPorSubprocesso(idSubprocesso);
        expect(atividades[0].conhecimentos).toHaveLength(1);
        expect(atividades[0].conhecimentos[0].descricao).toBe('Novo Conhecimento');
        expect(mockNotificacoesStore.sucesso).toHaveBeenCalledWith('Conhecimento adicionado', 'O novo conhecimento foi adicionado com sucesso.');
    });

    it('removerConhecimento should call service and update state', async () => {
        const store = useAtividadesStore();
        const idSubprocesso = 1;
        const idAtividade = 100;
        const idConhecimento = 1000;
        store.atividadesPorSubprocesso.set(idSubprocesso, [
            {
                codigo: 100,
                descricao: 'Atividade 1',
                conhecimentos: [{ id: 1000, descricao: 'Conhecimento 1' }],
            },
        ]);
        vi.spyOn(atividadeService, 'excluirConhecimento').mockResolvedValue();
        await store.removerConhecimento(idSubprocesso, idAtividade, idConhecimento);
        expect(atividadeService.excluirConhecimento).toHaveBeenCalledWith(idAtividade, idConhecimento);
        const atividades = store.getAtividadesPorSubprocesso(idSubprocesso);
        expect(atividades[0].conhecimentos).toHaveLength(0);
        expect(mockNotificacoesStore.sucesso).toHaveBeenCalledWith('Conhecimento removido', 'O conhecimento foi removido com sucesso.');
    });

    it('importarAtividades should call service and reload data', async () => {
        const store = useAtividadesStore();
        const idSubprocessoDestino = 1;
        const idSubprocessoOrigem = 2;
        vi.spyOn(subprocessoService, 'importarAtividades').mockResolvedValue();
        const fetchSpy = vi.spyOn(store, 'fetchAtividadesParaSubprocesso').mockResolvedValue();
        await store.importarAtividades(idSubprocessoDestino, idSubprocessoOrigem);
        expect(subprocessoService.importarAtividades).toHaveBeenCalledWith(idSubprocessoDestino, idSubprocessoOrigem);
        expect(fetchSpy).toHaveBeenCalledWith(idSubprocessoDestino);
        expect(mockNotificacoesStore.sucesso).toHaveBeenCalledWith('Atividades importadas', 'As atividades foram importadas com sucesso.');
    });

    // Error Handling Tests
    it('fetchAtividadesParaSubprocesso should handle errors', async () => {
        const store = useAtividadesStore();
        vi.spyOn(subprocessoService, 'obterMapaVisualizacao').mockRejectedValue(new Error('Failed'));
        await store.fetchAtividadesParaSubprocesso(1);
        expect(mockNotificacoesStore.erro).toHaveBeenCalledWith('Erro ao buscar atividades', 'Não foi possível carregar as atividades do subprocesso.');
    });

    it('adicionarAtividade should handle errors', async () => {
        const store = useAtividadesStore();
        vi.spyOn(atividadeService, 'criarAtividade').mockRejectedValue(new Error('Failed'));
        await store.adicionarAtividade(1, { descricao: 'test' });
        expect(mockNotificacoesStore.erro).toHaveBeenCalledWith('Erro ao adicionar atividade', 'Não foi possível salvar a nova atividade.');
    });

    it('removerAtividade should handle errors', async () => {
        const store = useAtividadesStore();
        vi.spyOn(atividadeService, 'excluirAtividade').mockRejectedValue(new Error('Failed'));
        await store.removerAtividade(1, 100);
        expect(mockNotificacoesStore.erro).toHaveBeenCalledWith('Erro ao remover atividade', 'Não foi possível remover a atividade.');
    });

    it('adicionarConhecimento should handle errors', async () => {
        const store = useAtividadesStore();
        vi.spyOn(atividadeService, 'criarConhecimento').mockRejectedValue(new Error('Failed'));
        await store.adicionarConhecimento(1, 100, { descricao: 'test' });
        expect(mockNotificacoesStore.erro).toHaveBeenCalledWith('Erro ao adicionar conhecimento', 'Não foi possível salvar o novo conhecimento.');
    });

    it('adicionarConhecimento should not update local state if atividade not found', async () => {
        const store = useAtividadesStore();
        const request: CriarConhecimentoRequest = { descricao: 'Novo Conhecimento' };
        const novoConhecimento: Conhecimento = { id: 2000, descricao: 'Novo Conhecimento' };
        vi.spyOn(atividadeService, 'criarConhecimento').mockResolvedValue(novoConhecimento);
        await store.adicionarConhecimento(1, 999, request); // 999 = Non-existent ID
        expect(atividadeService.criarConhecimento).toHaveBeenCalledWith(999, request);
        expect(mockNotificacoesStore.sucesso).toHaveBeenCalled();
        const atividades = store.getAtividadesPorSubprocesso(1);
        expect(atividades).toHaveLength(0);
    });

    it('removerConhecimento should handle errors', async () => {
        const store = useAtividadesStore();
        vi.spyOn(atividadeService, 'excluirConhecimento').mockRejectedValue(new Error('Failed'));
        await store.removerConhecimento(1, 100, 1000);
        expect(mockNotificacoesStore.erro).toHaveBeenCalledWith('Erro ao remover conhecimento', 'Não foi possível remover o conhecimento.');
    });

    it('removerConhecimento should not update local state if atividade is not found', async () => {
        const store = useAtividadesStore();
        vi.spyOn(atividadeService, 'excluirConhecimento').mockResolvedValue();
        await store.removerConhecimento(1, 999, 1000); // 999 = Non-existent ID
        expect(atividadeService.excluirConhecimento).toHaveBeenCalledWith(999, 1000);
        const atividades = store.getAtividadesPorSubprocesso(1);
        expect(atividades).toHaveLength(0);
        expect(mockNotificacoesStore.sucesso).toHaveBeenCalled();
    });

    it('importarAtividades should handle errors', async () => {
        const store = useAtividadesStore();
        vi.spyOn(subprocessoService, 'importarAtividades').mockRejectedValue(new Error('Failed'));
        await store.importarAtividades(1, 2);
        expect(mockNotificacoesStore.erro).toHaveBeenCalledWith('Erro ao importar', 'Não foi possível importar as atividades.');
    });

    describe('atualizarAtividade', () => {
        it('should call service and update state on success', async () => {
            const store = useAtividadesStore();
            const atividadeAtualizada: Atividade = { codigo: 100, descricao: 'Atividade Atualizada', conhecimentos: [] };
            store.atividadesPorSubprocesso.set(1, [{ codigo: 100, descricao: 'Original', conhecimentos: [] }]);
            vi.spyOn(atividadeService, 'atualizarAtividade').mockResolvedValue(atividadeAtualizada);
            await store.atualizarAtividade(1, 100, atividadeAtualizada);
            expect(atividadeService.atualizarAtividade).toHaveBeenCalledWith(100, atividadeAtualizada);
            expect(store.getAtividadesPorSubprocesso(1)[0].descricao).toBe('Atividade Atualizada');
            expect(mockNotificacoesStore.sucesso).toHaveBeenCalled();
        });

        it('should show error notification on failure', async () => {
            const store = useAtividadesStore();
            vi.spyOn(atividadeService, 'atualizarAtividade').mockRejectedValue(new Error('Failed'));
            await store.atualizarAtividade(1, 100, {} as any);
            expect(mockNotificacoesStore.erro).toHaveBeenCalledWith('Erro ao atualizar', 'Não foi possível atualizar a atividade.');
        });
    });

    describe('atualizarConhecimento', () => {
        it('should call service and update state on success', async () => {
            const store = useAtividadesStore();
            const conhecimentoAtualizado: Conhecimento = { id: 1000, descricao: 'Conhecimento Atualizado' };
            store.atividadesPorSubprocesso.set(1, [{ codigo: 100, descricao: 'Atividade', conhecimentos: [{ id: 1000, descricao: 'Original' }] }]);
            vi.spyOn(atividadeService, 'atualizarConhecimento').mockResolvedValue(conhecimentoAtualizado);
            await store.atualizarConhecimento(1, 100, 1000, conhecimentoAtualizado);
            expect(atividadeService.atualizarConhecimento).toHaveBeenCalledWith(100, 1000, conhecimentoAtualizado);
            expect(store.getAtividadesPorSubprocesso(1)[0].conhecimentos[0].descricao).toBe('Conhecimento Atualizado');
            expect(mockNotificacoesStore.sucesso).toHaveBeenCalled();
        });

        it('should not update state if atividade is not found', async () => {
            const store = useAtividadesStore();
            const conhecimentoAtualizado: Conhecimento = { id: 1000, descricao: 'Conhecimento Atualizado' };
            vi.spyOn(atividadeService, 'atualizarConhecimento').mockResolvedValue(conhecimentoAtualizado);
            await store.atualizarConhecimento(1, 999, 1000, conhecimentoAtualizado);
            expect(store.getAtividadesPorSubprocesso(1)).toEqual([]);
            expect(mockNotificacoesStore.sucesso).toHaveBeenCalled();
        });

        it('should show error notification on failure', async () => {
            const store = useAtividadesStore();
            vi.spyOn(atividadeService, 'atualizarConhecimento').mockRejectedValue(new Error('Failed'));
            await store.atualizarConhecimento(1, 100, 1000, {} as any);
            expect(mockNotificacoesStore.erro).toHaveBeenCalledWith('Erro ao atualizar', 'Não foi possível atualizar o conhecimento.');
        });
    });
});