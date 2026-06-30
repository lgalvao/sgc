import {describe, expect, it, vi} from "vitest";
import apiClient from "@/axios-setup";
import {listarProcessos, marcarAlertasLidos, obterBootstrap} from "../painelService";

vi.mock("@/axios-setup", () => ({
    default: {
        get: vi.fn(),
        post: vi.fn()
    }
}));

describe("painelService", () => {
    it("obterBootstrap deve chamar endpoint", async () => {
        vi.mocked(apiClient.get).mockResolvedValue({data: {usuario: {}}});
        await obterBootstrap();
        expect(apiClient.get).toHaveBeenCalledWith("/painel/bootstrap");
    });

    it("listarProcessos deve formatar query params", async () => {
        vi.mocked(apiClient.get).mockResolvedValue({data: {content: []}});

        await listarProcessos({codUnidade: 1, page: 2, sort: "descricao", order: "asc"});

        expect(apiClient.get).toHaveBeenCalledWith("/painel/processos", {
            params: {
                unidade: 1,
                page: 2,
                size: 20,
                sort: "descricao,asc"
            }
        });
    });

    it("marcarAlertasLidos deve chamar post", async () => {
        await marcarAlertasLidos([1, 2]);
        expect(apiClient.post).toHaveBeenCalledWith("/painel/alertas/marcar-lidos", [1, 2]);
    });
});
