import {afterEach, beforeEach, expect, it, vi} from "vitest";
import apiClient from "@/axios-setup";

vi.mock("@/axios-setup", () => ({
    default: {
        get: vi.fn(),
        post: vi.fn(),
        put: vi.fn(),
        delete: vi.fn(),
    },
}));

const mockApi = apiClient as any;

/**
 * Utilitário para configurar testes de Service
 */
export function setupServiceTest() {
    beforeEach(() => {
        vi.clearAllMocks();
    });

    afterEach(() => {
        // Opcional: restaurar mocks se necessário, mas geralmente clearAllMocks é suficiente com vi.fn
        vi.restoreAllMocks();
    });

    return {mockApi};
}

/**
 * Testa chamada GET
 */
export function testGetEndpoint(
    action: () => Promise<unknown>,
    url: string,
    response: unknown = {}
) {
    it(`deve fazer GET em ${url}`, async () => {
        mockApi.get.mockResolvedValue({data: response});
        await action();
        expect(mockApi.get).toHaveBeenCalled();
        expect(mockApi.get.mock.calls[0][0]).toBe(url);
    });
}

/**
 * Testa chamada POST
 */
export function testPostEndpoint(
    action: () => Promise<unknown>,
    url: string,
    payload?: unknown,
    response: unknown = {}
) {
    it(`deve fazer POST em ${url}`, async () => {
        mockApi.post.mockResolvedValue({data: response});
        await action();
        expect(mockApi.post).toHaveBeenCalled();
        expect(mockApi.post.mock.calls[0][0]).toBe(url);

        if (payload !== undefined) {
            expect(mockApi.post.mock.calls[0][1]).toEqual(payload);
        }
    });
}

/**
 * Testa tratamento de erros comuns
 */
export function testErrorHandling(
    action: () => Promise<unknown>,
    method: 'get' | 'post' | 'put' | 'delete' = 'get'
) {
    it(`deve lidar com erro 404`, async () => {
        mockApi[method].mockRejectedValue({isAxiosError: true, response: {status: 404}});
        await expect(action()).rejects.toHaveProperty("response.status", 404);
    });

    it(`deve lidar com erro 500`, async () => {
        mockApi[method].mockRejectedValue({isAxiosError: true, response: {status: 500}});
        await expect(action()).rejects.toHaveProperty("response.status", 500);
    });

    it(`deve lidar com erro de rede`, async () => {
        mockApi[method].mockRejectedValue(new Error("Network error"));
        await expect(action()).rejects.toThrow("Network error");
    });
}
