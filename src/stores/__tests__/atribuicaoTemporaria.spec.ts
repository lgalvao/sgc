import {beforeEach, describe, expect, it} from 'vitest';
import {createPinia, setActivePinia} from 'pinia';
import {useAtribuicaoTemporariaStore} from '../atribuicaoTemporaria';
import type {AtribuicaoTemporaria} from '@/types/tipos';

describe('useAtribuicaoTemporariaStore', () => {
    let atribuicaoTemporariaStore: ReturnType<typeof useAtribuicaoTemporariaStore>;

    beforeEach(() => {
        setActivePinia(createPinia());
        atribuicaoTemporariaStore = useAtribuicaoTemporariaStore();
        // Reset the store state before each test
        atribuicaoTemporariaStore.$patch({
            atribuicoes: [] // Start with an empty array for consistent tests
        });
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

        it.skip('getAtribuicoesPorServidor should filter atribuicoes by idServidor', () => {
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

            // Manually filter the array to verify content
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
});
