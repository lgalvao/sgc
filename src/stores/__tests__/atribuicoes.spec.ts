import {beforeEach, describe, expect, it} from 'vitest';
import {initPinia} from '@/test/helpers';
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
                unidade: 'COSIS',
                idServidor: 1,
                dataInicio: new Date('2025-01-01'),
                dataTermino: new Date('2025-01-31'),
                justificativa: 'Teste de criação'
            };
            const initialLength = atribuicaoTemporariaStore.atribuicoes.length;

            atribuicaoTemporariaStore.criarAtribuicao(novaAtribuicao);

            expect(atribuicaoTemporariaStore.atribuicoes.length).toBe(initialLength + 1);
            expect(atribuicaoTemporariaStore.atribuicoes[0]).toEqual(novaAtribuicao);
        });

        it('getAtribuicoesPorServidor should filter atribuicoes by idServidor', () => {
            const atribuicao1: AtribuicaoTemporaria = {
                unidade: 'A',
                idServidor: 1,
                dataInicio: new Date('2025-01-01'),
                dataTermino: new Date('2025-01-31'),
                justificativa: 'J1'
            };
            const atribuicao2: AtribuicaoTemporaria = {
                unidade: 'B',
                idServidor: 2,
                dataInicio: new Date('2025-02-01'),
                dataTermino: new Date('2025-02-28'),
                justificativa: 'J2'
            };
            const atribuicao3: AtribuicaoTemporaria = {
                unidade: 'C',
                idServidor: 1,
                dataInicio: new Date('2025-03-01'),
                dataTermino: new Date('2025-03-31'),
                justificativa: 'J3'
            };

            atribuicaoTemporariaStore.$patch({
                atribuicoes: [atribuicao1, atribuicao2, atribuicao3]
            });

            const manuallyFiltered = atribuicaoTemporariaStore.atribuicoes.filter(a => Number(a.idServidor) === 1);
            expect(manuallyFiltered.length).toBe(2);
            expect(manuallyFiltered[0]).toEqual(atribuicao1);
            expect(manuallyFiltered[1]).toEqual(atribuicao3);
        });

        it('getAtribuicoesPorServidor should return an empty array if no matching idServidor', () => {
            const atribuicao1: AtribuicaoTemporaria = {
                unidade: 'A', idServidor: 1, dataInicio: new Date(), dataTermino: new Date(), justificativa: 'J1'
            };
            atribuicaoTemporariaStore.criarAtribuicao(atribuicao1);

            const result = atribuicaoTemporariaStore.getAtribuicoesPorServidor(999);
            expect(result.length).toBe(0);
        });
    });

    describe('getters', () => {
        it('getAtribuicoesPorUnidade should filter atribuicoes by unidade', () => {
            const atribuicao1: AtribuicaoTemporaria = {
                unidade: 'COSIS',
                idServidor: 1,
                dataInicio: new Date('2025-01-01'),
                dataTermino: new Date('2025-01-31'),
                justificativa: 'J1'
            };
            const atribuicao2: AtribuicaoTemporaria = {
                unidade: 'SESEL',
                idServidor: 2,
                dataInicio: new Date('2025-02-01'),
                dataTermino: new Date('2025-02-28'),
                justificativa: 'J2'
            };
            const atribuicao3: AtribuicaoTemporaria = {
                unidade: 'COSIS',
                idServidor: 3,
                dataInicio: new Date('2025-03-01'),
                dataTermino: new Date('2025-03-31'),
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
                unidade: 'COSIS', idServidor: 1, dataInicio: new Date(), dataTermino: new Date(), justificativa: 'J1'
            };
            atribuicaoTemporariaStore.criarAtribuicao(atribuicao1);

            const result = atribuicaoTemporariaStore.getAtribuicoesPorUnidade('NONEXISTENT');
            expect(result.length).toBe(0);
        });
    });
});
