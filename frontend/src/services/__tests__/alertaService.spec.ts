import { describe, expect, it, vi } from "vitest";
import { setupServiceTest } from "@/test-utils/serviceTestHelpers";
import * as AlertaService from "../alertaService";

// Mock do axios via helper
vi.mock("@/axios-setup");

describe("AlertaService", () => {
    const { mockApi } = setupServiceTest();

    it("marcarComoLido deve chamar o endpoint correto", async () => {
        const alertaId = 123;
        mockApi.post.mockResolvedValue({ data: {} });

        await AlertaService.marcarComoLido(alertaId);

        expect(mockApi.post).toHaveBeenCalledWith(`/alertas/${alertaId}/marcar-como-lido`);
    });

    it("deve lançar um erro em caso de falha", async () => {
        const alertaId = 123;
        const erro = new Error("Erro na API");
        mockApi.post.mockRejectedValue(erro);

        await expect(AlertaService.marcarComoLido(alertaId)).rejects.toThrow("Erro na API");
    });
});
