import {describe, expect, it, vi} from "vitest";
import {useAsyncAction} from "../useAsyncAction";

describe("useAsyncAction", () => {
    it("deve executar acao com sucesso", async () => {
        const {carregando, erro, executar} = useAsyncAction();
        const acao = vi.fn().mockResolvedValue("sucesso");

        const resultado = await executar(acao);

        expect(resultado).toBe("sucesso");
        expect(carregando.value).toBe(false);
        expect(erro.value).toBeNull();
    });

    it("deve capturar erro na execucao", async () => {
        const {carregando, erro, executar} = useAsyncAction();
        const acao = vi.fn().mockRejectedValue(new Error("falha"));

        await expect(executar(acao)).rejects.toThrow("falha");

        expect(carregando.value).toBe(false);
        expect(erro.value).toBe("falha");
    });

    it("deve usar mensagem padrao se erro nao tiver mensagem", async () => {
        const {erro, executar} = useAsyncAction();
        const acao = vi.fn().mockRejectedValue({});

        await expect(executar(acao, "erro custom")).rejects.toEqual({});

        expect(erro.value).toBe("erro custom");
    });

    it("deve executar silencioso com sucesso", async () => {
        const {erro, executarSilencioso} = useAsyncAction();
        const acao = vi.fn().mockResolvedValue("ok");

        const resultado = await executarSilencioso(acao);

        expect(resultado).toBe("ok");
        expect(erro.value).toBeNull();
    });

    it("deve capturar erro silenciosamente", async () => {
        const {carregando, erro, executarSilencioso} = useAsyncAction();
        const acao = vi.fn().mockRejectedValue(new Error("silencio"));

        const resultado = await executarSilencioso(acao);

        expect(resultado).toBeUndefined();
        expect(carregando.value).toBe(false);
        expect(erro.value).toBe("silencio");
    });

    it("deve usar mensagem padrao no silencioso se erro nao tiver mensagem", async () => {
        const {erro, executarSilencioso} = useAsyncAction();
        const acao = vi.fn().mockRejectedValue({});

        await executarSilencioso(acao, "padrao");

        expect(erro.value).toBe("padrao");
    });
});
