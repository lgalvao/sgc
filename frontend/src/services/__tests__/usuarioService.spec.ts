import {beforeEach, describe, expect, it, vi} from "vitest";
import * as apiUtils from "@/utils/apiUtils";
import * as usuarioService from "../usuarioService";

vi.mock("@/utils/apiUtils", () => ({
    apiGet: vi.fn(),
    apiPost: vi.fn(),
}));

describe("usuarioService", () => {
    beforeEach(() => {
        vi.clearAllMocks();
    });

    const getMock = vi.mocked(apiUtils.apiGet);
    const postMock = vi.mocked(apiUtils.apiPost);

    it("mapeia o fluxo de login no retorno público", async () => {
        postMock
            .mockResolvedValueOnce({
                autenticado: true,
                requerSelecaoPerfil: true,
                perfisUnidades: [{
                    perfil: "GESTOR",
                    unidade: {codigo: 2, nome: "Unidade 2", sigla: "U2"},
                    siglaUnidade: "SIGLA_EXPLICITA",
                }],
                sessao: {
                    tituloEleitoral: "123",
                    nome: "Pessoa",
                    perfil: "CHEFE",
                    unidadeCodigo: 9,
                    permissoes: {
                        mostrarCriarProcesso: true,
                        mostrarArvoreCompletaUnidades: true,
                        mostrarCtaPainelVazio: true,
                        mostrarDiagnosticoOrganizacional: true,
                        mostrarMenuConfiguracoes: false,
                        mostrarMenuAdministradores: false,
                        mostrarCriarAtribuicaoTemporaria: false,
                    },
                },
            } as never);

        const fluxo = await usuarioService.login({tituloEleitoral: "123", senha: "abc"});

        expect(fluxo).toEqual({
            autenticado: true,
            requerSelecaoPerfil: true,
            perfisUnidades: [{
                perfil: "GESTOR",
                unidade: {codigo: 2, nome: "Unidade 2", sigla: "U2"},
                siglaUnidade: "SIGLA_EXPLICITA",
            }],
            sessao: {
                tituloEleitoral: "123",
                nome: "Pessoa",
                perfil: "CHEFE",
                unidadeCodigo: 9,
                permissoes: {
                    mostrarCriarProcesso: true,
                    mostrarArvoreCompletaUnidades: true,
                    mostrarCtaPainelVazio: true,
                    mostrarDiagnosticoOrganizacional: true,
                    mostrarMenuConfiguracoes: false,
                    mostrarMenuAdministradores: false,
                    mostrarCriarAtribuicaoTemporaria: false,
                },
            },
        });
        expect(apiUtils.apiPost).toHaveBeenCalledWith("/usuarios/login", {tituloEleitoral: "123", senha: "abc"});
    });

    it("mapeia a sessão ao concluir login com perfil", async () => {
        postMock.mockResolvedValueOnce({
            tituloEleitoral: "123",
            nome: "Pessoa",
            perfil: "ADMIN",
            unidadeCodigo: 1,
            permissoes: {
                mostrarCriarProcesso: true,
                mostrarArvoreCompletaUnidades: true,
                mostrarCtaPainelVazio: true,
                mostrarDiagnosticoOrganizacional: true,
                mostrarMenuConfiguracoes: true,
                mostrarMenuAdministradores: true,
                mostrarCriarAtribuicaoTemporaria: true,
            },
        } as never);

        const sessao = await usuarioService.entrar({perfil: "ADMIN", unidadeCodigo: 1});

        expect(sessao.perfil).toBe("ADMIN");
        expect(apiUtils.apiPost).toHaveBeenCalledWith("/usuarios/entrar", {perfil: "ADMIN", unidadeCodigo: 1});
    });

    it("faz logout", async () => {
        postMock.mockResolvedValueOnce(undefined as never);

        await usuarioService.logout();

        expect(apiUtils.apiPost).toHaveBeenCalledWith("/usuarios/logout");
    });

    it("pesquisa usuarios", async () => {
        getMock.mockResolvedValueOnce([{codigo: 1}] as never);

        const usuarios = await usuarioService.pesquisarUsuarios("termo");

        expect(usuarios).toEqual([{codigo: 1}]);
        expect(apiUtils.apiGet).toHaveBeenCalledWith("/usuarios/pesquisar", {termo: "termo"});
    });
});
