import {createPinia, setActivePinia} from "pinia";
import {beforeEach, describe, expect, it, vi} from "vitest";
import {nextTick} from "vue";
import * as usuarioService from "@/services/usuarioService";
import {setupStoreTest} from "@/test-utils/storeTestHelpers";
import {Perfil} from "@/types/tipos";
import {usePerfilStore} from "../perfil";

vi.mock("@/services/usuarioService");

const subprocessoStoreMock = {
    invalidar: vi.fn(),
};

const processoStoreMock = {
    invalidar: vi.fn(),
};

const painelStoreMock = {
    invalidar: vi.fn(),
};

const unidadeStoreMock = {
    invalidarCache: vi.fn(),
};

vi.mock("@/stores/painel", () => ({
    usePainelStore: () => painelStoreMock,
}));

vi.mock("@/stores/processo", () => ({
    useProcessoStore: () => processoStoreMock,
}));

vi.mock("@/stores/subprocesso", () => ({
    useSubprocessoStore: () => subprocessoStoreMock,
}));

vi.mock("@/stores/unidade", () => ({
    useUnidadeStore: () => unidadeStoreMock,
}));

const createMockStorage = () => {
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
};

const mockLocalStorage = createMockStorage();
const mockSessionStorage = createMockStorage();

Object.defineProperty(globalThis, "localStorage", {value: mockLocalStorage});
Object.defineProperty(globalThis, "sessionStorage", {value: mockSessionStorage});

describe("usePerfilStore", () => {
    const permissoesAdmin = {
        mostrarCriarProcesso: true,
        mostrarArvoreCompletaUnidades: true,
        mostrarCtaPainelVazio: true,
        mostrarDiagnosticoOrganizacional: true,
        mostrarMenuConfiguracoes: true,
        mostrarMenuAdministradores: true,
        mostrarCriarAtribuicaoTemporaria: true,
    };

    beforeEach(() => {
        mockLocalStorage.clear();
        mockSessionStorage.clear();
        mockSessionStorage.setItem("usuarioCodigo", JSON.stringify("9")); // Set default for initial state
        painelStoreMock.invalidar.mockClear();
        processoStoreMock.invalidar.mockClear();
        subprocessoStoreMock.invalidar.mockClear();
        unidadeStoreMock.invalidarCache.mockClear();
    });

    const context = setupStoreTest(usePerfilStore);

    it("deve inicializar com valores padrão se o localStorage estiver vazio", () => {
        expect(context.store.usuarioCodigo).toBe("9");
        expect(context.store.perfilSelecionado).toBeNull();
        expect(context.store.unidadeSelecionada).toBeNull();
    });

    it("deve inicializar com valores do localStorage/sessionStorage se disponíveis", () => {
        mockSessionStorage.setItem("usuarioCodigo", JSON.stringify("10"));
        mockLocalStorage.setItem("perfilSelecionado", JSON.stringify("USER"));
        mockLocalStorage.setItem("unidadeSelecionada", JSON.stringify(123));
        mockLocalStorage.setItem("unidadeSelecionadaSigla", JSON.stringify("U10"));

        setActivePinia(createPinia());
        const newPerfilStore = usePerfilStore();

        expect(newPerfilStore.usuarioCodigo).toBe("10");
        expect(newPerfilStore.perfilSelecionado).toBe("USER");
        expect(newPerfilStore.unidadeSelecionada).toBe(123);
        expect(newPerfilStore.unidadeSelecionadaSigla).toBe("U10");
    });

    describe("actions", () => {
        const mockUsuarioService = vi.mocked(usuarioService);

        it("definirUsuarioCodigo deve atualizar usuarioCodigo", () => {
            context.store.definirUsuarioCodigo("15");
            expect(context.store.usuarioCodigo).toBe("15");
        });

        it("NÃO deve armazenar PII (usuarioCodigo, usuarioNome) no localStorage para evitar vazamento via XSS/Sessão", async () => {
            vi.clearAllMocks();
            context.store.definirUsuarioCodigo("999999");
            context.store.definirPerfilUnidade({
                perfil: Perfil.ADMIN,
                unidadeCodigo: 123,
                unidadeSigla: "TEST",
                permissoes: permissoesAdmin,
                nome: "Nome Pessoal",
            });
            
            await nextTick();
            
            expect(mockLocalStorage.setItem).not.toHaveBeenCalledWith("usuarioNome", expect.anything());
            
            // Mas deve estar no sessionStorage
            expect(mockSessionStorage.setItem).toHaveBeenCalledWith("usuarioCodigo", JSON.stringify("999999"));
            expect(mockSessionStorage.setItem).toHaveBeenCalledWith("usuarioNome", JSON.stringify("Nome Pessoal"));
        });

        it("definirPerfilUnidade deve atualizar perfilSelecionado e unidadeSelecionada e armazená-los no localStorage", () => {
            const unidadeCodigo = 123;
            const unidadeSigla = "TEST_SIGLA";

            context.store.definirPerfilUnidade({
                perfil: Perfil.ADMIN,
                unidadeCodigo,
                unidadeSigla,
                permissoes: permissoesAdmin,
            });

            expect(context.store.perfilSelecionado).toBe(Perfil.ADMIN);
            expect(context.store.unidadeSelecionada).toBe(unidadeCodigo);
            expect(context.store.unidadeSelecionadaSigla).toBe(unidadeSigla);
            expect(painelStoreMock.invalidar).toHaveBeenCalledTimes(1);
            expect(processoStoreMock.invalidar).toHaveBeenCalledTimes(1);
            expect(subprocessoStoreMock.invalidar).toHaveBeenCalledTimes(1);
            expect(unidadeStoreMock.invalidarCache).toHaveBeenCalledTimes(1);

            context.store.definirPerfilUnidade({
                perfil: Perfil.ADMIN,
                unidadeCodigo,
                unidadeSigla,
                permissoes: permissoesAdmin,
                nome: "Nome teste",
            });
            expect(context.store.usuarioNome).toBe("Nome teste");
        });

        it("iniciarLogin deve carregar perfis e selecionar automaticamente se houver apenas um perfil", async () => {
            const perfilUnidade = {
                perfil: Perfil.CHEFE,
                unidade: {codigo: 1, sigla: "UT", nome: "Unidade UT"},
                siglaUnidade: "UT",
            };
            const mockFluxoLogin = {
                autenticado: true,
                requerSelecaoPerfil: false,
                perfisUnidades: [perfilUnidade],
                sessao: {
                    perfil: Perfil.CHEFE,
                    unidadeCodigo: 1,
                    tituloEleitoral: "123",
                    nome: "João da Silva",
                    permissoes: {
                        ...permissoesAdmin,
                        mostrarCriarProcesso: false,
                        mostrarArvoreCompletaUnidades: false,
                        mostrarCtaPainelVazio: false,
                        mostrarDiagnosticoOrganizacional: false,
                        mostrarMenuConfiguracoes: false,
                        mostrarMenuAdministradores: false,
                        mostrarCriarAtribuicaoTemporaria: false,
                    },
                },
            };

            mockUsuarioService.login.mockResolvedValue(mockFluxoLogin as any);

            const result = await context.store.iniciarLogin("123", "pass");

            expect(mockUsuarioService.login).toHaveBeenCalledWith({
                tituloEleitoral: "123",
                senha: "pass",
            });
            expect(mockUsuarioService.entrar).not.toHaveBeenCalled();
            expect(context.store.perfilSelecionado).toBe(Perfil.CHEFE);
            expect(context.store.unidadeSelecionada).toBe(1);
            expect(context.store.unidadeSelecionadaSigla).toBe("UT");
            expect(context.store.permissoesSessao?.mostrarCriarProcesso).toBe(false);
            expect(result.autenticado).toBe(true);
            expect(result.sessao?.tituloEleitoral).toBe("123");
        });

        it("iniciarLogin não deve selecionar automaticamente se houver múltiplos perfis", async () => {
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
            mockUsuarioService.login.mockResolvedValue({
                autenticado: true,
                requerSelecaoPerfil: true,
                perfisUnidades: perfis,
                sessao: null,
            } as any);

            await context.store.iniciarLogin("123", "pass");

            expect(mockUsuarioService.entrar).not.toHaveBeenCalled();
            expect(context.store.perfisUnidades).toEqual(perfis);
            expect(context.store.perfilSelecionado).toBeNull();
            expect(context.store.unidadeSelecionada).toBeNull();
            expect(context.store.unidadeSelecionadaSigla).toBeNull();
        });

        it("concluirLoginComPerfil deve chamar entrar e definir o perfil", async () => {
            const perfilUnidade = {
                perfil: Perfil.GESTOR,
                unidade: {codigo: 2, sigla: "XYZ", nome: "Unidade XYZ"},
                siglaUnidade: "XYZ",
            };
            const mockLoginResponse = {
                perfil: Perfil.GESTOR,
                unidadeCodigo: 2,
                tituloEleitoral: "456",
                nome: "Maria oliveira",
                permissoes: {
                    ...permissoesAdmin,
                    mostrarCriarProcesso: false,
                    mostrarArvoreCompletaUnidades: false,
                    mostrarCtaPainelVazio: false,
                    mostrarDiagnosticoOrganizacional: false,
                    mostrarMenuConfiguracoes: false,
                    mostrarMenuAdministradores: false,
                    mostrarCriarAtribuicaoTemporaria: false,
                },
            };
            mockUsuarioService.entrar.mockResolvedValue(mockLoginResponse);

            await context.store.concluirLoginComPerfil(perfilUnidade);

            expect(mockUsuarioService.entrar).toHaveBeenCalledWith({
                perfil: Perfil.GESTOR,
                unidadeCodigo: 2,
            });
            expect(context.store.perfilSelecionado).toBe(Perfil.GESTOR);
            expect(context.store.unidadeSelecionada).toBe(2);
            expect(context.store.unidadeSelecionadaSigla).toBe("XYZ");
            expect(context.store.permissoesSessao?.mostrarMenuConfiguracoes).toBe(false);
            expect(context.store.usuarioCodigo).toBe("456");
        });

        it("deve lidar com erro em iniciarLogin", async () => {
            mockUsuarioService.login.mockRejectedValue(new Error("Fail"));
            await expect(context.store.iniciarLogin("123", "pass")).rejects.toThrow(
                "Fail",
            );
            expect(context.store.lastError).toBeTruthy();
        });

        it("deve lidar com erro em concluirLoginComPerfil", async () => {
            mockUsuarioService.entrar.mockRejectedValue(new Error("Fail"));
            const perfilUnidade = {
                perfil: Perfil.GESTOR,
                unidade: {codigo: 2, sigla: "XYZ", nome: "Unidade XYZ"},
                siglaUnidade: "XYZ",
            };
            await expect(
                context.store.concluirLoginComPerfil(perfilUnidade),
            ).rejects.toThrow("Fail");
            expect(context.store.lastError).toBeTruthy();
        });

        it("deve retornar fluxo vazio se o login falhar com 401", async () => {
            mockUsuarioService.login.mockRejectedValue({isAxiosError: true, response: {status: 401}});
            const result = await context.store.iniciarLogin("123", "pass");
            expect(result.perfisUnidades).toEqual([]);
            expect(result.autenticado).toBe(false);
            expect(result.sessao).toBeNull();
        });

        it("deve fazer logout corretamente", () => {
            context.store.logout();
            expect(context.store.usuarioCodigo).toBeNull();
            expect(context.store.perfilSelecionado).toBeNull();
            expect(context.store.unidadeSelecionada).toBeNull();
            expect(context.store.permissoesSessao).toBeNull();
            expect(painelStoreMock.invalidar).toHaveBeenCalledTimes(1);
            expect(processoStoreMock.invalidar).toHaveBeenCalledTimes(1);
            expect(subprocessoStoreMock.invalidar).toHaveBeenCalledTimes(1);
            expect(unidadeStoreMock.invalidarCache).toHaveBeenCalledTimes(1);
        });

        it("deve limpar o erro", () => {
            context.store.lastError = {kind: "unexpected", message: "Error", subErrors: []};
            context.store.clearError();
            expect(context.store.lastError).toBeNull();
        });

        it("iniciarLogin deve retornar fluxo vazio para erros 401 ou 404", async () => {
            const mockUsuarioService = vi.mocked(usuarioService);
            mockUsuarioService.login.mockRejectedValue({isAxiosError: true, response: {status: 401}});

            const result = await context.store.iniciarLogin("123", "pass");
            expect(result.perfisUnidades).toEqual([]);
            expect(result.autenticado).toBe(false);

            mockUsuarioService.login.mockRejectedValue({isAxiosError: true, response: {status: 404}});
            const result2 = await context.store.iniciarLogin("123", "pass");
            expect(result2.perfisUnidades).toEqual([]);
            expect(result2.autenticado).toBe(false);

            mockUsuarioService.login.mockRejectedValue(new Error("Other error"));
            await expect(context.store.iniciarLogin("123", "pass")).rejects.toThrow("Other error");
        });
    });

    describe("getters", () => {
        it("unidadeAtual deve retornar unidadeSelecionada se definida", () => {
            context.store.perfilSelecionado = Perfil.ADMIN;
            context.store.unidadeSelecionada = 50;
            expect(context.store.unidadeAtual).toBe(50);
        });

        it("unidadeAtual deve retornar unidade do perfil se unidadeSelecionada for null", () => {
            context.store.perfilSelecionado = Perfil.CHEFE;
            context.store.unidadeSelecionada = null;
            context.store.perfisUnidades = [
                {perfil: Perfil.CHEFE, unidade: {codigo: 60}} as any
            ];
            expect(context.store.unidadeAtual).toBe(60);
        });

        it("unidadeAtual deve retornar null se nenhum perfil selecionado", () => {
            context.store.perfilSelecionado = null;
            expect(context.store.unidadeAtual).toBeNull();
        });

        it("unidadeAtual deve retornar null se perfil não encontrado no mapa", () => {
            context.store.perfilSelecionado = Perfil.GESTOR;
            context.store.unidadeSelecionada = null;
            context.store.perfisUnidades = [];
            expect(context.store.unidadeAtual).toBeNull();
        });
    });
});
