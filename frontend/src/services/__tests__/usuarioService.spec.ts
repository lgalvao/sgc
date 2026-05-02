import {beforeEach, describe, expect, it, vi} from "vitest";
import apiClient from "@/axios-setup";
import * as usuarioService from "../usuarioService";

vi.mock("@/axios-setup", () => ({
    default: {
        get: vi.fn(),
        post: vi.fn(),
    },
}));

describe("usuarioService", () => {
    beforeEach(() => {
        vi.clearAllMocks();
    });

    const getMock = vi.mocked(apiClient.get);
    const postMock = vi.mocked(apiClient.post);

    it("mapeia perfis e sessão para o frontend", () => {
        const perfilUnidade = usuarioService.mapPerfilUnidadeToFrontend({
            perfil: "ADMIN",
            unidade: {codigo: 1, nome: "Unidade", sigla: "UND"},
            siglaUnidade: "UND",
        } as never);
        expect(perfilUnidade).toEqual({
            perfil: "ADMIN",
            unidade: {codigo: 1, nome: "Unidade", sigla: "UND"},
            siglaUnidade: "UND",
        });

        expect(usuarioService.mapPermissoesSessaoToFrontend({
            mostrarCriarProcesso: true,
            mostrarArvoreCompletaUnidades: false,
            mostrarCtaPainelVazio: true,
            mostrarDiagnosticoOrganizacional: false,
            mostrarMenuConfiguracoes: true,
            mostrarMenuAdministradores: false,
            mostrarCriarAtribuicaoTemporaria: true,
        } as never)).toEqual({
            mostrarCriarProcesso: true,
            mostrarArvoreCompletaUnidades: false,
            mostrarCtaPainelVazio: true,
            mostrarDiagnosticoOrganizacional: false,
            mostrarMenuConfiguracoes: true,
            mostrarMenuAdministradores: false,
            mostrarCriarAtribuicaoTemporaria: true,
        });

        const sessao = usuarioService.mapSessaoLoginToFrontend({
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
        } as never);
        expect(sessao.perfil).toBe("CHEFE");

        const fluxoComSessao = usuarioService.mapFluxoLoginToFrontend({
            autenticado: true,
            requerSelecaoPerfil: false,
            perfisUnidades: [{
                perfil: "GESTOR",
                unidade: {codigo: 2, nome: "Unidade 2", sigla: "U2"},
                siglaUnidade: "U2",
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
        expect(fluxoComSessao.sessao).not.toBeNull();

        const fluxoSemSessao = usuarioService.mapFluxoLoginToFrontend({
            autenticado: false,
            requerSelecaoPerfil: true,
            perfisUnidades: [],
            sessao: null,
        } as never);
        expect(fluxoSemSessao.sessao).toBeNull();
    });

    it("mapeia usuarios e arrays de usuarios", () => {
        expect(usuarioService.mapUsuarioToFrontend({
            tituloEleitoral: "123",
            nome: "Pessoa",
            matricula: "M123",
            email: "pessoa@teste.com",
            ramal: "123",
            unidade: {codigo: 1, nome: "Unidade", sigla: "UND"},
            perfis: [],
        } as never).codigo).toBe(123);

        expect(usuarioService.mapUsuarioToFrontend({
            tituloEleitoral: "abc",
            nome: "Pessoa",
            matricula: "M123",
            email: "pessoa@teste.com",
            ramal: "123",
            unidade: {codigo: 1, nome: "Unidade", sigla: "UND"},
            perfis: [],
        } as never).codigo).toBe(0);

        expect(usuarioService.mapVWUsuarioToUsuario({
            titulo: "999",
            matricula: "M999",
            nome: "VW",
            unidade_lot_codigo: 1,
            email: "vw@teste.com",
            ramal: "321",
        })).toMatchObject({codigo: 999, nome: "VW", tituloEleitoral: "999", matricula: "M999"});

        expect(usuarioService.mapVWUsuarioToUsuario({
            titulo: "123",
            matricula: "M123",
            nome: "Nome",
            unidade_lot_codigo: 1,
            email: "teste@teste",
            ramal: "777",
            unidade_sigla: "UND",
        })).toMatchObject({
            codigo: 123,
            nome: "Nome",
            tituloEleitoral: "123",
            ramal: "777",
            unidade: {codigo: 1, sigla: "UND"}
        });

        expect(usuarioService.mapVWUsuariosArray([
            {titulo: "1", matricula: "M1", nome: "A", email: "a@a", ramal: "1", unidade_lot_codigo: 1},
            {titulo: "2", matricula: "M2", nome: "B", email: "b@b", ramal: "2", unidade_lot_codigo: 2},
        ])).toHaveLength(2);
    });

    it("faz chamadas HTTP e respeita cache por titulo", async () => {
        postMock
            .mockResolvedValueOnce({data: {
                autenticado: true,
                requerSelecaoPerfil: false,
                perfisUnidades: [],
                sessao: null,
            }} as never)
            .mockResolvedValueOnce({data: {
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
            }} as never)
            .mockResolvedValueOnce({data: undefined} as never);
        getMock.mockResolvedValueOnce({data: {tituloEleitoral: "123", nome: "Pessoa", unidade: {codigo: 1, nome: "Unidade", sigla: "UND"}, perfis: []}} as never);
        await usuarioService.login({tituloEleitoral: "123", senha: "abc"});
        await usuarioService.entrar({perfil: "ADMIN", unidadeCodigo: 1});
        await usuarioService.logout();

        expect(apiClient.post).toHaveBeenCalledWith("/usuarios/login", {tituloEleitoral: "123", senha: "abc"});
        expect(apiClient.post).toHaveBeenCalledWith("/usuarios/entrar", {perfil: "ADMIN", unidadeCodigo: 1});
        expect(apiClient.post).toHaveBeenCalledWith("/usuarios/logout");

        getMock.mockResolvedValueOnce({data: [{codigo: 1}]} as never);
        await usuarioService.buscarUsuariosPorUnidade(1);
        expect(apiClient.get).toHaveBeenCalledWith("/unidades/1/usuarios");

        getMock.mockResolvedValueOnce({data: {codigo: 9, nome: "Cache"}} as never);
        const primeiro = await usuarioService.buscarUsuarioPorTitulo("999");
        const segundo = await usuarioService.buscarUsuarioPorTitulo("999");
        expect(primeiro).toEqual(segundo);
        expect(apiClient.get).toHaveBeenCalledWith("/usuarios/999");

        getMock.mockResolvedValueOnce({data: [{codigo: 1}]} as never);
        await usuarioService.pesquisarUsuarios("termo");
        expect(apiClient.get).toHaveBeenCalledWith("/usuarios/pesquisar", {params: {termo: "termo"}});
    });
});
