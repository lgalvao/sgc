import {createPinia, setActivePinia} from "pinia";
import {afterEach, beforeAll, beforeEach, describe, expect, it, vi} from "vitest";
import router from "@/router";
import {useFeedbackStore} from "@/stores/feedback";

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
    let requestErrorInterceptor: (error: any) => any;
    let responseErrorInterceptor: (error: any) => any;

    beforeAll(async () => {
        // Import axios-setup AFTER mocking its dependencies
        await import("../axios-setup"); // Use dynamic import

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
    });

    beforeEach(async () => {
        setActivePinia(createPinia());
        vi.clearAllMocks();
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

    it("request error interceptor should reject promise", async () => {
        const error = new Error("Request error");
        await expect(requestErrorInterceptor(error)).rejects.toThrow("Request error");
    });

    it("response error interceptor should redirect to login on 401", async () => {
        const feedbackStore = useFeedbackStore();
        vi.spyOn(feedbackStore, "show");

        const error = {isAxiosError: true, response: {status: 401, data: {}}};
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

        const error = {isAxiosError: true, response: {status: 400, data: {}}};
        await expect(responseErrorInterceptor(error)).rejects.toEqual(error);
        expect(feedbackStore.show).not.toHaveBeenCalled();
    });

    it("response error interceptor should show unexpected error for 500 with message", async () => {
        const feedbackStore = useFeedbackStore();
        vi.spyOn(feedbackStore, "show");

        const error = {
            isAxiosError: true,
            response: {status: 500, data: {message: "Server Error"}},
        };
        await expect(responseErrorInterceptor(error)).rejects.toEqual(error);
        // Title changed to "Erro Inesperado" in apiError.ts
        expect(feedbackStore.show).toHaveBeenCalledWith("Erro Inesperado", "Server Error", "danger");
    });

    it("response error interceptor should show generic error for 500 without message", async () => {
        const feedbackStore = useFeedbackStore();
        vi.spyOn(feedbackStore, "show");

        const error = {
            isAxiosError: true,
            response: {status: 500, data: {}},
        };
        await expect(responseErrorInterceptor(error)).rejects.toEqual(error);
        expect(feedbackStore.show).toHaveBeenCalledWith(
            "Erro Inesperado",
            "Erro desconhecido.", // Updated expectation
            "danger"
        );
    });

    it("response error interceptor should show network error", async () => {
        const feedbackStore = useFeedbackStore();
        vi.spyOn(feedbackStore, "show");

        // isAxiosError: true, and no response, but request exists
        const error = {isAxiosError: true, request: {}, message: "Network Error"};
        await expect(responseErrorInterceptor(error)).rejects.toEqual(error);
        expect(feedbackStore.show).toHaveBeenCalledWith(
            "Erro de Rede",
            expect.stringContaining("Não foi possível conectar"),
            "danger"
        );
    });

    it("response error interceptor should show generic error with message property", async () => {
        const feedbackStore = useFeedbackStore();
        vi.spyOn(feedbackStore, "show");

        const error = new Error("Generic failure");
        await expect(responseErrorInterceptor(error)).rejects.toEqual(error);
        expect(feedbackStore.show).toHaveBeenCalledWith("Erro Inesperado", "Generic failure", "danger");
    });

    // Keeping this test but I need to update axios-setup.ts to handle the exception if I want it to pass.
    // Or I can update expectation if I don't want to swallow the error.
    // I'll update axios-setup.ts to swallow the error.
    it("response error interceptor should handle store errors gracefully", async () => {
        const feedbackStore = useFeedbackStore();
        // Force an error in store.show
        vi.spyOn(feedbackStore, "show").mockImplementation(() => {
            throw new Error("Store error");
        });
        const consoleSpy = vi.spyOn(console, "error").mockImplementation(() => {
        });

        const error = new Error("Generic failure");

        await expect(responseErrorInterceptor(error)).rejects.toEqual(error);

        expect(consoleSpy).toHaveBeenCalledWith("Erro ao exibir notificação:", expect.any(Error));
    });
});
