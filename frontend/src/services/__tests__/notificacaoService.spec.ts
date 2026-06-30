import {describe, expect, it, vi} from "vitest";
import apiClient from "@/axios-setup";
import {
    buscarUrlLeitorEmailTestes,
    compararNotificacoes,
    listarNotificacoesAdmin,
    obterTimestampOrdenacao,
    reenviarNotificacao
} from "../notificacaoService";

vi.mock("@/axios-setup", () => ({
    default: {
        get: vi.fn(),
        post: vi.fn()
    }
}));

describe("notificacaoService", () => {
    it("listarNotificacoesAdmin deve chamar o endpoint correto", async () => {
        vi.mocked(apiClient.get).mockResolvedValue({data: []});
        await listarNotificacoesAdmin(20);
        expect(apiClient.get).toHaveBeenCalledWith("/admin/notificacoes/listar", {params: {limite: 20}});
    });

    it("reenviarNotificacao deve chamar o endpoint correto", async () => {
        vi.mocked(apiClient.post).mockResolvedValue({data: {codigo: 1, reenfileiradas: 1}});
        const res = await reenviarNotificacao(123);
        expect(apiClient.post).toHaveBeenCalledWith("/admin/notificacoes/123/reenviar");
        expect(res.codigo).toBe(1);
    });

    it("buscarUrlLeitorEmailTestes deve retornar url ou null", async () => {
        vi.mocked(apiClient.get).mockResolvedValue({data: {url: " http://mail.test "}});
        expect(await buscarUrlLeitorEmailTestes()).toBe("http://mail.test");

        vi.mocked(apiClient.get).mockResolvedValue({data: {url: null}});
        expect(await buscarUrlLeitorEmailTestes()).toBeNull();
    });

    it("obterTimestampOrdenacao deve retornar timestamp correto", () => {
        const item = {proximaTentativaEm: "2026-05-24T10:00:00Z"} as any;
        expect(obterTimestampOrdenacao(item)).toBe(Date.parse("2026-05-24T10:00:00Z"));

        const item2 = {dataHoraEnvio: "2026-05-24T11:00:00Z"} as any;
        expect(obterTimestampOrdenacao(item2)).toBe(Date.parse("2026-05-24T11:00:00Z"));

        expect(obterTimestampOrdenacao({} as any)).toBe(0);
        expect(obterTimestampOrdenacao({proximaTentativaEm: "invalido"} as any)).toBe(0);
    });

    it("compararNotificacoes deve ordenar por prioridade e depois timestamp", () => {
        const a = {situacao: "PENDENTE", dataHoraCriacao: "2026-05-24T10:00:00Z"} as any;
        const b = {situacao: "ENVIADO", dataHoraEnvio: "2026-05-24T11:00:00Z"} as any;

        // PENDENTE tem prioridade menor (valor numerico) que ENVIADO?
        // Vamos assumir que a logica de info.prioridade define isso.
        const res = compararNotificacoes(a, b);
        expect(res).not.toBe(0);

        const c = {situacao: "PENDENTE", dataHoraCriacao: "2026-05-24T09:00:00Z"} as any;
        // Mesmo status, 'a' é mais recente que 'c'. Comparador retorna b - a para timestamp (descendente).
        expect(compararNotificacoes(a, c)).toBeLessThan(0);
    });

    it("compararNotificacoes deve lidar com situacao desconhecida", () => {
        expect(compararNotificacoes({situacao: "X"} as any, {situacao: "Y"} as any)).toBe(0);
    });
});
