import { vi, it, expect, beforeEach, afterEach } from "vitest";
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
 * Testa chamada PUT
 */
export function testPutEndpoint(
    action: () => Promise<any>,
    url: string,
    payload?: any,
    response: any = {}
) {
    it(`deve fazer PUT em ${url}`, async () => {
        mockApi.put.mockResolvedValue({ data: response });
        await action();
        if (payload !== undefined) {
            expect(mockApi.put).toHaveBeenCalledWith(url, payload);
        } else {
            expect(mockApi.put).toHaveBeenCalledWith(url);
        }
    });
}

/**
 * Testa chamada DELETE
 */
export function testDeleteEndpoint(
    action: () => Promise<any>,
    url: string,
    response: any = {}
) {
    it(`deve fazer DELETE em ${url}`, async () => {
        mockApi.delete.mockResolvedValue({ data: response });
        await action();
        expect(mockApi.delete).toHaveBeenCalledWith(url);
    });
}
