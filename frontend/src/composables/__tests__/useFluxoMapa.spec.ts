import {beforeEach, describe, expect, it, vi} from "vitest";
import {useFluxoMapa} from "../useFluxoMapa";
import * as subprocessoService from "@/services/subprocessoService";
import {useCacheMapa} from "@/composables/useMapaQuery";

vi.mock("@/services/subprocessoService", async (importOriginal) => {
    const actual = await importOriginal<typeof import("@/services/subprocessoService")>();
    return {
        ...actual,
        salvarMapaCompleto: vi.fn(),
        salvarMapaAjuste: vi.fn(),
        disponibilizarMapa: vi.fn(),
        adicionarCompetencia: vi.fn(),
        atualizarCompetencia: vi.fn(),
        removerCompetencia: vi.fn(),
        validarMapa: vi.fn(),
        aceitarValidacao: vi.fn(),
        homologarValidacao: vi.fn(),
        devolverValidacao: vi.fn(),
    };
});

vi.mock("@/composables/useMapaQuery", () => ({
    useCacheMapa: vi.fn(() => ({
        obterMapa: vi.fn().mockReturnValue(null)
    }))
}));

describe("useFluxoMapa", () => {
    beforeEach(() => {
        vi.clearAllMocks();
    });

    it("deve adicionar competencia e retornar mapa atualizado", async () => {
        const fluxoMapa = useFluxoMapa();
        const resposta = {codigo: 1, competencias: [{codigo: 2}]} as any;
        vi.mocked(subprocessoService.adicionarCompetencia).mockResolvedValue(resposta);

        const resultado = await fluxoMapa.adicionarCompetencia(10, {descricao: "Comp", atividadesCodigos: []});

        expect(subprocessoService.adicionarCompetencia).toHaveBeenCalledWith(10, {
            descricao: "Comp",
            atividadesCodigos: []
        });
        expect(resultado).toEqual(resposta);
    });

    it("deve atualizar competencia e retornar mapa atualizado", async () => {
        const fluxoMapa = useFluxoMapa();
        const resposta = {codigo: 1, competencias: [{codigo: 3}]} as any;
        vi.mocked(subprocessoService.atualizarCompetencia).mockResolvedValue(resposta);

        const resultado = await fluxoMapa.atualizarCompetencia(10, 20, {descricao: "Comp", atividadesCodigos: [1]});

        expect(subprocessoService.atualizarCompetencia).toHaveBeenCalledWith(10, 20, {
            descricao: "Comp",
            atividadesCodigos: [1]
        });
        expect(resultado).toEqual(resposta);
    });

    it("deve remover competencia e retornar mapa atualizado", async () => {
        const fluxoMapa = useFluxoMapa();
        const resposta = {codigo: 1, competencias: []} as any;
        vi.mocked(subprocessoService.removerCompetencia).mockResolvedValue(resposta);

        const resultado = await fluxoMapa.removerCompetencia(10, 20);

        expect(subprocessoService.removerCompetencia).toHaveBeenCalledWith(10, 20);
        expect(resultado).toEqual(resposta);
    });

    it("deve disponibilizar mapa", async () => {
        const fluxoMapa = useFluxoMapa();
        vi.mocked(subprocessoService.disponibilizarMapa).mockResolvedValue(undefined);

        await fluxoMapa.disponibilizarMapa(10, {dataLimite: "2026-05-01", observacoes: "obs"});

        expect(subprocessoService.disponibilizarMapa).toHaveBeenCalledWith(10, {
            dataLimite: "2026-05-01",
            observacoes: "obs"
        });
    });

    it("deve salvar mapa completo", async () => {
        const fluxoMapa = useFluxoMapa();
        const resposta = {codigo: 1, competencias: []} as any;
        vi.mocked(subprocessoService.salvarMapaCompleto).mockResolvedValue(resposta);

        const dados = {competencias: []};
        const resultado = await fluxoMapa.salvarMapa(10, dados);

        expect(subprocessoService.salvarMapaCompleto).toHaveBeenCalledWith(10, dados);
        expect(resultado).toEqual(resposta);
    });

    it("deve salvar ajustes do mapa", async () => {
        const fluxoMapa = useFluxoMapa();
        vi.mocked(subprocessoService.salvarMapaAjuste).mockResolvedValue(undefined);

        const dados = {competencias: [], atividades: [], sugestoes: "ajuste"};
        await fluxoMapa.salvarAjustes(10, dados);

        expect(subprocessoService.salvarMapaAjuste).toHaveBeenCalledWith(10, dados);
    });

    describe("ações de análise", () => {
        it("deve validar mapa", async () => {
            const fluxoMapa = useFluxoMapa();
            await fluxoMapa.validarMapa(10);
            expect(subprocessoService.validarMapa).toHaveBeenCalledWith(10);
        });

        it("deve aceitar mapa", async () => {
            const fluxoMapa = useFluxoMapa();
            await fluxoMapa.aceitarMapa(10, {observacao: "ok"});
            expect(subprocessoService.aceitarValidacao).toHaveBeenCalledWith(10, {texto: "ok"});
        });

        it("deve homologar mapa", async () => {
            const fluxoMapa = useFluxoMapa();
            await fluxoMapa.homologarMapa(10, {observacao: "ok"});
            expect(subprocessoService.homologarValidacao).toHaveBeenCalledWith(10, {texto: "ok"});
        });

        it("deve devolver mapa", async () => {
            const fluxoMapa = useFluxoMapa();
            await fluxoMapa.devolverMapa(10, {justificativa: "ajustar"});
            expect(subprocessoService.devolverValidacao).toHaveBeenCalledWith(10, {justificativa: "ajustar"});
        });
    });

    describe("removerAtividadeDaCompetencia", () => {
        it("deve remover atividade da competencia e atualizar", async () => {
            const mockMapa = {
                competencias: [
                    {
                        codigo: 20,
                        descricao: "Comp 20",
                        atividades: [{codigo: 100}, {codigo: 101}]
                    }
                ]
            };
            vi.mocked(useCacheMapa).mockReturnValue({
                obterMapa: vi.fn().mockReturnValue(mockMapa)
            } as any);

            const fluxoMapa = useFluxoMapa();
            await fluxoMapa.removerAtividadeDaCompetencia(10, 20, 100);

            expect(subprocessoService.atualizarCompetencia).toHaveBeenCalledWith(10, 20, {
                descricao: "Comp 20",
                atividadesCodigos: [101]
            });
        });

        it("deve lançar erro se competencia não for encontrada", async () => {
            vi.mocked(useCacheMapa).mockReturnValue({
                obterMapa: vi.fn().mockReturnValue({competencias: []})
            } as any);

            const fluxoMapa = useFluxoMapa();
            await expect(fluxoMapa.removerAtividadeDaCompetencia(10, 20, 100)).rejects.toThrow("Competência não encontrada.");
        });
    });

    describe("tratamento de erros", () => {
        it("deve propagar erro se salvarAjustes falhar", async () => {
            const fluxoMapa = useFluxoMapa();
            vi.mocked(subprocessoService.salvarMapaAjuste).mockRejectedValue(new Error("Erro ajuste"));

            const dados = {competencias: [], atividades: [], sugestoes: ""};
            await expect(fluxoMapa.salvarAjustes(10, dados)).rejects.toThrow("Erro ajuste");
        });

        it("deve propagar erro se salvarMapa falhar", async () => {
            const fluxoMapa = useFluxoMapa();
            vi.mocked(subprocessoService.salvarMapaCompleto).mockRejectedValue(new Error("Erro grave"));

            const dados = {competencias: []};
            await expect(fluxoMapa.salvarMapa(10, dados)).rejects.toThrow("Erro grave");
            expect(fluxoMapa.erro.value).toBe("Erro grave");
        });
    });
});
