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
        push: vi.fn().mockResolvedValue(undefined),
        currentRoute: {
            value: { path: '/' }
        }
    },
}));

// Mock logger
vi.mock("@/utils", () => {
    return {
        logger: {
            error: vi.fn(),
            warn: vi.fn(),
            info: vi.fn(),
        }
    }
});

// Mock axios
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

    it("interceptor de erro de resposta não deve mostrar erro global para 400, 404, 409, 422", async () => {
        const feedbackStore = useFeedbackStore();
        vi.spyOn(feedbackStore, "show");

        const error = {isAxiosError: true, response: {status: 400, data: {}}};
        await expect(responseErrorInterceptor(error)).rejects.toEqual(error);
        expect(feedbackStore.show).not.toHaveBeenCalled();
    });

    it("interceptor de erro de resposta deve mostrar erro inesperado para 500 com mensagem", async () => {
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

    it("interceptor de erro de resposta deve mostrar erro genérico para 500 sem mensagem", async () => {
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

    it("interceptor de erro de resposta deve mostrar erro de rede", async () => {
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

    it("interceptor de erro de resposta deve mostrar erro genérico com propriedade de mensagem", async () => {
        const feedbackStore = useFeedbackStore();
        vi.spyOn(feedbackStore, "show");

        const error = new Error("Generic failure");
        await expect(responseErrorInterceptor(error)).rejects.toEqual(error);
        expect(feedbackStore.show).toHaveBeenCalledWith("Erro Inesperado", "Generic failure", "danger");
    });

    // Keeping this test but I need to update axios-setup.ts to handle the exception if I want it to pass.
    // Or I can update expectation if I don't want to swallow the error.
    // I'll update axios-setup.ts to swallow the error.
    it("interceptor de erro de resposta deve tratar erros da store graciosamente", async () => {
        const feedbackStore = useFeedbackStore();
        // Simular erro ao chamar show
        vi.spyOn(feedbackStore, "show").mockImplementation(() => {
            throw new Error("Store error");
        });

        const error = new Error("Generic failure");

        // O interceptor deve rejeitar o erro mesmo quando feedbackStore.show falha
        await expect(responseErrorInterceptor(error)).rejects.toEqual(error);

        // Verificar que tentou exibir o erro (e falhou)
        expect(feedbackStore.show).toHaveBeenCalled();
    });
});
