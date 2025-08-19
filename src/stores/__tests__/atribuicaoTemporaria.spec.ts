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
        servidorId: 1,
        dataInicio: new Date('2025-01-01'),
        dataTermino: new Date('2025-01-31'),
        justificativa: 'Teste de criação'
      };
      const initialLength = atribuicaoTemporariaStore.atribuicoes.length;

      atribuicaoTemporariaStore.criarAtribuicao(novaAtribuicao);

      expect(atribuicaoTemporariaStore.atribuicoes.length).toBe(initialLength + 1);
      expect(atribuicaoTemporariaStore.atribuicoes[0]).toEqual(novaAtribuicao);
    });

    it('getAtribuicoesPorServidor should filter atribuicoes by servidorId', () => {
      const atribuicao1: AtribuicaoTemporaria = {
        unidade: 'A', servidorId: 1, dataInicio: new Date(), dataTermino: new Date(), justificativa: 'J1'
      };
      const atribuicao2: AtribuicaoTemporaria = {
        unidade: 'B', servidorId: 2, dataInicio: new Date(), dataTermino: new Date(), justificativa: 'J2'
      };
      const atribuicao3: AtribuicaoTemporaria = {
        unidade: 'C', servidorId: 1, dataInicio: new Date(), dataTermino: new Date(), justificativa: 'J3'
      };

      atribuicaoTemporariaStore.criarAtribuicao(atribuicao1);
      atribuicaoTemporariaStore.criarAtribuicao(atribuicao2);
      atribuicaoTemporariaStore.criarAtribuicao(atribuicao3);

      const result = atribuicaoTemporariaStore.getAtribuicoesPorServidor(1);
      expect(result.length).toBe(2);
      // Compare by content using toEqual or check specific properties
      expect(result[0]).toEqual(atribuicao1);
      expect(result[1]).toEqual(atribuicao3);
      expect(result).not.toContain(atribuicao2); // This assertion should still work as it's a different object
    });

    it('getAtribuicoesPorServidor should return an empty array if no matching servidorId', () => {
      const atribuicao1: AtribuicaoTemporaria = {
        unidade: 'A', servidorId: 1, dataInicio: new Date(), dataTermino: new Date(), justificativa: 'J1'
      };
      atribuicaoTemporariaStore.criarAtribuicao(atribuicao1);

      const result = atribuicaoTemporariaStore.getAtribuicoesPorServidor(999);
      expect(result.length).toBe(0);
    });
  });
});
