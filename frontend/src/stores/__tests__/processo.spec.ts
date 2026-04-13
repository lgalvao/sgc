import {describe, expect, it, vi} from "vitest";
import {useProcessoStore} from "../processo";
import {setupStoreTest} from "@/test-utils/storeTestHelpers";
import * as processoService from "@/services/processoService";
import {logger} from "@/utils";

vi.mock("@/services/processoService");
vi.mock("@/utils", async () => {
    const actual = await vi.importActual("@/utils") as any;
    return {
        ...actual,
        logger: {
            error: vi.fn(),
        },
    };
});

describe("processo store", () => {
    const context = setupStoreTest(useProcessoStore);

    it("deve inicializar com estado padrão", () => {
        expect(context.store.contextoCompleto).toBeNull();
        expect(context.store.codProcessoCarregado).toBeNull();
        expect(context.store.invalido).toBe(true);
    });

    it("dadosValidos deve retornar false se estiver inválido", () => {
        context.store.invalido = true;
        context.store.codProcessoCarregado = 1;
        context.store.contextoCompleto = {} as any;
        expect(context.store.dadosValidos(1)).toBe(false);
    });

    it("dadosValidos deve retornar false para outro código de processo", () => {
        context.store.invalido = false;
        context.store.codProcessoCarregado = 1;
        context.store.contextoCompleto = {} as any;
        expect(context.store.dadosValidos(2)).toBe(false);
    });

    it("dadosValidos deve retornar false mesmo com contexto carregado, pois não reusa snapshot de permissões", () => {
        context.store.invalido = false;
        context.store.codProcessoCarregado = 1;
        context.store.contextoCompleto = {} as any;
        expect(context.store.dadosValidos(1)).toBe(false);
    });

    it("invalidar deve marcar como inválido", () => {
        context.store.invalido = false;
        context.store.invalidar();
        expect(context.store.invalido).toBe(true);
    });

    describe("garantirContextoCompleto", () => {
        it("deve buscar do service mesmo com contexto anterior carregado", async () => {
            const mockContexto = {codigo: 1} as any;
            context.store.invalido = false;
            context.store.codProcessoCarregado = 1;
            context.store.contextoCompleto = {codigo: 999} as any;
            vi.mocked(processoService.buscarContextoCompleto).mockResolvedValue(mockContexto);

            const result = await context.store.garantirContextoCompleto(1);

            expect(processoService.buscarContextoCompleto).toHaveBeenCalledWith(1);
            expect(result).toEqual(mockContexto);
            expect(context.store.contextoCompleto).toEqual(mockContexto);
            expect(context.store.codProcessoCarregado).toBe(1);
            expect(context.store.invalido).toBe(true);
        });

        it("deve reutilizar carregamento em andamento para evitar requisições duplicadas", async () => {
            let resolver!: (valor: any) => void;
            const promessa = new Promise<any>((resolve) => {
                resolver = resolve;
            });
            vi.mocked(processoService.buscarContextoCompleto).mockReturnValue(promessa);

            const requisicaoA = context.store.garantirContextoCompleto(1);
            const requisicaoB = context.store.garantirContextoCompleto(1);

            expect(processoService.buscarContextoCompleto).toHaveBeenCalledTimes(1);
            resolver({codigo: 1});

            await expect(requisicaoA).resolves.toEqual({codigo: 1});
            await expect(requisicaoB).resolves.toEqual({codigo: 1});
        });

        it("deve lançar erro se o service falhar", async () => {
            const error = new Error("Erro API");
            vi.mocked(processoService.buscarContextoCompleto).mockRejectedValue(error);

            await expect(context.store.garantirContextoCompleto(1)).rejects.toThrow("Erro API");
            expect(logger.error).toHaveBeenCalled();
        });
    });
});
