import {createPinia, setActivePinia} from "pinia";
import {beforeEach, describe, expect, it, vi} from "vitest";
import * as mapaService from "@/services/mapaService";
import * as subprocessoService from "@/services/subprocessoService";
import type {ImpactoMapa, MapaAjuste, MapaCompleto, MapaVisualizacao} from "@/types/tipos";
import {useMapasStore} from "../mapas";

vi.mock("@/services/mapaService", () => ({
    obterMapaCompleto: vi.fn(),
    salvarMapaCompleto: vi.fn(),
    obterMapaAjuste: vi.fn(),
    salvarMapaAjuste: vi.fn(),
    verificarImpactosMapa: vi.fn(),
    obterMapaVisualizacao: vi.fn(),
    disponibilizarMapa: vi.fn(),
}));

vi.mock("@/services/subprocessoService", () => ({
    adicionarCompetencia: vi.fn(),
    atualizarCompetencia: vi.fn(),
    removerCompetencia: vi.fn(),
}));

describe("useMapasStore", () => {
    let store: ReturnType<typeof useMapasStore>;
    const codSubrocesso = 1;

    beforeEach(() => {
        setActivePinia(createPinia());
        store = useMapasStore();
        vi.clearAllMocks();
    });

    it("deve inicializar com valores nulos", () => {
        expect(store.mapaCompleto).toBeNull();
        expect(store.mapaAjuste).toBeNull();
        expect(store.impactoMapa).toBeNull();
    });

    describe("buscarMapaCompleto", () => {
        it("deve chamar o serviço e atualizar o estado em caso de sucesso", async () => {
            const mockMapa: MapaCompleto = {
                codigo: 1,
                subprocessoCodigo: 1,
                observacoes: "teste",
                competencias: [],
                situacao: "EM_ANDAMENTO",
            };
            vi.mocked(mapaService.obterMapaCompleto).mockResolvedValue(mockMapa);

            await store.buscarMapaCompleto(codSubrocesso);

            expect(mapaService.obterMapaCompleto).toHaveBeenCalledWith(codSubrocesso);
            expect(store.mapaCompleto).toEqual(mockMapa);
        });

        it("deve definir o estado como nulo em caso de falha", async () => {
            vi.mocked(mapaService.obterMapaCompleto).mockRejectedValue(
                new Error("Failed"),
            );
            store.mapaCompleto = {} as any; // Pre-set state

            await expect(store.buscarMapaCompleto(codSubrocesso)).rejects.toThrow("Failed");

            expect(store.mapaCompleto).toBeNull();
        });
    });

    describe("salvarMapa", () => {
        it("deve chamar o serviço e atualizar o estado em caso de sucesso", async () => {
            const request = {competencias: []};
            const mockResponse: MapaCompleto = {
                codigo: 1,
                subprocessoCodigo: 1,
                observacoes: "teste",
                competencias: [
                    {codigo: 1, descricao: "Nova", atividadesAssociadas: []},
                ],
                situacao: "EM_ANDAMENTO",
            };
            vi.mocked(mapaService.salvarMapaCompleto).mockResolvedValue(mockResponse);

            await store.salvarMapa(codSubrocesso, request);

            expect(mapaService.salvarMapaCompleto).toHaveBeenCalledWith(
                codSubrocesso,
                request,
            );
            expect(store.mapaCompleto).toEqual(mockResponse);
        });

        it("deve lançar erro em caso de falha", async () => {
            const request = {competencias: []};
            vi.mocked(mapaService.salvarMapaCompleto).mockRejectedValue(new Error("Fail"));

            await expect(store.salvarMapa(codSubrocesso, request)).rejects.toThrow("Fail");
        });
    });

    describe("buscarMapaAjuste", () => {
        it("deve chamar o serviço e atualizar o estado em caso de sucesso", async () => {
            const mockMapa: MapaAjuste = {
                codigo: 1,
                descricao: "teste",
                competencias: [],
            };
            vi.mocked(mapaService.obterMapaAjuste).mockResolvedValue(mockMapa);

            await store.buscarMapaAjuste(codSubrocesso);

            expect(mapaService.obterMapaAjuste).toHaveBeenCalledWith(codSubrocesso);
            expect(store.mapaAjuste).toEqual(mockMapa);
        });

        it("deve definir o estado como nulo em caso de falha", async () => {
            vi.mocked(mapaService.obterMapaAjuste).mockRejectedValue(new Error("Fail"));
            store.mapaAjuste = {} as any;

            await expect(store.buscarMapaAjuste(codSubrocesso)).rejects.toThrow("Fail");
            expect(store.mapaAjuste).toBeNull();
        });
    });

    describe("salvarAjustes", () => {
        it("deve chamar o serviço com sucesso", async () => {
            const request = {competencias: [], atividades: [], sugestoes: ""};
            vi.mocked(mapaService.salvarMapaAjuste).mockResolvedValue(undefined);

            await store.salvarAjustes(codSubrocesso, request);

            expect(mapaService.salvarMapaAjuste).toHaveBeenCalledWith(
                codSubrocesso,
                request,
            );
        });

        it("deve lançar erro em caso de falha", async () => {
            const request = {competencias: [], atividades: [], sugestoes: ""};
            vi.mocked(mapaService.salvarMapaAjuste).mockRejectedValue(new Error("Fail"));

            await expect(store.salvarAjustes(codSubrocesso, request)).rejects.toThrow("Fail");
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
            vi.mocked(mapaService.verificarImpactosMapa).mockResolvedValue(
                mockImpacto,
            );

            await store.buscarImpactoMapa(codSubrocesso);

            expect(mapaService.verificarImpactosMapa).toHaveBeenCalledWith(
                codSubrocesso,
            );
            expect(store.impactoMapa).toEqual(mockImpacto);
        });

        it("deve definir o estado como nulo em caso de falha", async () => {
            vi.mocked(mapaService.verificarImpactosMapa).mockRejectedValue(new Error("Fail"));
            store.impactoMapa = {} as any;

            await expect(store.buscarImpactoMapa(codSubrocesso)).rejects.toThrow("Fail");
            expect(store.impactoMapa).toBeNull();
        });
    });

    describe("adicionarCompetencia", () => {
        it("deve chamar o serviço e atualizar o estado em caso de sucesso", async () => {
            const competencia = {
                descricao: "Nova Competencia",
                codigo: 0,
                atividadesAssociadas: [],
            };
            const mockResponse: MapaCompleto = {
                codigo: 1,
                subprocessoCodigo: 1,
                observacoes: "teste",
                competencias: [
                    {codigo: 1, descricao: "Nova", atividadesAssociadas: []},
                ],
                situacao: "EM_ANDAMENTO",
            };
            vi.mocked(subprocessoService.adicionarCompetencia).mockResolvedValue(
                mockResponse,
            );

            await store.adicionarCompetencia(codSubrocesso, competencia);

            expect(subprocessoService.adicionarCompetencia).toHaveBeenCalledWith(
                codSubrocesso,
                competencia,
            );
            expect(store.mapaCompleto).toEqual(mockResponse);
        });

        it("deve lançar erro em caso de falha", async () => {
            const competencia = {
                descricao: "Nova Competencia",
                codigo: 0,
                atividadesAssociadas: [],
            };
            vi.mocked(subprocessoService.adicionarCompetencia).mockRejectedValue(new Error("Fail"));

            await expect(store.adicionarCompetencia(codSubrocesso, competencia)).rejects.toThrow("Fail");
        });
    });

    describe("atualizarCompetencia", () => {
        it("deve chamar o serviço e atualizar o estado em caso de sucesso", async () => {
            const competencia = {
                codigo: 1,
                descricao: "Competencia Atualizada",
                atividadesAssociadas: [],
            };
            const mockResponse: MapaCompleto = {
                codigo: 1,
                subprocessoCodigo: 1,
                observacoes: "teste",
                competencias: [
                    {codigo: 1, descricao: "Nova", atividadesAssociadas: []},
                ],
                situacao: "EM_ANDAMENTO",
            };
            vi.mocked(subprocessoService.atualizarCompetencia).mockResolvedValue(
                mockResponse,
            );

            await store.atualizarCompetencia(codSubrocesso, competencia);

            expect(subprocessoService.atualizarCompetencia).toHaveBeenCalledWith(
                codSubrocesso,
                competencia,
            );
            expect(store.mapaCompleto).toEqual(mockResponse);
        });

        it("deve lançar erro em caso de falha", async () => {
            const competencia = {
                codigo: 1,
                descricao: "Competencia Atualizada",
                atividadesAssociadas: [],
            };
            vi.mocked(subprocessoService.atualizarCompetencia).mockRejectedValue(new Error("Fail"));

            await expect(store.atualizarCompetencia(codSubrocesso, competencia)).rejects.toThrow("Fail");
        });
    });

    describe("removerCompetencia", () => {
        it("deve chamar o serviço e atualizar o estado em caso de sucesso", async () => {
            const idCompetencia = 1;
            const mockResponse: MapaCompleto = {
                codigo: 1,
                subprocessoCodigo: 1,
                observacoes: "teste",
                competencias: [
                    {codigo: 1, descricao: "Nova", atividadesAssociadas: []},
                ],
                situacao: "EM_ANDAMENTO",
            };
            vi.mocked(subprocessoService.removerCompetencia).mockResolvedValue(
                mockResponse,
            );

            await store.removerCompetencia(codSubrocesso, idCompetencia);

            expect(subprocessoService.removerCompetencia).toHaveBeenCalledWith(
                codSubrocesso,
                idCompetencia,
            );
            expect(store.mapaCompleto).toEqual(mockResponse);
        });

        it("deve lançar erro em caso de falha", async () => {
            const idCompetencia = 1;
            vi.mocked(subprocessoService.removerCompetencia).mockRejectedValue(new Error("Fail"));

            await expect(store.removerCompetencia(codSubrocesso, idCompetencia)).rejects.toThrow("Fail");
        });
    });

    describe("buscarMapaVisualizacao", () => {
        it("deve chamar o serviço e atualizar o estado em caso de sucesso", async () => {
            const mockMapa: MapaVisualizacao = {
                codigo: 1,
                descricao: "Teste",
                competencias: [],
            };
            vi.mocked(mapaService.obterMapaVisualizacao).mockResolvedValue(mockMapa);

            await store.buscarMapaVisualizacao(codSubrocesso);

            expect(mapaService.obterMapaVisualizacao).toHaveBeenCalledWith(
                codSubrocesso,
            );
            expect(store.mapaVisualizacao).toEqual(mockMapa);
        });

        it("deve definir o estado como nulo em caso de falha", async () => {
            vi.mocked(mapaService.obterMapaVisualizacao).mockRejectedValue(
                new Error("Failed"),
            );
            store.mapaVisualizacao = {} as any; // Pre-set state

            await expect(store.buscarMapaVisualizacao(codSubrocesso)).rejects.toThrow("Failed");

            expect(store.mapaVisualizacao).toBeNull();
        });
    });

    describe("disponibilizarMapa", () => {
        it("deve chamar o serviço com sucesso", async () => {
            const request = {observacoes: "teste", dataLimite: "2025-12-31"};
            vi.mocked(mapaService.disponibilizarMapa).mockResolvedValue(undefined);

            await store.disponibilizarMapa(codSubrocesso, request);

            expect(mapaService.disponibilizarMapa).toHaveBeenCalledWith(
                codSubrocesso,
                request,
            );
        });

        it("deve lançar erro em caso de falha", async () => {
            const request = {observacoes: "teste", dataLimite: "2025-12-31"};
            const error = {response: {data: {message: "Error"}}};
            vi.mocked(mapaService.disponibilizarMapa).mockRejectedValue(error);

            await expect(
                store.disponibilizarMapa(codSubrocesso, request),
            ).rejects.toThrow();
        });
    });
});
