import {describe, expect, it, vi} from "vitest";
import {useOrganizacaoStore} from "../organizacao";
import {setupStoreTest} from "@/test-utils/storeTestHelpers";
import * as unidadeService from "@/services/unidadeService";
import {logger} from "@/utils";

vi.mock("@/services/unidadeService");
vi.mock("@/utils", async () => {
    const actual = await vi.importActual("@/utils") as any;
    return {
        ...actual,
        logger: {
            error: vi.fn(),
        },
    };
});

describe("organizacao store", () => {
    const context = setupStoreTest(useOrganizacaoStore);

    it("deve inicializar com estado vazio", () => {
        expect(context.store.diagnostico).toBeNull();
        expect(context.store.erroDiagnostico).toBeNull();
        expect(context.store.carregado).toBe(false);
    });

    describe("garantirDiagnostico", () => {
        it("não deve carregar se deveExibir for false", async () => {
            await context.store.garantirDiagnostico(false);
            expect(unidadeService.buscarDiagnosticoOrganizacional).not.toHaveBeenCalled();
            expect(context.store.carregado).toBe(false);
        });

        it("deve carregar diagnóstico com sucesso", async () => {
            const mockDiagnostico = {
                possuiViolacoes: false,
                resumo: "OK",
                grupos: [],
                quantidadeTiposViolacao: 0,
                quantidadeOcorrencias: 0
            };
            vi.mocked(unidadeService.buscarDiagnosticoOrganizacional).mockResolvedValue(mockDiagnostico);

            await context.store.garantirDiagnostico(true);

            expect(unidadeService.buscarDiagnosticoOrganizacional).toHaveBeenCalled();
            expect(context.store.diagnostico).toEqual(mockDiagnostico);
            expect(context.store.erroDiagnostico).toBeNull();
            expect(context.store.carregado).toBe(true);
        });

        it("não deve carregar novamente se já estiver carregado", async () => {
            context.store.carregado = true;
            await context.store.garantirDiagnostico(true);
            expect(unidadeService.buscarDiagnosticoOrganizacional).not.toHaveBeenCalled();
        });

        it("deve lidar com erro ao buscar diagnóstico", async () => {
            vi.mocked(unidadeService.buscarDiagnosticoOrganizacional).mockRejectedValue(new Error("Erro de rede"));

            await context.store.garantirDiagnostico(true);

            expect(context.store.diagnostico).toBeNull();
            expect(context.store.erroDiagnostico).toBe("Não foi possível verificar as pendências organizacionais.");
            expect(context.store.carregado).toBe(true);
            expect(logger.error).toHaveBeenCalled();
        });
    });
});
