import {beforeEach, describe, expect, it, vi} from 'vitest';
import {createPinia, setActivePinia} from 'pinia';
import {useMapasStore} from '../mapas';
import * as SubprocessoService from '@/services/subprocessoService';
import {MapaAjuste, MapaCompleto, ImpactoMapa} from "@/types/tipos";

vi.mock('@/services/subprocessoService', () => ({
    obterMapaCompleto: vi.fn(),
    salvarMapa: vi.fn(),
    obterMapaAjuste: vi.fn(),
    salvarAjustes: vi.fn(),
    verificarImpactosMapa: vi.fn(),
}));

describe('useMapasStore', () => {
    let store: ReturnType<typeof useMapasStore>;
    const idSubprocesso = 1;

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
            const mockMapa: MapaCompleto = {id: 1, competencias: []};
            vi.mocked(SubprocessoService.obterMapaCompleto).mockResolvedValue(mockMapa);

            await store.fetchMapaCompleto(idSubprocesso);

            expect(SubprocessoService.obterMapaCompleto).toHaveBeenCalledWith(idSubprocesso);
            expect(store.mapaCompleto).toEqual(mockMapa);
        });

        it('should set state to null on failure', async () => {
            vi.mocked(SubprocessoService.obterMapaCompleto).mockRejectedValue(new Error('Failed'));
            store.mapaCompleto = {} as any; // Pre-set state

            await store.fetchMapaCompleto(idSubprocesso);

            expect(store.mapaCompleto).toBeNull();
        });
    });

    describe('salvarMapa', () => {
        it('should call service and update state on success', async () => {
            const request = {competencias: []};
            const mockResponse: MapaCompleto = {id: 1, competencias: [{id: 1, descricao: 'Nova'}]};
            vi.mocked(SubprocessoService.salvarMapa).mockResolvedValue(mockResponse);

            await store.salvarMapa(idSubprocesso, request);

            expect(SubprocessoService.salvarMapa).toHaveBeenCalledWith(idSubprocesso, request);
            expect(store.mapaCompleto).toEqual(mockResponse);
        });
    });

    describe('fetchMapaAjuste', () => {
        it('should call service and update state on success', async () => {
            const mockMapa: MapaAjuste = {id: 1, competenciasSugeridas: [], atividadesSugeridas: []};
            vi.mocked(SubprocessoService.obterMapaAjuste).mockResolvedValue(mockMapa);

            await store.fetchMapaAjuste(idSubprocesso);

            expect(SubprocessoService.obterMapaAjuste).toHaveBeenCalledWith(idSubprocesso);
            expect(store.mapaAjuste).toEqual(mockMapa);
        });
    });

    describe('salvarAjustes', () => {
        it('should call service successfully', async () => {
            const request = {competencias: [], atividades: []};
            vi.mocked(SubprocessoService.salvarAjustes).mockResolvedValue(undefined);

            await store.salvarAjustes(idSubprocesso, request);

            expect(SubprocessoService.salvarAjustes).toHaveBeenCalledWith(idSubprocesso, request);
        });
    });

    describe('fetchImpactoMapa', () => {
        it('should call service and update state on success', async () => {
            const mockImpacto: ImpactoMapa = { temImpacto: true, competencias: [] };
            vi.mocked(SubprocessoService.verificarImpactosMapa).mockResolvedValue(mockImpacto);

            await store.fetchImpactoMapa(idSubprocesso);

            expect(SubprocessoService.verificarImpactosMapa).toHaveBeenCalledWith(idSubprocesso);
            expect(store.impactoMapa).toEqual(mockImpacto);
        });
    });
});