import {createPinia, setActivePinia} from "pinia";
import {beforeEach, describe, expect, it, vi} from "vitest";
import * as usuarioService from "@/services/usuarioService";
import {initPinia} from "@/test-utils/helpers";
import {Perfil} from "@/types/tipos";
import {usePerfilStore} from "../perfil";

vi.mock("@/services/usuarioService");

// Mock localStorage globally
const mockLocalStorage = (() => {
    let store: { [key: string]: string } = {};
    return {
        getItem: vi.fn((key: string) => store[key] || null),
        setItem: vi.fn((key: string, value: string) => {
            store[key] = value;
        }),
        removeItem: vi.fn((key: string) => {
            delete store[key];
        }),
        clear: vi.fn(() => {
            store = {};
        }),
    };
})();

Object.defineProperty(window, "localStorage", {value: mockLocalStorage});

describe("usePerfilStore", () => {
    let perfilStore: ReturnType<typeof usePerfilStore>;

    beforeEach(() => {
        // Clear the mock localStorage before each test
        mockLocalStorage.clear();
        mockLocalStorage.setItem("usuarioCodigo", "9"); // Set default for initial state

        initPinia();
        perfilStore = usePerfilStore();
    });

    afterEach(() => {
        vi.clearAllMocks();
    });

    it("should initialize with default values if localStorage is empty", () => {
        expect(perfilStore.usuarioCodigo).toBe(9);
        expect(perfilStore.perfilSelecionado).toBeNull();
        expect(perfilStore.unidadeSelecionada).toBeNull();
    });

    it("should initialize with values from localStorage if available", () => {
        mockLocalStorage.setItem("usuarioCodigo", "10");
        mockLocalStorage.setItem("perfilSelecionado", "USER");
        mockLocalStorage.setItem("unidadeSelecionada", "123");
        mockLocalStorage.setItem("unidadeSelecionadaSigla", "U10"); // Add this

        // Create a new Pinia instance and store to pick up new localStorage mocks
        setActivePinia(createPinia()); // Re-activate Pinia with a fresh instance
        const newPerfilStore = usePerfilStore(); // Get a fresh store instance

        expect(newPerfilStore.usuarioCodigo).toBe(10);
        expect(newPerfilStore.perfilSelecionado).toBe("USER");
        expect(newPerfilStore.unidadeSelecionada).toBe(123);
        expect(newPerfilStore.unidadeSelecionadaSigla).toBe("U10"); // Add this
    });

    describe("actions", () => {
        const mockUsuarioService = vi.mocked(usuarioService);

        it("definirUsuarioCodigo should update usuarioCodigo and store it in localStorage", () => {
            perfilStore.definirUsuarioCodigo(15);
            expect(perfilStore.usuarioCodigo).toBe(15);
            expect(mockLocalStorage.setItem).toHaveBeenCalledWith("usuarioCodigo", "15");
        });

        it("definirPerfilUnidade should update perfilSelecionado and unidadeSelecionada and store them in localStorage", () => {
            const unidadeCodigo = 123;
            const unidadeSigla = "TEST_SIGLA";

            perfilStore.definirPerfilUnidade(Perfil.ADMIN, unidadeCodigo, unidadeSigla);

            expect(perfilStore.perfilSelecionado).toBe(Perfil.ADMIN);
            expect(perfilStore.unidadeSelecionada).toBe(unidadeCodigo);
            expect(perfilStore.unidadeSelecionadaSigla).toBe(unidadeSigla); // Add this
            expect(mockLocalStorage.setItem).toHaveBeenCalledWith(
                "perfilSelecionado",
                Perfil.ADMIN,
            );
            expect(mockLocalStorage.setItem).toHaveBeenCalledWith(
                "unidadeSelecionada",
                unidadeCodigo.toString(),
            );
            expect(mockLocalStorage.setItem).toHaveBeenCalledWith( // Add this
                "unidadeSelecionadaSigla",
                unidadeSigla,
            );
            perfilStore.definirPerfilUnidade(Perfil.ADMIN, unidadeCodigo, unidadeSigla, "Nome Teste");
            expect(perfilStore.usuarioNome).toBe("Nome Teste");
            expect(mockLocalStorage.setItem).toHaveBeenCalledWith("usuarioNome", "Nome Teste");
        });

        it("loginCompleto should authenticate, fetch profiles, and auto-select if one profile", async () => {
            const perfilUnidade = {
                perfil: Perfil.CHEFE,
                unidade: {codigo: 1, sigla: "UT", nome: "Unidade UT"},
                siglaUnidade: "UT",
            };
            const mockLoginResponse = {
                perfil: Perfil.CHEFE,
                unidadeCodigo: 1,
                tituloEleitoral: 123,
                token: "fake-token",
                nome: "João da Silva",
            };

            mockUsuarioService.autenticar.mockResolvedValue(true);
            mockUsuarioService.autorizar.mockResolvedValue([perfilUnidade]);
            mockUsuarioService.entrar.mockResolvedValue(mockLoginResponse);

            const result = await perfilStore.loginCompleto("123", "pass");

            expect(mockUsuarioService.autenticar).toHaveBeenCalledWith({
                tituloEleitoral: 123,
                senha: "pass",
            });
            expect(mockUsuarioService.autorizar).toHaveBeenCalledWith(123);
            expect(mockUsuarioService.entrar).toHaveBeenCalled();
            expect(perfilStore.perfilSelecionado).toBe(Perfil.CHEFE);
            expect(perfilStore.unidadeSelecionada).toBe(1);
            expect(perfilStore.unidadeSelecionadaSigla).toBe("UT"); // Add this
            expect(result).toBe(true);
        });

        it("loginCompleto should not auto-select if multiple profiles", async () => {
            const perfis = [
                {
                    perfil: Perfil.CHEFE,
                    unidade: {codigo: 1, nome: "Unidade 1", sigla: "U1"},
                    siglaUnidade: "U1",
                },
                {
                    perfil: Perfil.GESTOR,
                    unidade: {codigo: 2, nome: "Unidade 2", sigla: "U2"},
                    siglaUnidade: "U2",
                },
            ];
            mockUsuarioService.autenticar.mockResolvedValue(true);
            mockUsuarioService.autorizar.mockResolvedValue(perfis);

            await perfilStore.loginCompleto("123", "pass");

            expect(mockUsuarioService.entrar).not.toHaveBeenCalled();
            expect(perfilStore.perfisUnidades).toEqual(perfis);
            expect(perfilStore.perfilSelecionado).toBeNull(); // Add this
            expect(perfilStore.unidadeSelecionada).toBeNull(); // Add this
            expect(perfilStore.unidadeSelecionadaSigla).toBeNull(); // Add this
        });

        it("selecionarPerfilUnidade should call entrar and set profile", async () => {
            const perfilUnidade = {
                perfil: Perfil.GESTOR,
                unidade: {codigo: 2, sigla: "XYZ", nome: "Unidade XYZ"},
                siglaUnidade: "XYZ",
            };
            const mockLoginResponse = {
                perfil: Perfil.GESTOR,
                unidadeCodigo: 2,
                tituloEleitoral: 456,
                token: "fake-token",
                nome: "Maria Oliveira",
            };
            mockUsuarioService.entrar.mockResolvedValue(mockLoginResponse);

            await perfilStore.selecionarPerfilUnidade(456, perfilUnidade);

            expect(mockUsuarioService.entrar).toHaveBeenCalledWith({
                tituloEleitoral: 456,
                perfil: Perfil.GESTOR,
                unidadeCodigo: 2,
            });
            expect(perfilStore.perfilSelecionado).toBe(Perfil.GESTOR);
            expect(perfilStore.unidadeSelecionada).toBe(2);
            expect(perfilStore.unidadeSelecionadaSigla).toBe("XYZ"); // Add this
            expect(perfilStore.usuarioCodigo).toBe(456);
        });

        it("should handle error in loginCompleto", async () => {
            mockUsuarioService.autenticar.mockRejectedValue(new Error("Fail"));
            await expect(perfilStore.loginCompleto("123", "pass")).rejects.toThrow(
                "Fail",
            );
            expect(perfilStore.lastError).toBeTruthy();
        });

        it("should handle error in selecionarPerfilUnidade", async () => {
            mockUsuarioService.entrar.mockRejectedValue(new Error("Fail"));
            const perfilUnidade = {
                perfil: Perfil.GESTOR,
                unidade: {codigo: 2, sigla: "XYZ", nome: "Unidade XYZ"},
                siglaUnidade: "XYZ",
            };
            await expect(
                perfilStore.selecionarPerfilUnidade(123, perfilUnidade),
            ).rejects.toThrow("Fail");
            expect(perfilStore.lastError).toBeTruthy();
        });

        it("should return false if authentication fails in loginCompleto", async () => {
            mockUsuarioService.autenticar.mockResolvedValue(false);
            const result = await perfilStore.loginCompleto("123", "pass");
            expect(result).toBe(false);
        });

        it("should logout correctly", () => {
            perfilStore.logout();
            expect(perfilStore.usuarioCodigo).toBeNull();
            expect(perfilStore.perfilSelecionado).toBeNull();
            expect(perfilStore.unidadeSelecionada).toBeNull();
            expect(mockLocalStorage.removeItem).toHaveBeenCalledTimes(7);
        });

        it("should clear error", () => {
            perfilStore.lastError = { kind: "unexpected", message: "Error", subErrors: [] };
            perfilStore.clearError();
            expect(perfilStore.lastError).toBeNull();
        });
    });
});
