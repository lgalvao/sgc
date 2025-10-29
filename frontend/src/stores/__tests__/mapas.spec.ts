import {beforeEach, describe, expect, it, vi} from 'vitest';
import {createPinia, setActivePinia} from 'pinia';
import {useMapasStore} from '../mapas';
import * as mapaService from '@/services/mapaService';
import {ImpactoMapa, MapaAjuste, MapaCompleto} from "@/types/tipos";

vi.mock('@/services/mapaService', () => ({
    obterMapaCompleto: vi.fn(),
    salvarMapaCompleto: vi.fn(),
    obterMapaAjuste: vi.fn(),
    salvarMapaAjuste: vi.fn(),
    verificarImpactosMapa: vi.fn(),
}));

vi.mock('@/services/subprocessoService', () => ({
    adicionarCompetencia: vi.fn(),
    atualizarCompetencia: vi.fn(),
    removerCompetencia: vi.fn(),
}));

describe('useMapasStore', () => {
    let store: ReturnType<typeof useMapasStore>;
    const codSubrocesso = 1;

    beforeEach(() => {
        setActivePinia(createPinia());
        store = useMapasStore();
        vi.clearAllMocks();
    });

    it('should initialize with null values', () => {
        expect(store.mapaCompleto).toBeNull();
        expect(store.mapaAjuste).toBeNull();
        expect(store.impactoMapa).toBeNull();
    });

    describe('fetchMapaCompleto', () => {
        it('should call service and update state on success', async () => {
            const mockMapa: MapaCompleto = {codigo: 1, subprocessoCodigo: 1, observacoes: 'teste', competencias: []};
            vi.mocked(mapaService.obterMapaCompleto).mockResolvedValue(mockMapa);

            await store.fetchMapaCompleto(codSubrocesso);

            expect(mapaService.obterMapaCompleto).toHaveBeenCalledWith(codSubrocesso);
            expect(store.mapaCompleto).toEqual(mockMapa);
        });

        it('should set state to null on failure', async () => {
            vi.mocked(mapaService.obterMapaCompleto).mockRejectedValue(new Error('Failed'));
            store.mapaCompleto = {} as any; // Pre-set state

            await store.fetchMapaCompleto(codSubrocesso);

            expect(store.mapaCompleto).toBeNull();
        });
    });

    describe('salvarMapa', () => {
        it('should call service and update state on success', async () => {
            const request = {competencias: []};
            const mockResponse: MapaCompleto = {codigo: 1, subprocessoCodigo: 1, observacoes: 'teste', competencias: [{codigo: 1, descricao: 'Nova', atividadesAssociadas: []}]};
            vi.mocked(mapaService.salvarMapaCompleto).mockResolvedValue(mockResponse);

            await store.salvarMapa(codSubrocesso, request);

            expect(mapaService.salvarMapaCompleto).toHaveBeenCalledWith(codSubrocesso, request);
            expect(store.mapaCompleto).toEqual(mockResponse);
        });
    });

    describe('fetchMapaAjuste', () => {
        it('should call service and update state on success', async () => {
            const mockMapa: MapaAjuste = {codigo: 1, descricao: 'teste', competencias: []};
            vi.mocked(mapaService.obterMapaAjuste).mockResolvedValue(mockMapa);

            await store.fetchMapaAjuste(codSubrocesso);

            expect(mapaService.obterMapaAjuste).toHaveBeenCalledWith(codSubrocesso);
            expect(store.mapaAjuste).toEqual(mockMapa);
        });
    });

    describe('salvarAjustes', () => {
        it('should call service successfully', async () => {
            const request = {competencias: [], atividades: [], sugestoes: ''};
            vi.mocked(mapaService.salvarMapaAjuste).mockResolvedValue(undefined);

            await store.salvarAjustes(codSubrocesso, request);

            expect(mapaService.salvarMapaAjuste).toHaveBeenCalledWith(codSubrocesso, request);
        });
    });

    describe('fetchImpactoMapa', () => {
        it('should call service and update state on success', async () => {
            const mockImpacto: ImpactoMapa = {
                temImpactos: true,
                totalAtividadesInseridas: 0,
                totalAtividadesRemovidas: 0,
                totalAtividadesAlteradas: 0,
                totalCompetenciasImpactadas: 0,
                atividadesInseridas: [],
                atividadesRemovidas: [],
                atividadesAlteradas: [],
                competenciasImpactadas: []
            };
            vi.mocked(mapaService.verificarImpactosMapa).mockResolvedValue(mockImpacto);

            await store.fetchImpactoMapa(codSubrocesso);

            expect(mapaService.verificarImpactosMapa).toHaveBeenCalledWith(codSubrocesso);
            expect(store.impactoMapa).toEqual(mockImpacto);
        });
    });
});