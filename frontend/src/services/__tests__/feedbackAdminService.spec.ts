import {describe, expect, it, vi} from "vitest";
import apiClient from "@/axios-setup";
import {listarFeedbacksAdmin, obterUrlScreenshot} from "../feedbackAdminService";

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

  it("obterUrlScreenshot deve construir a URL corretamente", () => {
    const url = obterUrlScreenshot("abc");
    expect(url).toBe("http://localhost:8080/api/feedback/abc/screenshot");
  });

  it("obterUrlScreenshot deve lidar com baseURL sem /api ou com slash", () => {
    apiClient.defaults.baseURL = "http://api.teste/";
    expect(obterUrlScreenshot("x")).toBe("http://api.teste/feedback/x/screenshot");

    apiClient.defaults.baseURL = "";
    expect(obterUrlScreenshot("y")).toBe("/api/feedback/y/screenshot");
  });
});
