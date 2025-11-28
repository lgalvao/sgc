import {createTestingPinia} from "@pinia/testing";
import {flushPromises, mount} from "@vue/test-utils";
import {BFormInput} from "bootstrap-vue-next";
import {afterEach, beforeEach, describe, expect, it, vi} from "vitest";
import {computed} from "vue";
import ImportarAtividadesModal from "@/components/ImportarAtividadesModal.vue";
import * as usePerfilModule from "@/composables/usePerfil";
import * as analiseService from "@/services/analiseService";
// Import services to mock/spy
import * as atividadeService from "@/services/atividadeService";
import * as cadastroService from "@/services/cadastroService";
import * => {
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
vi.mock("vue-router", () => ({
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

    beforeAll(() => {
        // Access interceptors from the hoisted mock instance
        // These calls happened when '../axios-setup' was imported
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
        // const store = useNotificacoesStore(); // Remove this
        const avisoSpy = vi.spyOn(ToastService, "erro"); // Use ToastService

        const error = {response: {status: 401}};
        await expect(responseErrorInterceptor(error)).rejects.toEqual(error);
        expect(avisoSpy).toHaveBeenCalledWith(
            "Não Autorizado",
            expect.stringContaining("Sua sessão expirou"),
        );
        expect(router.push).toHaveBeenCalledWith("/login");
    });

    it("response error interceptor should not show global error for 400, 404, 409, 422", async () => {
        // const store = useNotificacoesStore(); // Remove this
        const avisoSpy = vi.spyOn(ToastService, "erro"); // Use ToastService

        const error = {response: {status: 400}};
        await expect(responseErrorInterceptor(error)).rejects.toEqual(error);
        expect(avisoSpy).not.toHaveBeenCalled();
    });

    it("response error interceptor should show unexpected error for 500", async () => {
        // const store = useNotificacoesStore(); // Remove this
        const avisoSpy = vi.spyOn(ToastService, "erro"); // Use ToastService

        const error = {
            response: {status: 500, data: {message: "Server Error"}},
        };
        await expect(responseErrorInterceptor(error)).rejects.toEqual(error);
        expect(avisoSpy).toHaveBeenCalledWith("Erro Inesperado", "Server Error");
    });

    it("response error interceptor should show network error", async () => {
        // const store = useNotificacoesStore(); // Remove this
        const avisoSpy = vi.spyOn(ToastService, "erro"); // Use ToastService

        const error = {request: {}}; // No response, but request exists -> Network error usually
        await expect(responseErrorInterceptor(error)).rejects.toEqual(error);
        expect(avisoSpy).toHaveBeenCalledWith(
            "Erro de Rede",
            expect.stringContaining("Não foi possível conectar"),
        );
    });
});
