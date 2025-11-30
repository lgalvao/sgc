import {createPinia, setActivePinia} from "pinia";
import {beforeAll, beforeEach, describe, expect, it, vi} from "vitest";
import router from "@/router";
import { ToastService } from "@/services/toastService"; // Import ToastService

// Hoist mock instance so it's shared between module and test
const { mockInstance } = vi.hoisted(() => {
  return {
    mockInstance: {
        interceptors: {
            request: {use: vi.fn()},
            response: {use: vi.fn()},
        },
        defaults: {headers: {common: {}}},
        get: vi.fn(),
        post: vi.fn(),
        put: vi.fn(),
        delete: vi.fn(),
    },
  };
});

// Mock router
vi.mock("@/router", () => ({
    default: {
        push: vi.fn(),
    },
}));

// Mock axios
vi.mock("axios", async (importOriginal) => {
    const actual = await importOriginal<any>();
    return {
        default: {
            ...actual,
            create: vi.fn(() => mockInstance),
        },
    };
});

// Mock ToastService
vi.mock("@/services/toastService", () => {
    const mockToastService = {
        sucesso: vi.fn(),
        erro: vi.fn(),
        aviso: vi.fn(),
        info: vi.fn(),
    };
    return {
        ToastService: mockToastService,
        registerToast: vi.fn(),
        // Export the mockToastService for direct access in tests
        mockToastService,
    };
});

describe("axios-setup", () => {
    let requestInterceptor: (config: any) => any;
    let responseErrorInterceptor: (error: any) => any;

    beforeAll(async () => {
        // Import axios-setup AFTER mocking its dependencies
        await import("../axios-setup"); // Use dynamic import

        // Access interceptors from the hoisted mock instance
        const requestUseCalls = mockInstance.interceptors.request.use.mock.calls;
        const responseUseCalls = mockInstance.interceptors.response.use.mock.calls;

        if (requestUseCalls.length > 0) {
            requestInterceptor = requestUseCalls[0][0];
        }
        if (responseUseCalls.length > 0) {
            responseErrorInterceptor = responseUseCalls[0][1];
        }
    });

    beforeEach(async () => {
        setActivePinia(createPinia());
        // Reset ToastService mocks before each test
        (ToastService.sucesso as any).mockClear();
        (ToastService.erro as any).mockClear();
        (ToastService.aviso as any).mockClear();
        (ToastService.info as any).mockClear();
    });

    afterEach(() => {
        vi.restoreAllMocks();
    });

    it("request interceptor should add token if exists", () => {
        localStorage.setItem("jwtToken", "token123");
        const config = {headers: {}};
        const result = requestInterceptor(config);
        expect(result.headers.Authorization).toBe("Bearer token123");
    });

    it("request interceptor should not add token if missing", () => {
        localStorage.removeItem("jwtToken");
        const config = {headers: {}};
        const result = requestInterceptor(config);
        expect(result.headers.Authorization).toBeUndefined();
    });

    it("response error interceptor should redirect to login on 401", async () => {
        vi.spyOn(ToastService, "erro"); // Spy on ToastService.erro

        const error = {response: {status: 401}};
        await expect(responseErrorInterceptor(error)).rejects.toEqual(error);
        expect(ToastService.erro).toHaveBeenCalledWith(
            "Não Autorizado",
            expect.stringContaining("Sua sessão expirou"),
        );
        expect(router.push).toHaveBeenCalledWith("/login");
    });

    it("response error interceptor should not show global error for 400, 404, 409, 422", async () => {
        vi.spyOn(ToastService, "erro"); // Spy on ToastService.erro

        const error = {response: {status: 400}};
        await expect(responseErrorInterceptor(error)).rejects.toEqual(error);
        expect(ToastService.erro).not.toHaveBeenCalled();
    });

    it("response error interceptor should show unexpected error for 500", async () => {
        vi.spyOn(ToastService, "erro"); // Spy on ToastService.erro

        const error = {
            response: {status: 500, data: {message: "Server Error"}},
        };
        await expect(responseErrorInterceptor(error)).rejects.toEqual(error);
        expect(ToastService.erro).toHaveBeenCalledWith("Erro Inesperado", "Server Error");
    });

    it("response error interceptor should show network error", async () => {
        vi.spyOn(ToastService, "erro"); // Spy on ToastService.erro

        const error = {request: {}}; // No response, but request exists -> Network error usually
        await expect(responseErrorInterceptor(error)).rejects.toEqual(error);
        expect(ToastService.erro).toHaveBeenCalledWith(
            "Erro de Rede",
            expect.stringContaining("Não foi possível conectar"),
        );
    });
});
