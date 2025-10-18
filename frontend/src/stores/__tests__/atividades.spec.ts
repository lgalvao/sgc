import { setActivePinia, createPinia } from 'pinia';
import { describe, it, expect, beforeEach, vi } from 'vitest';
import { useAtividadesStore } from '../atividades';
import * as subprocessoService from '@/services/subprocessoService';
import * as atividadeService from '@/services/atividadeService';
import { useNotificacoesStore } from '../notificacoes';
import type { MapaVisualizacao, CriarAtividadeRequest, CriarConhecimentoRequest } from '@/types/tipos';

// Mocking services
vi.mock('@/services/subprocessoService');
vi.mock('@/services/atividadeService');
vi.mock('../notificacoes', () => ({
    useNotificacoesStore: vi.fn(() => ({
        erro: vi.fn(),
        sucesso: vi.fn(),
    })),
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
                    conhecimentos: [{ codigo: 1000, descricao: 'Conhecimento 1' }],
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
        // A store de notificações já é mockada globalmente
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
        expect(atividades[1].descricao).toBe('Atividade 2');
    });

    it('adicionarAtividade should call service and reload data', async () => {
        const store = useAtividadesStore();
        const idSubprocesso = 1;
        const request: CriarAtividadeRequest = { descricao: 'Nova Atividade' };
        const novaAtividade = { codigo: 300, descricao: 'Nova Atividade', conhecimentos: [] };

        vi.spyOn(atividadeService, 'criarAtividade').mockResolvedValue(novaAtividade);
        const fetchSpy = vi.spyOn(store, 'fetchAtividadesParaSubprocesso').mockResolvedValue();

        await store.adicionarAtividade(idSubprocesso, request);

        expect(atividadeService.criarAtividade).toHaveBeenCalledWith(request);
        expect(fetchSpy).toHaveBeenCalledWith(idSubprocesso);
    });

    it('removerAtividade should call service and update state', async () => {
        const store = useAtividadesStore();
        const idSubprocesso = 1;
        const idAtividade = 100;

        // Pre-fill state
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
    });

    it('adicionarConhecimento should call service and update state', async () => {
        const store = useAtividadesStore();
        const idSubprocesso = 1;
        const idAtividade = 100;
        const request: CriarConhecimentoRequest = { descricao: 'Novo Conhecimento' };
        const novoConhecimento = { codigo: 2000, descricao: 'Novo Conhecimento' };

        // Pre-fill state
        store.atividadesPorSubprocesso.set(idSubprocesso, [
            { codigo: 100, descricao: 'Atividade 1', conhecimentos: [] },
        ]);

        vi.spyOn(atividadeService, 'criarConhecimento').mockResolvedValue(novoConhecimento);

        await store.adicionarConhecimento(idSubprocesso, idAtividade, request);

        expect(atividadeService.criarConhecimento).toHaveBeenCalledWith(idAtividade, request);
        const atividades = store.getAtividadesPorSubprocesso(idSubprocesso);
        expect(atividades[0].conhecimentos).toHaveLength(1);
        expect(atividades[0].conhecimentos[0].descricao).toBe('Novo Conhecimento');
    });

    it('removerConhecimento should call service and update state', async () => {
        const store = useAtividadesStore();
        const idSubprocesso = 1;
        const idAtividade = 100;
        const idConhecimento = 1000;

        // Pre-fill state
        store.atividadesPorSubprocesso.set(idSubprocesso, [
            {
                codigo: 100,
                descricao: 'Atividade 1',
                conhecimentos: [{ codigo: 1000, descricao: 'Conhecimento 1' }],
            },
        ]);

        vi.spyOn(atividadeService, 'excluirConhecimento').mockResolvedValue();

        await store.removerConhecimento(idSubprocesso, idAtividade, idConhecimento);

        expect(atividadeService.excluirConhecimento).toHaveBeenCalledWith(idAtividade, idConhecimento);
        const atividades = store.getAtividadesPorSubprocesso(idSubprocesso);
        expect(atividades[0].conhecimentos).toHaveLength(0);
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
    });
});