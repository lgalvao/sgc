import { describe, it, expect, beforeEach, vi } from 'vitest';
import { setActivePinia, createPinia } from 'pinia';
import { useMapasStore } from '../mapas';
import type { Mapa, Competencia } from '@/types/tipos';

// Mock the mapas.json import
vi.mock('../../mocks/mapas.json', () => ({
  default: [
    {
      "id": 1,
      "unidade": "SESEL",
      "idProcesso": 1,
      "situacao": "em_andamento",
      "competencias": [
        { "id": 1, "descricao": "Acompanhamento de processos eleitorais", "atividadesAssociadas": [6] }
      ],
      "dataCriacao": "2025-06-01",
      "dataDisponibilizacao": null,
      "dataFinalizacao": null
    },
    {
      "id": 2,
      "unidade": "COSIS",
      "idProcesso": 2,
      "situacao": "finalizado",
      "competencias": [],
      "dataCriacao": "2025-05-01",
      "dataDisponibilizacao": "2025-05-10",
      "dataFinalizacao": "2025-05-30"
    }
  ]
}));

describe('useMapasStore', () => {
  let mapasStore: ReturnType<typeof useMapasStore>;

  beforeEach(() => {
    setActivePinia(createPinia());
    mapasStore = useMapasStore();
    // Reset the store state to the mock data before each test
    // Manually reset the state based on the initial mock data, parsing dates
    mapasStore.$patch({
      mapas: [
        {
          "id": 1,
          "unidade": "SESEL",
          "idProcesso": 1,
          "situacao": "em_andamento",
          "competencias": [
            { "id": 1, "descricao": "Acompanhamento de processos eleitorais", "atividadesAssociadas": [6] }
          ],
          "dataCriacao": "2025-06-01",
          "dataDisponibilizacao": null,
          "dataFinalizacao": null
        },
        {
          "id": 2,
          "unidade": "COSIS",
          "idProcesso": 2,
          "situacao": "finalizado",
          "competencias": [],
          "dataCriacao": "2025-05-01",
          "dataDisponibilizacao": "2025-05-10",
          "dataFinalizacao": "2025-05-30"
        }
      ].map(mapa => ({
        ...mapa,
        dataCriacao: new Date(mapa.dataCriacao),
        dataDisponibilizacao: mapa.dataDisponibilizacao ? new Date(mapa.dataDisponibilizacao) : null,
        dataFinalizacao: mapa.dataFinalizacao ? new Date(mapa.dataFinalizacao) : null,
      }))
    });
  });

  it('should initialize with mock mapas and parsed dates', () => {
    expect(mapasStore.mapas.length).toBe(2); // Directly use the expected length
    expect(mapasStore.mapas[0].dataCriacao).toBeInstanceOf(Date);
    expect(mapasStore.mapas[1].dataDisponibilizacao).toBeInstanceOf(Date);
    expect(mapasStore.mapas[1].dataFinalizacao).toBeInstanceOf(Date);
  });

  describe('getters', () => {
    it('getMapaByUnidadeId should return the correct map by unidade and idProcesso', () => {
      const mapa = mapasStore.getMapaByUnidadeId('SESEL', 1);
      expect(mapa).toBeDefined();
      expect(mapa?.id).toBe(1);
      expect(mapa?.unidade).toBe('SESEL');
    });

    it('getMapaByUnidadeId should return undefined if no matching map is found', () => {
      const mapa = mapasStore.getMapaByUnidadeId('NONEXISTENT', 999);
      expect(mapa).toBeUndefined();
    });

    it('getMapaVigentePorUnidade should return the correct "em_andamento" map by unidade', () => {
      const mapa = mapasStore.getMapaVigentePorUnidade('SESEL');
      expect(mapa).toBeDefined();
      expect(mapa?.id).toBe(1);
      expect(mapa?.situacao).toBe('em_andamento');
    });

    it('getMapaVigentePorUnidade should return undefined if no "em_andamento" map is found', () => {
      const mapa = mapasStore.getMapaVigentePorUnidade('COSIS'); // COSIS map is 'finalizado'
      expect(mapa).toBeUndefined();
    });
  });

  describe('actions', () => {
    it('adicionarMapa should add a new map to the store', () => {
      const novaCompetencia: Competencia = {
        id: 10,
        descricao: "Nova Competencia",
        atividadesAssociadas: [100]
      };
      const novoMapa: Mapa = {
        id: 3,
        unidade: 'NOVA',
        idProcesso: 3,
        situacao: 'em_andamento',
        competencias: [novaCompetencia],
        dataCriacao: new Date('2025-07-01'),
        dataDisponibilizacao: null,
        dataFinalizacao: null
      };
      const initialLength = mapasStore.mapas.length;

      mapasStore.adicionarMapa(novoMapa);

      expect(mapasStore.mapas.length).toBe(initialLength + 1);
      expect(mapasStore.mapas[initialLength]).toEqual(novoMapa);
    });

    it('editarMapa should update an existing map', () => {
      const novosDados: Partial<Mapa> = {
        situacao: 'finalizado',
        dataFinalizacao: new Date('2025-07-15')
      };
      mapasStore.editarMapa(1, novosDados);

      const mapaAtualizado = mapasStore.getMapaByUnidadeId('SESEL', 1);
      expect(mapaAtualizado?.situacao).toBe('finalizado');
      expect(mapaAtualizado?.dataFinalizacao).toEqual(new Date('2025-07-15'));
    });

    it('editarMapa should not change state if map not found', () => {
      const initialMapas = [...mapasStore.mapas]; // Clone to compare
      const novosDados: Partial<Mapa> = {
        situacao: 'finalizado'
      };
      mapasStore.editarMapa(999, novosDados);
      expect(mapasStore.mapas).toEqual(initialMapas);
    });
  });
});
