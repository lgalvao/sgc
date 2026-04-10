import {describe, expect, it, vi} from "vitest";
import {useUnidadeStore} from "../unidade";
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

describe("unidade store", () => {
    const context = setupStoreTest(useUnidadeStore);

    it("deve inicializar com estado vazio", () => {
        expect(context.store.cacheArvoreElegibilidade.size).toBe(0);
    });

    describe("garantirArvoreElegibilidade", () => {
        it("deve buscar do service e mapear resultados", async () => {
            const mockArvore = [{codigo: 1, nome: "U1", sigla: "U1"}] as any;
            vi.mocked(unidadeService.buscarArvoreComElegibilidade).mockResolvedValue(mockArvore);
            vi.mocked(unidadeService.mapUnidadesArray).mockReturnValue(mockArvore);

            const result = await context.store.garantirArvoreElegibilidade("MAPEAMENTO");

            expect(unidadeService.buscarArvoreComElegibilidade).toHaveBeenCalledWith("MAPEAMENTO", undefined);
            expect(unidadeService.mapUnidadesArray).toHaveBeenCalled();
            expect(result).toEqual(mockArvore);
            expect(context.store.cacheArvoreElegibilidade.get("MAPEAMENTO_novo")).toEqual(mockArvore);
        });

        it("deve usar cache se já estiver disponível", async () => {
            const mockArvore = [{codigo: 1}] as any;
            context.store.cacheArvoreElegibilidade.set("MAPEAMENTO_1", mockArvore);

            const result = await context.store.garantirArvoreElegibilidade("MAPEAMENTO", 1);

            expect(unidadeService.buscarArvoreComElegibilidade).not.toHaveBeenCalled();
            expect(result).toEqual(mockArvore);
        });

        it("deve lidar com chamadas paralelas e evitar buscas duplicadas", async () => {
            const mockArvore = [{codigo: 1}] as any;
            let resolveBusca: (val: any) => void = () => {};
            const buscaPromise = new Promise((resolve) => {
                resolveBusca = resolve;
            });
            vi.mocked(unidadeService.buscarArvoreComElegibilidade).mockReturnValue(buscaPromise as any);
            vi.mocked(unidadeService.mapUnidadesArray).mockReturnValue(mockArvore);

            const p1 = context.store.garantirArvoreElegibilidade("MAPEAMENTO");
            const p2 = context.store.garantirArvoreElegibilidade("MAPEAMENTO");

            resolveBusca(mockArvore);

            const [r1, r2] = await Promise.all([p1, p2]);

            expect(unidadeService.buscarArvoreComElegibilidade).toHaveBeenCalledTimes(1);
            expect(r1).toEqual(mockArvore);
            expect(r2).toEqual(mockArvore);
        });

        it("deve retornar array vazio e logar erro se a busca falhar", async () => {
            vi.mocked(unidadeService.buscarArvoreComElegibilidade).mockRejectedValue(new Error("Erro API"));

            const result = await context.store.garantirArvoreElegibilidade("MAPEAMENTO");

            expect(result).toEqual([]);
            expect(logger.error).toHaveBeenCalled();
        });
    });

    it("invalidarCache deve limpar o cache", () => {
        context.store.cacheArvoreElegibilidade.set("test", []);
        context.store.invalidarCache();
        expect(context.store.cacheArvoreElegibilidade.size).toBe(0);
    });
});
