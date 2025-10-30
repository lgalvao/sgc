import {beforeEach, describe, expect, it, vi} from 'vitest';
import {initPinia} from '@/test-utils/helpers';
import {useAtribuicaoTemporariaStore} from '../atribuicoes';
import type {AtribuicaoTemporaria} from '@/types/tipos';
import {AtribuicaoTemporariaService} from "@/services/atribuicaoTemporariaService";

const mockAtribuicoes: AtribuicaoTemporaria[] = [
    {
        codigo: 1,
        unidade: { codigo: 1, nome: 'A', sigla: 'A' },
        servidor: { codigo: 1, nome: 'Servidor 1', tituloEleitoral: '123', unidade: { codigo: 1, nome: 'A', sigla: 'A' }, email: '', ramal: '' },
        dataInicio: '2025-01-01',
        dataFim: '2025-01-31',
        dataTermino: '2025-01-31',
        justificativa: 'J1'
    },
    {
        codigo: 2,
        unidade: { codigo: 2, nome: 'B', sigla: 'B' },
        servidor: { codigo: 2, nome: 'Servidor 2', tituloEleitoral: '456', unidade: { codigo: 2, nome: 'B', sigla: 'B' }, email: '', ramal: '' },
        dataInicio: '2025-02-01',
        dataFim: '2025-02-28',
        dataTermino: '2025-02-28',
        justificativa: 'J2'
    },
    {
        codigo: 3,
        unidade: { codigo: 3, nome: 'C', sigla: 'C' },
        servidor: { codigo: 1, nome: 'Servidor 1', tituloEleitoral: '123', unidade: { codigo: 3, nome: 'C', sigla: 'C' }, email: '', ramal: '' },
        dataInicio: '2025-03-01',
        dataFim: '2025-03-31',
        dataTermino: '2025-03-31',
        justificativa: 'J3'
    }
];

vi.mock('@/services/atribuicaoTemporariaService', () => ({
    AtribuicaoTemporariaService: {
        buscarTodasAtribuicoes: vi.fn(() => Promise.resolve({ data: mockAtribuicoes }))
    }
}));

describe('useAtribuicaoTemporariaStore', () => {
    let atribuicaoTemporariaStore: ReturnType<typeof useAtribuicaoTemporariaStore>;

    beforeEach(() => {
        initPinia();
        atribuicaoTemporariaStore = useAtribuicaoTemporariaStore();
        atribuicaoTemporariaStore.atribuicoes = mockAtribuicoes;
        vi.clearAllMocks();
    });

    it('should initialize with mock atribuicoes', () => {
        expect(atribuicaoTemporariaStore.atribuicoes.length).toBe(3);
        expect(atribuicaoTemporariaStore.atribuicoes[0].codigo).toBe(1);
    });

    describe('actions', () => {
        it('fetchAtribuicoes should fetch and set atribuicoes', async () => {
            atribuicaoTemporariaStore.atribuicoes = [];
            await atribuicaoTemporariaStore.fetchAtribuicoes();
            expect(AtribuicaoTemporariaService.buscarTodasAtribuicoes).toHaveBeenCalledTimes(1);
            expect(atribuicaoTemporariaStore.atribuicoes.length).toBe(3);
        });

        it('fetchAtribuicoes should handle errors', async () => {
            (AtribuicaoTemporariaService.buscarTodasAtribuicoes as any).mockRejectedValue(new Error('Failed'));
            await atribuicaoTemporariaStore.fetchAtribuicoes();
            expect(atribuicaoTemporariaStore.error).toContain('Falha ao carregar atribuições');
        });

        it('criarAtribuicao should add a new atribuicao to the store', () => {
            const novaAtribuicao: AtribuicaoTemporaria = {
                codigo: 4,
                unidade: { codigo: 4, nome: 'D', sigla: 'D' },
                servidor: { codigo: 4, nome: 'Servidor 4', tituloEleitoral: '123', unidade: { codigo: 4, nome: 'D', sigla: 'D' }, email: '', ramal: '' },
                dataInicio: '2025-04-01',
                dataFim: '2025-04-30',
                dataTermino: '2025-04-30',
                justificativa: 'J4'
            };
            const initialLength = atribuicaoTemporariaStore.atribuicoes.length;

            atribuicaoTemporariaStore.criarAtribuicao(novaAtribuicao);

            expect(atribuicaoTemporariaStore.atribuicoes.length).toBe(initialLength + 1);
            expect(atribuicaoTemporariaStore.atribuicoes[initialLength]).toEqual(novaAtribuicao);
        });
    });

    describe('getters', () => {
        it('getAtribuicoesPorServidor should return the correct atribuicoes by servidor ID', () => {
            const servidorAtribuicoes = atribuicaoTemporariaStore.getAtribuicoesPorServidor(1);
            expect(servidorAtribuicoes.length).toBe(2);
            expect(servidorAtribuicoes[0].codigo).toBe(1);
            expect(servidorAtribuicoes[1].codigo).toBe(3);
        });

        it('getAtribuicoesPorServidor should return an empty array if no matching servidor is found', () => {
            const servidorAtribuicoes = atribuicaoTemporariaStore.getAtribuicoesPorServidor(999);
            expect(servidorAtribuicoes.length).toBe(0);
        });

        it('getAtribuicoesPorUnidade should return the correct atribuicoes by unidade sigla', () => {
            const unidadeAtribuicoes = atribuicaoTemporariaStore.getAtribuicoesPorUnidade('A');
            expect(unidadeAtribuicoes.length).toBe(1);
            expect(unidadeAtribuicoes[0].codigo).toBe(1);
        });

        it('getAtribuicoesPorUnidade should return an empty array if no matching unidade is found', () => {
            const unidadeAtribuicoes = atribuicaoTemporariaStore.getAtribuicoesPorUnidade('NONEXISTENT');
            expect(unidadeAtribuicoes.length).toBe(0);
        });
    });
});
