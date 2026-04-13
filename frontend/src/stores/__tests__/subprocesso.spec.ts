import {describe, expect, it, vi} from "vitest";
import {useSubprocessoStore} from "../subprocesso";
import {setupStoreTest} from "@/test-utils/storeTestHelpers";
import * as subprocessoService from "@/services/subprocessoService";
import {logger} from "@/utils";

vi.mock("@/services/subprocessoService");
vi.mock("@/utils", async () => {
    const actual = await vi.importActual("@/utils") as any;
    return {
        ...actual,
        logger: {
            error: vi.fn(),
        },
    };
});

describe("subprocesso store", () => {
    const context = setupStoreTest(useSubprocessoStore);

    it("deve inicializar com estado padrão", () => {
        expect(context.store.contextoEdicao).toBeNull();
        expect(context.store.codSubprocessoCarregado).toBeNull();
        expect(context.store.invalido).toBe(true);
    });

    it("dadosValidos deve retornar false se estiver inválido", () => {
        context.store.invalido = true;
        context.store.codSubprocessoCarregado = 1;
        context.store.contextoEdicao = {} as any;
        expect(context.store.dadosValidos(1)).toBe(false);
    });

    it("dadosValidos deve retornar false para outro código de subprocesso", () => {
        context.store.invalido = false;
        context.store.codSubprocessoCarregado = 1;
        context.store.contextoEdicao = {} as any;
        expect(context.store.dadosValidos(2)).toBe(false);
    });

    it("dadosValidos deve retornar true se estiver válido e com código correto", () => {
        context.store.invalido = false;
        context.store.codSubprocessoCarregado = 1;
        context.store.contextoEdicao = {} as any;
        expect(context.store.dadosValidos(1)).toBe(true);
    });

    it("invalidar deve marcar como inválido", () => {
        context.store.invalido = false;
        context.store.invalidar();
        expect(context.store.invalido).toBe(true);
    });

    describe("garantirContextoEdicao", () => {
        it("deve usar cache se dados forem válidos", async () => {
            const mockContexto = {codigo: 1} as any;
            context.store.invalido = false;
            context.store.codSubprocessoCarregado = 1;
            context.store.contextoEdicao = mockContexto;

            const result = await context.store.garantirContextoEdicao(1);

            expect(subprocessoService.buscarContextoEdicao).not.toHaveBeenCalled();
            expect(result).toEqual(mockContexto);
        });

        it("deve buscar do service se cache for inválido", async () => {
            const mockContexto = {codigo: 1} as any;
            vi.mocked(subprocessoService.buscarContextoEdicao).mockResolvedValue(mockContexto);

            const result = await context.store.garantirContextoEdicao(1);

            expect(subprocessoService.buscarContextoEdicao).toHaveBeenCalledWith(1);
            expect(result).toEqual(mockContexto);
            expect(context.store.contextoEdicao).toEqual(mockContexto);
            expect(context.store.codSubprocessoCarregado).toBe(1);
            expect(context.store.invalido).toBe(false);
        });

        it("deve reutilizar carregamento em andamento por código", async () => {
            let resolver!: (valor: any) => void;
            const promessa = new Promise<any>((resolve) => {
                resolver = resolve;
            });
            vi.mocked(subprocessoService.buscarContextoEdicao).mockReturnValue(promessa);

            const requisicaoA = context.store.garantirContextoEdicao(1);
            const requisicaoB = context.store.garantirContextoEdicao(1);

            expect(subprocessoService.buscarContextoEdicao).toHaveBeenCalledTimes(1);
            resolver({detalhes: {unidade: {sigla: "TEST"}}});

            await expect(requisicaoA).resolves.toEqual({detalhes: {unidade: {sigla: "TEST"}}});
            await expect(requisicaoB).resolves.toEqual({detalhes: {unidade: {sigla: "TEST"}}});
        });

        it("deve retornar null e logar erro se o service falhar", async () => {
            vi.mocked(subprocessoService.buscarContextoEdicao).mockRejectedValue(new Error("Erro API"));

            const result = await context.store.garantirContextoEdicao(1);

            expect(result).toBeNull();
            expect(logger.error).toHaveBeenCalled();
        });
    });

    describe("garantirContextoEdicaoPorProcessoEUnidade", () => {
        it("deve reutilizar o cache quando processo e unidade já foram mapeados", async () => {
            const mockContexto = {detalhes: {codigo: 10, unidade: {sigla: "TEST"}}} as any;
            vi.mocked(subprocessoService.buscarContextoEdicaoPorProcessoEUnidade).mockResolvedValue(mockContexto);

            await context.store.garantirContextoEdicaoPorProcessoEUnidade(1, "TEST");
            vi.mocked(subprocessoService.buscarContextoEdicaoPorProcessoEUnidade).mockClear();

            const result = await context.store.garantirContextoEdicaoPorProcessoEUnidade(1, "TEST");

            expect(subprocessoService.buscarContextoEdicaoPorProcessoEUnidade).not.toHaveBeenCalled();
            expect(result).toEqual({codigo: 10, contexto: mockContexto});
        });

        it("deve buscar novamente quando for o mesmo sigla mas outro processo", async () => {
            const contextoAntigo = {detalhes: {codigo: 10, unidade: {sigla: "SESEL"}}} as any;
            const contextoNovo = {detalhes: {codigo: 20, unidade: {sigla: "SESEL"}}} as any;

            context.store.invalido = false;
            context.store.codSubprocessoCarregado = 10;
            context.store.contextoEdicao = contextoAntigo;
            vi.mocked(subprocessoService.buscarContextoEdicaoPorProcessoEUnidade).mockResolvedValue(contextoNovo);

            const result = await context.store.garantirContextoEdicaoPorProcessoEUnidade(101, "SESEL");

            expect(subprocessoService.buscarContextoEdicaoPorProcessoEUnidade).toHaveBeenCalledWith(101, "SESEL");
            expect(result).toEqual({codigo: 20, contexto: contextoNovo});
        });

        it("deve buscar do service se não houver cache para a unidade", async () => {
            const mockContexto = {detalhes: {codigo: 20, unidade: {sigla: "TEST"}}} as any;

            vi.mocked(subprocessoService.buscarContextoEdicaoPorProcessoEUnidade).mockResolvedValue(mockContexto);

            const result = await context.store.garantirContextoEdicaoPorProcessoEUnidade(1, "TEST");

            expect(subprocessoService.buscarContextoEdicaoPorProcessoEUnidade).toHaveBeenCalledWith(1, "TEST");
            expect(result).toEqual({codigo: 20, contexto: mockContexto});
            expect(context.store.contextoEdicao).toEqual(mockContexto);
            expect(context.store.codSubprocessoCarregado).toBe(20);
            expect(context.store.invalido).toBe(false);
        });

        it("deve reutilizar carregamento em andamento por processo+unidade", async () => {
            let resolverContextoPorUnidade!: (valor: any) => void;
            const promessaContextoPorUnidade = new Promise<any>((resolve) => {
                resolverContextoPorUnidade = resolve;
            });
            vi.mocked(subprocessoService.buscarContextoEdicaoPorProcessoEUnidade).mockReturnValue(promessaContextoPorUnidade);

            const requisicaoA = context.store.garantirContextoEdicaoPorProcessoEUnidade(1, "TEST");
            const requisicaoB = context.store.garantirContextoEdicaoPorProcessoEUnidade(1, "TEST");

            expect(subprocessoService.buscarContextoEdicaoPorProcessoEUnidade).toHaveBeenCalledTimes(1);
            resolverContextoPorUnidade({detalhes: {codigo: 20, unidade: {sigla: "TEST"}}});

            await expect(requisicaoA).resolves.toEqual({
                codigo: 20,
                contexto: {detalhes: {codigo: 20, unidade: {sigla: "TEST"}}}
            });
            await expect(requisicaoB).resolves.toEqual({
                codigo: 20,
                contexto: {detalhes: {codigo: 20, unidade: {sigla: "TEST"}}}
            });
        });

        it("deve retornar null e logar erro se a busca falhar", async () => {
            vi.mocked(subprocessoService.buscarContextoEdicaoPorProcessoEUnidade).mockRejectedValue(new Error("Erro API"));

            const result = await context.store.garantirContextoEdicaoPorProcessoEUnidade(1, "TEST");

            expect(result).toBeNull();
            expect(logger.error).toHaveBeenCalled();
        });
    });
});
