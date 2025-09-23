import {beforeEach, describe, expect, it, vi} from 'vitest';
import {createPinia, setActivePinia} from 'pinia';
import {useMapasStore} from '../mapas';
import type {Competencia, Mapa} from '@/types/tipos';

// Mock the mapas.json import
vi.mock('../../mocks/mapas.json', () => ({
    default: [
        {
            "id": 1,
            "unidade": "SESEL",
            "idProcesso": 1,
            "situacao": "em_andamento",
            "competencias": [
                {"id": 1, "descricao": "Acompanhamento de processos eleitorais", "atividadesAssociadas": [6]}
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
                        {"id": 1, "descricao": "Acompanhamento de processos eleitorais", "atividadesAssociadas": [6]}
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

        it('definirMapaComoVigente should set the specified map as vigente and demote existing vigente maps', () => {
            // Add a map that is already vigente
            const mapaVigente: Mapa = {
                id: 3,
                unidade: 'SESEL',
                idProcesso: 3,
                situacao: 'vigente',
                competencias: [],
                dataCriacao: new Date('2025-01-01'),
                dataDisponibilizacao: new Date('2025-01-02'),
                dataFinalizacao: new Date('2025-01-03')
            };
            mapasStore.adicionarMapa(mapaVigente);

            // Verify initial state
            const mapaVigenteInicial = mapasStore.getMapaByUnidadeId('SESEL', 3);
            expect(mapaVigenteInicial?.situacao).toBe('vigente');

            // Call definirMapaComoVigente
            mapasStore.definirMapaComoVigente('SESEL', 1);

            // Check that the previous vigente map was demoted to disponibilizado
            const mapaDemoted = mapasStore.getMapaByUnidadeId('SESEL', 3);
            expect(mapaDemoted?.situacao).toBe('disponibilizado');

            // Check that the new map is now vigente
            const mapaNovoVigente = mapasStore.getMapaByUnidadeId('SESEL', 1);
            expect(mapaNovoVigente?.situacao).toBe('vigente');
            expect(mapaNovoVigente?.dataFinalizacao).toBeInstanceOf(Date);
        });

        it('definirMapaComoVigente should handle case when no previous vigente map exists', () => {
            // SESEL has an 'em_andamento' map, not 'vigente'
            const mapaEmAndamento = mapasStore.getMapaByUnidadeId('SESEL', 1);
            expect(mapaEmAndamento?.situacao).toBe('em_andamento');

            // Call definirMapaComoVigente
            mapasStore.definirMapaComoVigente('SESEL', 1);

            // Check that the map is now vigente
            const mapaAgoraVigente = mapasStore.getMapaByUnidadeId('SESEL', 1);
            expect(mapaAgoraVigente?.situacao).toBe('vigente');
            expect(mapaAgoraVigente?.dataFinalizacao).toBeInstanceOf(Date);
        });

        it('definirMapaComoVigente should not change state if map not found', () => {
            const initialMapas = [...mapasStore.mapas];
            const initialSituacao = mapasStore.getMapaByUnidadeId('SESEL', 1)?.situacao;

            mapasStore.definirMapaComoVigente('NONEXISTENT', 999);

            // State should remain unchanged
            expect(mapasStore.mapas).toEqual(initialMapas);
            const mapaInalterado = mapasStore.getMapaByUnidadeId('SESEL', 1);
            expect(mapaInalterado?.situacao).toBe(initialSituacao);
        });
    });
});
