import {afterEach, beforeEach, expect, it, vi} from "vitest";
import apiClient from "@/axios-setup";

// Mock padrão do axios
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

    return { mockApi };
}

/**
 * Testa chamada GET
 */
export function testGetEndpoint(
    action: () => Promise<any>,
    url: string,
    response: any = {}
) {
    it(`deve fazer GET em ${url}`, async () => {
        mockApi.get.mockResolvedValue({ data: response });
        await action();
        expect(mockApi.get).toHaveBeenCalledWith(url);
    });
}

/**
 * Testa chamada POST
 */
export function testPostEndpoint(
    action: () => Promise<any>,
    url: string,
    payload?: any,
    response: any = {}
) {
    it(`deve fazer POST em ${url}`, async () => {
        mockApi.post.mockResolvedValue({ data: response });
        await action();
        if (payload !== undefined) {
            expect(mockApi.post).toHaveBeenCalledWith(url, payload);
        } else {
            expect(mockApi.post).toHaveBeenCalledWith(url);
        }
    });
}

/**
 * Testa tratamento de erros comuns
 */
export function testErrorHandling(
    action: () => Promise<any>,
    method: 'get' | 'post' | 'put' | 'delete' = 'get'
) {
    it(`deve lidar com erro 404`, async () => {
        mockApi[method].mockRejectedValue({ response: { status: 404 } });
        await expect(action()).rejects.toHaveProperty("response.status", 404);
    });

    it(`deve lidar com erro 500`, async () => {
        mockApi[method].mockRejectedValue({ response: { status: 500 } });
        await expect(action()).rejects.toHaveProperty("response.status", 500);
    });

    it(`deve lidar com erro de rede`, async () => {
        mockApi[method].mockRejectedValue(new Error("Network Error"));
        await expect(action()).rejects.toThrow("Network Error");
    });
}
