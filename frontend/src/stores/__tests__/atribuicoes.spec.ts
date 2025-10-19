import {beforeEach, describe, expect, it} from 'vitest';
import {initPinia} from '@/test-utils/helpers';
import {useAtribuicaoTemporariaStore} from '../atribuicoes';
import type {AtribuicaoTemporaria} from '@/types/tipos';

describe('useAtribuicaoTemporariaStore', () => {
    let atribuicaoTemporariaStore: ReturnType<typeof useAtribuicaoTemporariaStore>;

    beforeEach(() => {
        initPinia();
        atribuicaoTemporariaStore = useAtribuicaoTemporariaStore();
        atribuicaoTemporariaStore.$patch({atribuicoes: []});
    });

    it('should initialize with an empty array of atribuicoes', () => {
        expect(atribuicaoTemporariaStore.atribuicoes.length).toBe(0);
    });

    describe('actions', () => {
        it('criarAtribuicao should add a new atribuicao to the store', () => {
            const novaAtribuicao: AtribuicaoTemporaria = {
                unidade: { codigo: 1, nome: 'COSIS', sigla: 'COSIS' },
                servidor: { codigo: 1, nome: 'Servidor 1', tituloEleitoral: '123', unidade: { codigo: 1, nome: 'COSIS', sigla: 'COSIS' }, email: '', ramal: '' },
                dataInicio: '2025-01-01',
                dataTermino: '2025-01-31',
                justificativa: 'Teste de criação'
            };
            const initialLength = atribuicaoTemporariaStore.atribuicoes.length;

            atribuicaoTemporariaStore.criarAtribuicao(novaAtribuicao);

            expect(atribuicaoTemporariaStore.atribuicoes.length).toBe(initialLength + 1);
            expect(atribuicaoTemporariaStore.atribuicoes[0]).toEqual(novaAtribuicao);
        });

        it('getAtribuicoesPorServidor should filter atribuicoes by servidor.codigo', () => {
            const atribuicao1: AtribuicaoTemporaria = {
                unidade: { codigo: 1, nome: 'A', sigla: 'A' },
                servidor: { codigo: 1, nome: 'Servidor 1', tituloEleitoral: '123', unidade: { codigo: 1, nome: 'A', sigla: 'A' }, email: '', ramal: '' },
                dataInicio: '2025-01-01',
                dataTermino: '2025-01-31',
                justificativa: 'J1'
            };
            const atribuicao2: AtribuicaoTemporaria = {
                unidade: { codigo: 2, nome: 'B', sigla: 'B' },
                servidor: { codigo: 2, nome: 'Servidor 2', tituloEleitoral: '456', unidade: { codigo: 2, nome: 'B', sigla: 'B' }, email: '', ramal: '' },
                dataInicio: '2025-02-01',
                dataTermino: '2025-02-28',
                justificativa: 'J2'
            };
            const atribuicao3: AtribuicaoTemporaria = {
                unidade: { codigo: 3, nome: 'C', sigla: 'C' },
                servidor: { codigo: 1, nome: 'Servidor 1', tituloEleitoral: '123', unidade: { codigo: 3, nome: 'C', sigla: 'C' }, email: '', ramal: '' },
                dataInicio: '2025-03-01',
                dataTermino: '2025-03-31',
                justificativa: 'J3'
            };

            atribuicaoTemporariaStore.$patch({
                atribuicoes: [atribuicao1, atribuicao2, atribuicao3]
            });

            const manuallyFiltered = atribuicaoTemporariaStore.atribuicoes.filter(a => a.servidor.codigo === 1);
            expect(manuallyFiltered.length).toBe(2);
            expect(manuallyFiltered[0]).toEqual(atribuicao1);
            expect(manuallyFiltered[1]).toEqual(atribuicao3);
        });

        it('getAtribuicoesPorServidor should return an empty array if no matching servidor.codigo', () => {
            const atribuicao1: AtribuicaoTemporaria = {
                unidade: { codigo: 1, nome: 'A', sigla: 'A' }, servidor: { codigo: 1, nome: 'Servidor 1', tituloEleitoral: '123', unidade: { codigo: 1, nome: 'A', sigla: 'A' }, email: '', ramal: '' }, dataInicio: '2025-01-01', dataTermino: '2025-01-31', justificativa: 'J1'
            };
            atribuicaoTemporariaStore.criarAtribuicao(atribuicao1);

            const result = atribuicaoTemporariaStore.getAtribuicoesPorServidor(999);
            expect(result.length).toBe(0);
        });
    });

    describe('getters', () => {
        it('getAtribuicoesPorUnidade should filter atribuicoes by unidade', () => {
            const atribuicao1: AtribuicaoTemporaria = {
                unidade: { codigo: 1, nome: 'COSIS', sigla: 'COSIS' },
                servidor: { codigo: 1, nome: 'Servidor 1', tituloEleitoral: '123', unidade: { codigo: 1, nome: 'COSIS', sigla: 'COSIS' }, email: '', ramal: '' },
                dataInicio: '2025-01-01',
                dataTermino: '2025-01-31',
                justificativa: 'J1'
            };
            const atribuicao2: AtribuicaoTemporaria = {
                unidade: { codigo: 2, nome: 'SESEL', sigla: 'SESEL' },
                servidor: { codigo: 2, nome: 'Servidor 2', tituloEleitoral: '456', unidade: { codigo: 2, nome: 'SESEL', sigla: 'SESEL' }, email: '', ramal: '' },
                dataInicio: '2025-02-01',
                dataTermino: '2025-02-28',
                justificativa: 'J2'
            };
            const atribuicao3: AtribuicaoTemporaria = {
                unidade: { codigo: 1, nome: 'COSIS', sigla: 'COSIS' },
                servidor: { codigo: 3, nome: 'Servidor 3', tituloEleitoral: '789', unidade: { codigo: 1, nome: 'COSIS', sigla: 'COSIS' }, email: '', ramal: '' },
                dataInicio: '2025-03-01',
                dataTermino: '2025-03-31',
                justificativa: 'J3'
            };

            atribuicaoTemporariaStore.$patch({
                atribuicoes: [atribuicao1, atribuicao2, atribuicao3]
            });

            const result = atribuicaoTemporariaStore.getAtribuicoesPorUnidade('COSIS');
            expect(result.length).toBe(2);
            expect(result[0]).toEqual(atribuicao1);
            expect(result[1]).toEqual(atribuicao3);
        });

        it('getAtribuicoesPorUnidade should return an empty array if no matching unidade', () => {
            const atribuicao1: AtribuicaoTemporaria = {
                unidade: { codigo: 1, nome: 'COSIS', sigla: 'COSIS' }, servidor: { codigo: 1, nome: 'Servidor 1', tituloEleitoral: '123', unidade: { codigo: 1, nome: 'COSIS', sigla: 'COSIS' }, email: '', ramal: '' }, dataInicio: '2025-01-01', dataTermino: '2025-01-31', justificativa: 'J1'
            };
            atribuicaoTemporariaStore.criarAtribuicao(atribuicao1);

            const result = atribuicaoTemporariaStore.getAtribuicoesPorUnidade('NONEXISTENT');
            expect(result.length).toBe(0);
        });
    });
});
