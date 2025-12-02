import {createPinia, setActivePinia} from "pinia";
import {beforeAll, beforeEach, describe, expect, it, vi} from "vitest";
import router from "@/router";
import { useFeedbackStore } from "@/stores/feedback";

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
        const feedbackStore = useFeedbackStore();
        vi.spyOn(feedbackStore, "show");

        const error = {response: {status: 401}};
        await expect(responseErrorInterceptor(error)).rejects.toEqual(error);
        expect(feedbackStore.show).toHaveBeenCalledWith(
            "Não Autorizado",
            expect.stringContaining("Sua sessão expirou"),
            "danger"
        );
        expect(router.push).toHaveBeenCalledWith("/login");
    });

    it("response error interceptor should not show global error for 400, 404, 409, 422", async () => {
        const feedbackStore = useFeedbackStore();
        vi.spyOn(feedbackStore, "show");

        const error = {response: {status: 400}};
        await expect(responseErrorInterceptor(error)).rejects.toEqual(error);
        expect(feedbackStore.show).not.toHaveBeenCalled();
    });

    it("response error interceptor should show unexpected error for 500", async () => {
        const feedbackStore = useFeedbackStore();
        vi.spyOn(feedbackStore, "show");

        const error = {
            response: {status: 500, data: {message: "Server Error"}},
        };
        await expect(responseErrorInterceptor(error)).rejects.toEqual(error);
        expect(feedbackStore.show).toHaveBeenCalledWith("Erro Inesperado", "Server Error", "danger");
    });

    it("response error interceptor should show network error", async () => {
        const feedbackStore = useFeedbackStore();
        vi.spyOn(feedbackStore, "show");

        const error = {request: {}}; // No response, but request exists -> Network error usually
        await expect(responseErrorInterceptor(error)).rejects.toEqual(error);
        expect(feedbackStore.show).toHaveBeenCalledWith(
            "Erro de Rede",
            expect.stringContaining("Não foi possível conectar"),
            "danger"
        );
    });
});
