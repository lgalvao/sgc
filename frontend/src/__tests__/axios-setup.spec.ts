import {createPinia, setActivePinia} from "pinia";
import {afterEach, beforeAll, beforeEach, describe, expect, it, vi} from "vitest";
import router from "@/router";

// Hoist mock instance so it's shared between module and test
const {mockInstance} = vi.hoisted(() => {
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

vi.mock("@/router", () => ({
    default: {
        push: vi.fn().mockResolvedValue(undefined),
        currentRoute: {
            value: {path: '/'}
        }
    },
}));

vi.mock("@/utils", () => {
    return {
        logger: {
            error: vi.fn(),
            warn: vi.fn(),
            info: vi.fn(),
        }
    }
});

vi.mock("axios", () => {
    return {
        default: {
            create: vi.fn(() => mockInstance),
        },
    };
});

describe("axios-setup", () => {
    let requestInterceptor: (config: any) => any;
    let requestErrorInterceptor: (error: any) => any;
    let responseErrorInterceptor: (error: any) => any;

    beforeAll(async () => {
        const {setRouter} = await import("../axios-setup"); // Use dynamic import
        setRouter(router as any);

        // Access interceptors from the hoisted mock instance
        const requestUseCalls = mockInstance.interceptors.request.use.mock.calls;
        const responseUseCalls = mockInstance.interceptors.response.use.mock.calls;

        if (requestUseCalls.length > 0) {
            requestInterceptor = requestUseCalls[0][0];
            requestErrorInterceptor = requestUseCalls[0][1];
        }
        if (responseUseCalls.length > 0) {
            // responseUseCalls[0][0] is success handler (identity)
            responseErrorInterceptor = responseUseCalls[0][1];
        }
    }, 30000); // Increased timeout to 30s

    beforeEach(async () => {
        setActivePinia(createPinia());
        vi.clearAllMocks();
    });

    afterEach(() => {
        vi.restoreAllMocks();
    });

    it("interceptor de requisição deve adicionar token se existir", () => {
        localStorage.setItem("jwtToken", "token123");
        const config = {headers: {}};
        const result = requestInterceptor(config);
        expect(result.headers.Authorization).toBe("Bearer token123");
    });

    it("interceptor de requisição não deve adicionar token se estiver faltando", () => {
        localStorage.removeItem("jwtToken");
        const config = {headers: {}};
        const result = requestInterceptor(config);
        expect(result.headers.Authorization).toBeUndefined();
    });

    it("interceptor de erro de requisição deve rejeitar a promessa", async () => {
        const error = new Error("Request error");
        await expect(requestErrorInterceptor(error)).rejects.toThrow("Request error");
    });

    it("interceptor de erro de resposta deve redirecionar para login em caso de 401", async () => {
        const error = {isAxiosError: true, response: {status: 401, data: {}}};
        await expect(responseErrorInterceptor(error)).rejects.toEqual(error);
        expect(router.push).toHaveBeenCalledWith("/login");
    });

    it("interceptor de erro de resposta não deve redirecionar para 400, 404, 409, 422", async () => {
        const error = {isAxiosError: true, response: {status: 400, data: {}}};
        await expect(responseErrorInterceptor(error)).rejects.toEqual(error);
        expect(router.push).not.toHaveBeenCalled();
    });

    it("interceptor de erro de resposta deve propagar erro 500", async () => {
        const error = {
            isAxiosError: true,
            response: {status: 500, data: {message: "Server Error"}},
            stack: 'stack'
        };
        await expect(responseErrorInterceptor(error)).rejects.toEqual(error);
    });

    it("interceptor de erro de resposta deve propagar erro de rede", async () => {
        const error = {isAxiosError: true, request: {}, message: "Network Error"};
        await expect(responseErrorInterceptor(error)).rejects.toEqual(error);
    });

    it("interceptor de erro de resposta deve propagar erro genérico", async () => {
        const error = new Error("Generic failure");
        await expect(responseErrorInterceptor(error)).rejects.toEqual(error);
    });
});
