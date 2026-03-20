import {describe, expect, it, vi} from "vitest";
import * as subprocessoService from "@/services/subprocessoService";
import {setupStoreTest} from "@/test-utils/storeTestHelpers";
import type {ImpactoMapa, MapaAjuste, MapaCompleto, MapaVisualizacao} from "@/types/tipos";
import {useMapasStore} from "../mapas";

vi.mock("@/services/subprocessoService", () => ({
    obterMapaCompleto: vi.fn(),
    obterMapaAjuste: vi.fn(),
    verificarImpactosMapa: vi.fn(),
    obterMapaVisualizacao: vi.fn(),
}));

describe("useMapasStore", () => {
    const context = setupStoreTest(useMapasStore);
    const codSubprocesso = 1;

    it("deve inicializar com valores nulos", () => {
        expect(context.store.mapaCompleto).toBeNull();
        expect(context.store.mapaAjuste).toBeNull();
        expect(context.store.impactoMapa).toBeNull();
    });

    describe("buscarMapaCompleto", () => {
        it("deve limpar o estado anterior antes de buscar novo mapa completo", async () => {
            context.store.mapaCompleto = {codigo: 1} as any;
            vi.mocked(subprocessoService.obterMapaCompleto).mockReturnValue(new Promise(() => {
            }));

            context.store.buscarMapaCompleto(2);

        });

        it("deve chamar o serviço e atualizar o estado em caso de sucesso", async () => {
            const mockMapa: MapaCompleto = {
                codigo: 1,
                subprocessoCodigo: 1,
                observacoes: "teste",
                competencias: [],
                situacao: "EM_ANDAMENTO",
            };
            vi.mocked(subprocessoService.obterMapaCompleto).mockResolvedValue(mockMapa);

            await context.store.buscarMapaCompleto(codSubprocesso);

            expect(subprocessoService.obterMapaCompleto).toHaveBeenCalledWith(codSubprocesso);
            expect(context.store.mapaCompleto).toEqual(mockMapa);
        });

        it("deve definir o estado erro em caso de falha", async () => {
            vi.mocked(subprocessoService.obterMapaCompleto).mockRejectedValue(
                new Error("Failed"),
            );

            await context.store.buscarMapaCompleto(codSubprocesso);
            expect(context.store.erro).toBe("Failed");
        });
    });

    describe("buscarMapaAjuste", () => {
        it("deve chamar o serviço e atualizar o estado em caso de sucesso", async () => {
            const mockMapa: MapaAjuste = {
                codigo: 1,
                descricao: "teste",
                competencias: [],
            };
            vi.mocked(subprocessoService.obterMapaAjuste).mockResolvedValue(mockMapa);

            await context.store.buscarMapaAjuste(codSubprocesso);

            expect(subprocessoService.obterMapaAjuste).toHaveBeenCalledWith(codSubprocesso);
            expect(context.store.mapaAjuste).toEqual(mockMapa);
        });
    });

    describe("buscarImpactoMapa", () => {
        it("deve chamar o serviço e atualizar o estado em caso de sucesso", async () => {
            const mockImpacto: ImpactoMapa = {
                temImpactos: true,
                totalAtividadesInseridas: 0,
                totalAtividadesRemovidas: 0,
                totalAtividadesAlteradas: 0,
                totalCompetenciasImpactadas: 0,
                atividadesInseridas: [],
                atividadesRemovidas: [],
                atividadesAlteradas: [],
                competenciasImpactadas: [],
            };
            vi.mocked(subprocessoService.verificarImpactosMapa).mockResolvedValue(
                mockImpacto,
            );

            await context.store.buscarImpactoMapa(codSubprocesso);

            expect(subprocessoService.verificarImpactosMapa).toHaveBeenCalledWith(
                codSubprocesso,
            );
            expect(context.store.impactoMapa).toEqual(mockImpacto);
        });
    });


    describe("buscarMapaVisualizacao", () => {
        it("deve chamar o serviço e atualizar o estado em caso de sucesso", async () => {
            const mockMapa: MapaVisualizacao = {
                codigo: 1,
                descricao: "Teste",
                competencias: [],
            };
            vi.mocked(subprocessoService.obterMapaVisualizacao).mockResolvedValue(mockMapa);

            await context.store.buscarMapaVisualizacao(codSubprocesso);

            expect(subprocessoService.obterMapaVisualizacao).toHaveBeenCalledWith(
                codSubprocesso,
            );
            expect(context.store.mapaVisualizacao).toEqual(mockMapa);
        });
    });

});
