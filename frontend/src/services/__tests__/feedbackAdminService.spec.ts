import {describe, expect, it, vi} from "vitest";
import apiClient from "@/axios-setup";
import {listarFeedbacksAdmin} from "../feedbackAdminService";

vi.mock("@/axios-setup", () => ({
  default: {
    get: vi.fn(),
    defaults: {
      baseURL: "http://localhost:8080/api"
    }
  }
}));

describe("feedbackAdminService", () => {
  it("listarFeedbacksAdmin deve chamar o endpoint correto", async () => {
    const mockData = [{codigo: "123"}];
    vi.mocked(apiClient.get).mockResolvedValue({data: mockData});

    const result = await listarFeedbacksAdmin(50);

    expect(apiClient.get).toHaveBeenCalledWith("/feedback/listar", {
      params: {limite: 50}
    });
    expect(result).toEqual(mockData);
  });
});
