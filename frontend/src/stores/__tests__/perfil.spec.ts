import {createPinia, setActivePinia} from "pinia";
import {beforeEach, describe, expect, it, vi} from "vitest";
import * as usuarioService from "@/services/usuarioService";
import {setupStoreTest} from "@/test-utils/storeTestHelpers";
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

Object.defineProperty(window, "localStorage", { value: mockLocalStorage });

describe("usePerfilStore", () => {
    // Setup localStorage BEFORE the store is initialized
    beforeEach(() => {
        mockLocalStorage.clear();
        mockLocalStorage.setItem("usuarioCodigo", "9"); // Set default for initial state
    });

    const context = setupStoreTest(usePerfilStore);

    it("deve inicializar com valores padrão se o localStorage estiver vazio", () => {
        expect(context.store.usuarioCodigo).toBe("9");
        expect(context.store.perfilSelecionado).toBeNull();
        expect(context.store.unidadeSelecionada).toBeNull();
    });

    it("deve inicializar com valores do localStorage se disponíveis", () => {
        mockLocalStorage.setItem("usuarioCodigo", "10");
        mockLocalStorage.setItem("perfilSelecionado", "USER");
        mockLocalStorage.setItem("unidadeSelecionada", "123");
        mockLocalStorage.setItem("unidadeSelecionadaSigla", "U10");

        // Re-initialize store to pick up new localStorage values
        setActivePinia(createPinia());
        const newPerfilStore = usePerfilStore();

        expect(newPerfilStore.usuarioCodigo).toBe("10");
        expect(newPerfilStore.perfilSelecionado).toBe("USER");
        expect(newPerfilStore.unidadeSelecionada).toBe(123);
        expect(newPerfilStore.unidadeSelecionadaSigla).toBe("U10");
    });

    describe("actions", () => {
        const mockUsuarioService = vi.mocked(usuarioService);

        it("definirUsuarioCodigo deve atualizar usuarioCodigo e armazená-lo no localStorage", () => {
            context.store.definirUsuarioCodigo("15");
            expect(context.store.usuarioCodigo).toBe("15");
            expect(mockLocalStorage.setItem).toHaveBeenCalledWith("usuarioCodigo", "15");
        });

        it("definirPerfilUnidade deve atualizar perfilSelecionado e unidadeSelecionada e armazená-los no localStorage", () => {
            const unidadeCodigo = 123;
            const unidadeSigla = "TEST_SIGLA";

            context.store.definirPerfilUnidade(Perfil.ADMIN, unidadeCodigo, unidadeSigla);

            expect(context.store.perfilSelecionado).toBe(Perfil.ADMIN);
            expect(context.store.unidadeSelecionada).toBe(unidadeCodigo);
            expect(context.store.unidadeSelecionadaSigla).toBe(unidadeSigla);
            expect(mockLocalStorage.setItem).toHaveBeenCalledWith(
                "perfilSelecionado",
                Perfil.ADMIN,
            );
            expect(mockLocalStorage.setItem).toHaveBeenCalledWith(
                "unidadeSelecionada",
                unidadeCodigo.toString(),
            );
            expect(mockLocalStorage.setItem).toHaveBeenCalledWith(
                "unidadeSelecionadaSigla",
                unidadeSigla,
            );
            context.store.definirPerfilUnidade(Perfil.ADMIN, unidadeCodigo, unidadeSigla, "Nome Teste");
            expect(context.store.usuarioNome).toBe("Nome Teste");
            expect(mockLocalStorage.setItem).toHaveBeenCalledWith("usuarioNome", "Nome Teste");
        });

        it("loginCompleto deve autenticar, buscar perfis e selecionar automaticamente se houver apenas um perfil", async () => {
            const perfilUnidade = {
                perfil: Perfil.CHEFE,
                unidade: { codigo: 1, sigla: "UT", nome: "Unidade UT" },
                siglaUnidade: "UT",
            };
            const mockLoginResponse = {
                perfil: Perfil.CHEFE,
                unidadeCodigo: 1,
                tituloEleitoral: "123",
                token: "fake-token",
                nome: "João da Silva",
            };

            mockUsuarioService.autenticar.mockResolvedValue(true);
            mockUsuarioService.autorizar.mockResolvedValue([perfilUnidade]);
            mockUsuarioService.entrar.mockResolvedValue(mockLoginResponse);

            const result = await context.store.loginCompleto("123", "pass");

            expect(mockUsuarioService.autenticar).toHaveBeenCalledWith({
                tituloEleitoral: "123",
                senha: "pass",
            });
            expect(mockUsuarioService.autorizar).toHaveBeenCalledWith("123");
            expect(mockUsuarioService.entrar).toHaveBeenCalled();
            expect(context.store.perfilSelecionado).toBe(Perfil.CHEFE);
            expect(context.store.unidadeSelecionada).toBe(1);
            expect(context.store.unidadeSelecionadaSigla).toBe("UT");
            expect(result).toBe(true);
        });

        it("loginCompleto não deve selecionar automaticamente se houver múltiplos perfis", async () => {
            const perfis = [
                {
                    perfil: Perfil.CHEFE,
                    unidade: { codigo: 1, nome: "Unidade 1", sigla: "U1" },
                    siglaUnidade: "U1",
                },
                {
                    perfil: Perfil.GESTOR,
                    unidade: { codigo: 2, nome: "Unidade 2", sigla: "U2" },
                    siglaUnidade: "U2",
                },
            ];
            mockUsuarioService.autenticar.mockResolvedValue(true);
            mockUsuarioService.autorizar.mockResolvedValue(perfis);

            await context.store.loginCompleto("123", "pass");

            expect(mockUsuarioService.entrar).not.toHaveBeenCalled();
            expect(context.store.perfisUnidades).toEqual(perfis);
            expect(context.store.perfilSelecionado).toBeNull();
            expect(context.store.unidadeSelecionada).toBeNull();
            expect(context.store.unidadeSelecionadaSigla).toBeNull();
        });

        it("selecionarPerfilUnidade deve chamar entrar e definir o perfil", async () => {
            const perfilUnidade = {
                perfil: Perfil.GESTOR,
                unidade: { codigo: 2, sigla: "XYZ", nome: "Unidade XYZ" },
                siglaUnidade: "XYZ",
            };
            const mockLoginResponse = {
                perfil: Perfil.GESTOR,
                unidadeCodigo: 2,
                tituloEleitoral: "456",
                token: "fake-token",
                nome: "Maria Oliveira",
            };
            mockUsuarioService.entrar.mockResolvedValue(mockLoginResponse);

            await context.store.selecionarPerfilUnidade("456", perfilUnidade);

            expect(mockUsuarioService.entrar).toHaveBeenCalledWith({
                tituloEleitoral: "456",
                perfil: Perfil.GESTOR,
                unidadeCodigo: 2,
            });
            expect(context.store.perfilSelecionado).toBe(Perfil.GESTOR);
            expect(context.store.unidadeSelecionada).toBe(2);
            expect(context.store.unidadeSelecionadaSigla).toBe("XYZ");
            expect(context.store.usuarioCodigo).toBe("456");
        });

        it("deve lidar com erro em loginCompleto", async () => {
            mockUsuarioService.autenticar.mockRejectedValue(new Error("Fail"));
            await expect(context.store.loginCompleto("123", "pass")).rejects.toThrow(
                "Fail",
            );
            expect(context.store.lastError).toBeTruthy();
        });

        it("deve lidar com erro em selecionarPerfilUnidade", async () => {
            mockUsuarioService.entrar.mockRejectedValue(new Error("Fail"));
            const perfilUnidade = {
                perfil: Perfil.GESTOR,
                unidade: { codigo: 2, sigla: "XYZ", nome: "Unidade XYZ" },
                siglaUnidade: "XYZ",
            };
            await expect(
                context.store.selecionarPerfilUnidade("123", perfilUnidade),
            ).rejects.toThrow("Fail");
            expect(context.store.lastError).toBeTruthy();
        });

        it("deve retornar falso se a autenticação falhar em loginCompleto", async () => {
            mockUsuarioService.autenticar.mockResolvedValue(false);
            const result = await context.store.loginCompleto("123", "pass");
            expect(result).toBe(false);
        });

        it("deve fazer logout corretamente", () => {
            context.store.logout();
            expect(context.store.usuarioCodigo).toBeNull();
            expect(context.store.perfilSelecionado).toBeNull();
            expect(context.store.unidadeSelecionada).toBeNull();
            expect(mockLocalStorage.removeItem).toHaveBeenCalledTimes(7);
        });

        it("deve limpar o erro", () => {
            context.store.lastError = { kind: "unexpected", message: "Error", subErrors: [] };
            context.store.clearError();
            expect(context.store.lastError).toBeNull();
        });
    });
});
