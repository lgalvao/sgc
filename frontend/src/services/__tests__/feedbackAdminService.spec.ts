import {beforeEach, describe, expect, it, vi} from "vitest";
import apiClient from "@/axios-setup";
import {listarFeedbacksAdmin} from "../feedbackAdminService";

vi.mock("@/axios-setup", () => ({
    default: {
        get: vi.fn(),
    }
}));

describe("feedbackAdminService", () => {
    beforeEach(() => {
        vi.clearAllMocks();
    });

    it("listarFeedbacksAdmin deve fazer GET para /feedback/listar com limite", async () => {
        const mockData = [{codigo: "1", tipo: "BUG"}];
        vi.mocked(apiClient.get).mockResolvedValue({data: mockData} as any);

        const resultado = await listarFeedbacksAdmin(25);

        expect(apiClient.get).toHaveBeenCalledWith("/feedback/listar", {
            params: {limite: 25}
        });
        expect(resultado).toEqual(mockData);
    });
});
